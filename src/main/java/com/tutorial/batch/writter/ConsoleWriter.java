package com.tutorial.batch.writter;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tutorial.batch.Person;
import java.util.List;

@Configuration
public class ConsoleWriter {

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
}

