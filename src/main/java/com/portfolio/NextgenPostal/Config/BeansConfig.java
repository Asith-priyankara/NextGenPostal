package com.portfolio.NextgenPostal.Config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@AllArgsConstructor
public class BeansConfig {
    @Bean
    public Random random() {
        return new Random();
    }
}
