package com.silently9527.smartmvcspringbootdemo;

import com.silently9527.smartmvc.configurure.context.ServletWebServerApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartmvcSpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SmartmvcSpringbootDemoApplication.class);
        application.setApplicationContextClass(ServletWebServerApplicationContext.class);
        application.run(args);
    }




}
