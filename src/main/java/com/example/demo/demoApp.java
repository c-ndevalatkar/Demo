package com.example.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class demoApp {

    @GetMapping("/")
    public String home() {
        return "Hello Nagesh! Welcome to Spring Boot.";
    }
}