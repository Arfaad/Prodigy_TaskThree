package com.example.taskone;

import com.example.taskone.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskoneApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(TaskoneApplication.class, args);
	}

}
