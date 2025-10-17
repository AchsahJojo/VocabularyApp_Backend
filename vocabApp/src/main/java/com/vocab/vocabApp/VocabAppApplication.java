package com.vocab.vocabApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.vocab")
@EnableMongoRepositories(basePackages = "com.vocab.repository")
public class VocabAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabAppApplication.class, args);
    }

}