package com.isalvama.fresh_keep;

import org.springframework.boot.SpringApplication;

public class TestFreshKeepApplication {

	public static void main(String[] args) {
		SpringApplication.from(FreshKeepApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
