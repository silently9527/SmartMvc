package com.silently9527.smartmvc.config;

import com.silently9527.smartmvc.handler.adapter.HandlerAdapter;
import com.silently9527.smartmvc.handler.adapter.RequestMappingHandlerAdapter;
import com.silently9527.smartmvc.handler.argument.HandlerMethodArgumentResolver;
import com.silently9527.smartmvc.handler.exception.ExceptionHandlerExceptionResolver;
import com.silently9527.smartmvc.handler.exception.HandlerExceptionResolver;
import com.silently9527.smartmvc.handler.interceptor.InterceptorRegistry;
import com.silently9527.smartmvc.handler.interceptor.MappedInterceptor;
import com.silently9527.smartmvc.handler.mapping.HandlerMapping;
import com.silently9527.smartmvc.handler.mapping.RequestMappingHandlerMapping;
import com.silently9527.smartmvc.handler.returnvalue.HandlerMethodReturnValueHandler;
import com.silently9527.smartmvc.view.View;
import com.silently9527.smartmvc.view.resolver.ContentNegotiatingViewResolver;
import com.silently9527.smartmvc.view.resolver.InternalResourceViewResolver;
import com.silently9527.smartmvc.view.resolver.ViewResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebMvcConfigurationSupport implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    private List<MappedInterceptor> interceptors;
    @Nullable
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Nullable
    private List<HandlerMethodReturnValueHandler> returnValueHandlers;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Bean
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        addFormatters(conversionService);
        return conversionService;
    }

    //数据转换格式化暴露对外的扩展点
    protected void addFormatters(FormatterRegistry registry) {
    }

    protected List<MappedInterceptor> getInterceptors(FormattingConversionService mvcConversionService) {
        if (this.interceptors == null) {
            InterceptorRegistry registry = new InterceptorRegistry();
            addInterceptors(registry);
            this.interceptors = registry.getMappedInterceptors();
        }
        return this.interceptors;
    }

    //拦截器暴露对外的扩展点
    protected void addInterceptors(InterceptorRegistry registry) {

    }

    //初始化HandlerMapping
    @Bean
    public HandlerMapping handlerMapping(FormattingConversionService mvcConversionService) {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setInterceptors(getInterceptors(mvcConversionService));
        return handlerMapping;
    }

    //初始化HandlerAdapter
    @Bean
    public HandlerAdapter handlerAdapter(ConversionService conversionService) {
        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        handlerAdapter.setConversionService(conversionService);
        handlerAdapter.setCustomArgumentResolvers(getArgumentResolvers());
        handlerAdapter.setCustomReturnValueHandlers(getReturnValueHandlers());
        return handlerAdapter;
    }

    protected List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        if (this.returnValueHandlers == null) {
            this.returnValueHandlers = new ArrayList<>();
            addReturnValueHandlers(this.returnValueHandlers);
        }
        return this.returnValueHandlers;
    }

    //返回值解析器
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

    }

    protected List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        if (this.argumentResolvers == null) {
            this.argumentResolvers = new ArrayList<>();
            addArgumentResolvers(this.argumentResolvers);
        }
        return this.argumentResolvers;
    }

    //参数解析器的扩展点
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

    }

    @Bean
    public HandlerExceptionResolver handlerExceptionResolver(FormattingConversionService mvcConversionService) {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionResolver.setCustomArgumentResolvers(getArgumentResolvers());
        exceptionResolver.setCustomReturnValueHandlers(getReturnValueHandlers());
        exceptionResolver.setConversionService(mvcConversionService);
        return exceptionResolver;
    }

    @Bean
    public ViewResolver viewResolver() {
        ContentNegotiatingViewResolver negotiatingViewResolver = new ContentNegotiatingViewResolver();

        List<ViewResolver> viewResolvers = new ArrayList<>();
        addViewResolvers(viewResolvers);
        if (CollectionUtils.isEmpty(viewResolvers)) {
            negotiatingViewResolver.setViewResolvers(Collections.singletonList(new InternalResourceViewResolver()));
        } else {
            negotiatingViewResolver.setViewResolvers(viewResolvers);
        }

        List<View> views = new ArrayList<>();
        addDefaultViews(views);
        if (!CollectionUtils.isEmpty(views)) {
            negotiatingViewResolver.setDefaultViews(views);
        }

        return negotiatingViewResolver;
    }

    //视图的扩展点
    protected void addDefaultViews(List<View> views) {

    }

    //视图解析器的扩展点
    protected void addViewResolvers(List<ViewResolver> viewResolvers) {

    }

}
