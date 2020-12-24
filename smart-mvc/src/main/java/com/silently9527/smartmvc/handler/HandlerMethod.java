package com.silently9527.smartmvc.handler;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HandlerMethod {

    private Object bean;
    private Class<?> beanType;
    private Method method;

    private List<MethodParameter> parameters;

    public HandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.beanType = bean.getClass();
        this.method = method;

        this.parameters = new ArrayList<>();
        int parameterCount = method.getParameterCount();
        for (int index = 0; index < parameterCount; index++) {
            parameters.add(new MethodParameter(method, index));
        }
    }

    public HandlerMethod(HandlerMethod handlerMethod) {
        Assert.notNull(handlerMethod, "HandlerMethod is required");
        this.bean = handlerMethod.bean;
        this.beanType = handlerMethod.beanType;
        this.method = handlerMethod.method;
        this.parameters = handlerMethod.parameters;
    }


    public Object getBean() {
        return bean;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public Method getMethod() {
        return method;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }
}
