package com.silently9527.smartmvcspringbootdemo.config;

import com.silently9527.smartmvc.config.WebMvcConfigurer;
import com.silently9527.smartmvc.handler.argument.HandlerMethodArgumentResolver;
import com.silently9527.smartmvcspringbootdemo.spi.MyHandlerMethodArgumentResolver;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new MyHandlerMethodArgumentResolver());
    }
}
