package com.silently9527.smartmvc.configurure;

import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.UpgradeProtocol;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnClass(ServletRequest.class)
@EnableConfigurationProperties(ServerProperties.class)
public class ServletWebServerFactoryAutoConfiguration {

    @Bean
    @ConditionalOnClass({Servlet.class, Tomcat.class, UpgradeProtocol.class})
    @ConditionalOnMissingBean(value = ServletWebServerFactory.class)
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(
            ServerProperties serverProperties,
            ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers,
            ObjectProvider<TomcatContextCustomizer> contextCustomizers,
            ObjectProvider<TomcatProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.getTomcatConnectorCustomizers()
                .addAll(connectorCustomizers.orderedStream().collect(Collectors.toList()));
        factory.getTomcatContextCustomizers()
                .addAll(contextCustomizers.orderedStream().collect(Collectors.toList()));
        factory.getTomcatProtocolHandlerCustomizers()
                .addAll(protocolHandlerCustomizers.orderedStream().collect(Collectors.toList()));

        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(serverProperties::getPort).to(factory::setPort);
        map.from(serverProperties::getAddress).to(factory::setAddress);
        map.from(serverProperties.getServlet()::getContextPath).to(factory::setContextPath);
        map.from(serverProperties.getServlet()::getApplicationDisplayName).to(factory::setDisplayName);
        map.from(serverProperties.getServlet()::isRegisterDefaultServlet).to(factory::setRegisterDefaultServlet);
        map.from(serverProperties.getServlet()::getSession).to(factory::setSession);
        map.from(serverProperties::getSsl).to(factory::setSsl);
        map.from(serverProperties.getServlet()::getJsp).to(factory::setJsp);
        map.from(serverProperties.getShutdown()).to(factory::setShutdown);

        return factory;
    }

}
