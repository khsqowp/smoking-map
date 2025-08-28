package com.smoking_map.smoking_map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // JPA Auditing 활성화
@SpringBootApplication
public class SmokingMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmokingMapApplication.class, args);
    }
}