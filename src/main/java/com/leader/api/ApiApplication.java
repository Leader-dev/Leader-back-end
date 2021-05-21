package com.leader.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class ApiApplication {

	public static Date PROCESS_START_TIME = new Date();

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}
