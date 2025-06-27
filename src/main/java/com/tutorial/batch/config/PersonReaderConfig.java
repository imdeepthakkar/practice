package com.tutorial.batch.config;

import javax.sql.DataSource;

import com.tutorial.batch.Person;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

@Configuration
public class PersonReaderConfig {

    @Bean
    public JdbcCursorItemReader<Person> personReader(DataSource dataSource) {
        JdbcCursorItemReader<Person> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT id, name, email FROM person");
        reader.setRowMapper(new BeanPropertyRowMapper<>(Person.class));
        return reader;
    }
}

