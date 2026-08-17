package com.isalvama.fresh_keep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FreshKeepApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreshKeepApplication.class, args);
	}

}
