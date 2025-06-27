package com.tutorial.batch.config;


import com.tutorial.batch.Person;
import com.tutorial.batch.PersonItemProcessor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Random;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired private JobBuilder jobBuilderFactory;
    @Autowired private StepBuilder stepBuilderFactory;
    @Autowired private DataSource dataSource;

    @Bean(name = "processPersonJob")
    public Job processPersonJob(JobRepository jobRepository, Step personStep) {
        return new JobBuilder("processPersonJob", jobRepository)
                .start(personStep)
                .build();
    }



    @Bean
    public Step personStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<Person> reader,
                           ItemProcessor<Person, Person> processor,
                           ItemWriter<Person> writer) {
        return new StepBuilder("personStep", jobRepository)
                .<Person, Person>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


    @Bean
    public JdbcCursorItemReader<Person> reader() {
        return new JdbcCursorItemReaderBuilder<Person>()
                .dataSource(dataSource)
                .sql("SELECT id, name, email FROM person")
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .name("personReader")
                .build();
    }

    @Bean
    public ItemProcessor<Person, Person> processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public ItemWriter<Person> writer() {
        return new ItemWriter<>() {
            @Override
            public void write(Chunk<? extends Person> chunk) throws Exception {
                for (Person person : chunk) {
                    System.out.println("✅ Processed: " + person.getName() + " (" + person.getEmail() + ")");
                }
            }
        };
    }


    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListenerSupport() {
            private Timestamp startTime;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                startTime = new Timestamp(System.currentTimeMillis());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                Timestamp endTime = new Timestamp(System.currentTimeMillis());
                int processed = (int)jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount)
                        .sum();

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO job_audit (id, job_name, start_time, end_time, status, processed_count) " +
                                     "VALUES (?, ?, ?, ?, ?, ?)"
                     )) {
                    ps.setInt(1, new Random().nextInt(1_000_000)); // Use sequence in real applications
                    ps.setString(2, jobExecution.getJobInstance().getJobName());
                    ps.setTimestamp(3, startTime);
                    ps.setTimestamp(4, endTime);
                    ps.setString(5, jobExecution.getStatus().toString());
                    ps.setInt(6, processed);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
