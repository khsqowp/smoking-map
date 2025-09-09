package com.smoking_map.smoking_map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching // 캐싱 기능 활성화
@EnableJpaAuditing // JPA Auditing 활성화
@SpringBootApplication
public class SmokingMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmokingMapApplication.class, args);
    }
}