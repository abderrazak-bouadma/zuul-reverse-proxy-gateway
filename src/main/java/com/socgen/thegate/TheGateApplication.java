package com.socgen.thegate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class TheGateApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheGateApplication.class, args);
	}
}
