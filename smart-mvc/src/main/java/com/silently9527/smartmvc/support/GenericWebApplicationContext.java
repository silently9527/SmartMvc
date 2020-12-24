package com.silently9527.smartmvc.support;

import org.springframework.context.support.GenericApplicationContext;

import javax.servlet.ServletContext;

public class GenericWebApplicationContext extends GenericApplicationContext
        implements ConfigurableWebApplicationContext {

    private ServletContext servletContext;

    public GenericWebApplicationContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public GenericWebApplicationContext() {
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

}
