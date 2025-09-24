package com.groupe2_ionic.eduka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.groupe2_ionic.eduka")
@EntityScan(basePackages = "com.groupe2_ionic.eduka.models")
@EnableJpaRepositories(basePackages = "com.groupe2_ionic.eduka.repository")
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
