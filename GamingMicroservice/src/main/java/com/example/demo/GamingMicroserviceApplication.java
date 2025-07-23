package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@OpenAPIDefinition
@EnableCaching
public class GamingMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamingMicroserviceApplication.class, args);

		//TODO подружить все с кафкой.
	}

}
