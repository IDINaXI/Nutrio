package com.nutrio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nutrio")
public class NutrioApplication {

	public static void main(String[] args) {
		SpringApplication.run(NutrioApplication.class, args);
	}

}
