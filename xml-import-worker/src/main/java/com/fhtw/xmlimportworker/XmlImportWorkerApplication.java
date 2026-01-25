package com.fhtw.xmlimportworker;

import com.fhtw.xmlimportworker.config.XmlImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(XmlImportProperties.class)
public class XmlImportWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmlImportWorkerApplication.class, args);
    }
}
