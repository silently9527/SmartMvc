package com.silently9527.smartmvc.configurure.servlet;

import com.silently9527.smartmvc.DispatcherServlet;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.util.Assert;

public class SmartMvcDispatcherServletRegistrationBean extends ServletRegistrationBean<DispatcherServlet>
        implements DispatcherServletPath {

    private final String path;

    public SmartMvcDispatcherServletRegistrationBean(DispatcherServlet servlet, String path) {
        super(servlet);
        Assert.notNull(path, "Path must not be null");
        this.path = path;
        super.addUrlMappings(getServletUrlMapping());
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
