package com.tutorial.batch;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {
    @Override
    public Person process(Person person) {
        person.setName(person.getName().toUpperCase());
        return person;
    }
}
