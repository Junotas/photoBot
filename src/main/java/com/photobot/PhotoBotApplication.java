package com.photobot;

import com.photobot.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class PhotoBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoBotApplication.class, args);
    }
}
