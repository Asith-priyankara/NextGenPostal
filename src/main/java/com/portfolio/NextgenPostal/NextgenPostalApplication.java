package com.portfolio.NextgenPostal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NextgenPostalApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextgenPostalApplication.class, args);
	}

}
