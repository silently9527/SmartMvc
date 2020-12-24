package com.silently9527.smartmvc.configurure.context;

import com.silently9527.smartmvc.support.GenericWebApplicationContext;
import com.silently9527.smartmvc.support.WebApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContextException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;

public class ServletWebServerApplicationContext extends GenericWebApplicationContext implements WebServerApplicationContext {

    private WebServer webServer;

    public ServletWebServerApplicationContext() {
    }

    @Override
    public WebServer getWebServer() {
        return this.webServer;
    }

    @Override
    public final void refresh() throws BeansException, IllegalStateException {
        try {
            super.refresh();
        } catch (RuntimeException ex) {
            WebServer webServer = this.webServer;
            if (webServer != null) {
                webServer.stop();
            }
            throw ex;
        }
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        try {
            this.webServer = createWebServer();
            this.webServer.start();
        } catch (Throwable ex) {
            throw new ApplicationContextException("Unable to start web server", ex);
        }
    }

    private WebServer createWebServer() {
        ServletWebServerFactory factory = getBeanFactory().getBean(ServletWebServerFactory.class);
        return factory.getWebServer(this::selfInitialize);
    }

    private void selfInitialize(ServletContext servletContext) throws ServletException {
        prepareWebApplicationContext(servletContext);
        Map<String, ServletContextInitializer> beanMaps = getBeanFactory().getBeansOfType(ServletContextInitializer.class);
        for (ServletContextInitializer bean : beanMaps.values()) {
            bean.onStartup(servletContext);
        }
    }

    private void prepareWebApplicationContext(ServletContext servletContext) {
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);
        setServletContext(servletContext);
    }


}
