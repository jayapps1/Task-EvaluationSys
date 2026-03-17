package com.evaluationsys.taskevaluationsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.evaluationsys.taskevaluationsys.entity")  // <-- tell Spring Boot to scan the entity package
public class TaskEvaluationSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskEvaluationSysApplication.class, args);
    }

}
