package com.optcg.deckbuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

//A
@SpringBootApplication
@EnableCaching
@EnableAsync
public class OptcgDeckBuilderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptcgDeckBuilderApplication.class, args);
    }
}
