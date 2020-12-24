package com.silently9527.smartmvc.config;

import com.silently9527.smartmvc.DispatcherServlet;
import com.silently9527.smartmvc.annotation.EnableWebMvc;
import com.silently9527.smartmvc.handler.adapter.HandlerAdapter;
import com.silently9527.smartmvc.handler.adapter.RequestMappingHandlerAdapter;
import com.silently9527.smartmvc.handler.exception.ExceptionHandlerExceptionResolver;
import com.silently9527.smartmvc.handler.exception.HandlerExceptionResolver;
import com.silently9527.smartmvc.handler.interceptor.InterceptorRegistry;
import com.silently9527.smartmvc.handler.mapping.HandlerMapping;
import com.silently9527.smartmvc.handler.mapping.RequestMappingHandlerMapping;
import com.silently9527.smartmvc.intercepter.Test2HandlerInterceptor;
import com.silently9527.smartmvc.intercepter.TestHandlerInterceptor;
import com.silently9527.smartmvc.view.resolver.ContentNegotiatingViewResolver;
import com.silently9527.smartmvc.view.resolver.InternalResourceViewResolver;
import com.silently9527.smartmvc.view.resolver.ViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.util.Collections;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.silently9527.smartmvc")
public class AppConfig {

//    @Bean
//    public RequestMappingHandlerMapping handlerMapping() {
//        InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
//
//        TestHandlerInterceptor interceptor = new TestHandlerInterceptor();
//        interceptorRegistry.addInterceptor(interceptor)
//                .addExcludePatterns("/ex_test")
//                .addIncludePatterns("/in_test");
//
//        Test2HandlerInterceptor interceptor2 = new Test2HandlerInterceptor();
//        interceptorRegistry.addInterceptor(interceptor2)
//                .addIncludePatterns("/in_test2", "/in_test3");
//
//        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
//        mapping.setInterceptors(interceptorRegistry.getMappedInterceptors());
//        return mapping;
//    }

//    @Bean
//    public HandlerMapping handlerMapping() {
//        return new RequestMappingHandlerMapping();
//    }
//
//    @Bean
//    public HandlerAdapter handlerAdapter(ConversionService conversionService) {
//        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
//        handlerAdapter.setConversionService(conversionService);
//        return handlerAdapter;
//    }
//
//    @Bean
//    public ConversionService conversionService() {
//        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
//        DateFormatter dateFormatter = new DateFormatter();
//        dateFormatter.setPattern("yyyy-MM-dd HH:mm:ss");
//        conversionService.addFormatter(dateFormatter);
//        return conversionService;
//    }
//
//    @Bean
//    public ViewResolver viewResolver() {
//        ContentNegotiatingViewResolver negotiatingViewResolver = new ContentNegotiatingViewResolver();
//        negotiatingViewResolver.setViewResolvers(Collections.singletonList(new InternalResourceViewResolver()));
//        return negotiatingViewResolver;
//    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

//    @Bean
//    public HandlerExceptionResolver handlerExceptionResolver(ConversionService conversionService) {
//        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
//        resolver.setConversionService(conversionService);
//        return resolver;
//    }

}
