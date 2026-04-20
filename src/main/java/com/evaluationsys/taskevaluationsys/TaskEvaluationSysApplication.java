package com.evaluationsys.taskevaluationsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.evaluationsys.taskevaluationsys.entity")
@EnableJpaRepositories("com.evaluationsys.taskevaluationsys.repository") // <-- scan repositories
public class TaskEvaluationSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskEvaluationSysApplication.class, args);
    }

}