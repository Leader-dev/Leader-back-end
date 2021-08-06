package com.leader.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;

@EnableScheduling
@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
public class ApiApplication {

	public static Date PROCESS_START_TIME = new Date();

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}
