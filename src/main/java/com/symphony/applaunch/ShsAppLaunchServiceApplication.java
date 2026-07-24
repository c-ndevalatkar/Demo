package com.symphony.applaunch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class ShsAppLaunchServiceApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ShsAppLaunchServiceApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ShsAppLaunchServiceApplication.class, args);
	}
}
