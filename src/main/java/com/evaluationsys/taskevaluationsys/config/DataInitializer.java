package com.evaluationsys.taskevaluationsys.config;

import com.evaluationsys.taskevaluationsys.service.InitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final InitializationService initializationService;

    public DataInitializer(InitializationService initializationService) {
        this.initializationService = initializationService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Application started. Checking for default data...");
        initializationService.initializeDefaultData();
    }
}