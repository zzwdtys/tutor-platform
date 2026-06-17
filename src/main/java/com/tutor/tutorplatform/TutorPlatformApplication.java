package com.tutor.tutorplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tutor.tutorplatform.mapper")
public class TutorPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorPlatformApplication.class, args);
    }

}
