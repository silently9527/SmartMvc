
开发smartmvc-springboot-starter的准备工作都已完成，接下来我们将开始来完成starter的开发工作。开发之前先聊聊注意点：

1. 命名规则：SpringBoot提供的starter以spring-boot-starter-xxx的方式命名的。官方建议第三方自定义的starter使用xxx-spring-boot-starter命名规则。
以区分SpringBoot生态提供的starter。

2. 通常建议建立两个项目，比如我们将会建立`smartmvc-springboot-autoconfigure`、`smartmvc-springboot-starter`
    - `smartmvc-springboot-autoconfigure` 完成所有自动化配置的代码
    - `smartmvc-springboot-starter` 其实是一个空的项目，只需要在pom.xml中加入依赖的项目
为什么有这样的建议？就拿我们将要开发的starter来举例，我们的SmartMvc可以运行在不同的Web服务器中，比如：tomcat,jetty；
这些web服务器的自动化配置都可以存放在`smartmvc-springboot-autoconfigure`中；在实际项目的使用过程中，我们不可能既使用
Tomcat,有使用jetty；所以我们需要一个`smartmvc-springboot-starter`项目来引入Tomcat的maven，或者是Jetty的；这样即达到了
所有自动化配置的统一管理，又可以在实际的项目使用过程中灵活的选择

### 开发步骤

#### `smartmvc-springboot-autoconfigure`

##### 1. 在pom.xml中加入依赖
```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure-processor</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <!-- 以上三个是starter项目都需要添加的 -->
    
    <dependency>
        <groupId>com.silently9527</groupId>
        <artifactId>smart-mvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <optional>true</optional>    <!-- 很重要：只需要在编译时生效，具体是否需要使用Tomcat需要在starter项目中引入 -->
    </dependency>
</dependencies>
```

##### 2. 创建`TomcatServletWebServerFactory`
我们这里选择使用的Web服务器是Tomcat

```
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
```

##### 2. 创建DispatcherServlet、SmartMvcDispatcherServletRegistrationBean

```
@Configuration
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(WebMvcProperties.class) //开启配置类WebMvcProperties
public class SmartMvcDispatcherServletAutoConfiguration {
    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "smartMvcDispatcherServlet";

    @Bean
    @ConditionalOnMissingBean(value = DispatcherServlet.class)
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    @ConditionalOnBean(value = DispatcherServlet.class)
    public SmartMvcDispatcherServletRegistrationBean dispatcherServletRegistration(
            DispatcherServlet dispatcherServlet, WebMvcProperties webMvcProperties) {
        SmartMvcDispatcherServletRegistrationBean registration = new SmartMvcDispatcherServletRegistrationBean(dispatcherServlet,
                webMvcProperties.getServlet().getPath()); //通过webMvcProperties配置Servlet的拦截路径
        registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
        registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        return registration;
    }
}
```

##### 3. SmartMvc自动化配置的入口类 WebMvcAutoConfiguration

```
@Configuration
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
//导入刚才创建的两个配置类
@Import({SmartMvcDispatcherServletAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class})
public class WebMvcAutoConfiguration {

    @EnableWebMvc //使用SmartMVC的默认配置，不在starter中自定义任何组件
    @EnableConfigurationProperties({WebMvcProperties.class})
    public static class EnableWebMvcAutoConfiguration {
    }
}
```

##### 4. 添加spring.factories

在 `src/main/resource/META-INF` 目录下创建一个配置文件 `spring.factories`，配置文件内容见下文。这个文件很重要，
spring-core中的SpringFactoriesLoader通过检索这个文件中的内容，获取到指定的配置类。

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.silently9527.smartmvc.configurure.WebMvcAutoConfiguration
```


#### `smartmvc-springboot-starter`

只需要在pom.xml中加入一下依赖：

```
<dependencies>
    <dependency>
        <groupId>com.silently9527</groupId>
        <artifactId>smartmvc-springboot-autoconfigure</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId> 
    </dependency>
</dependencies>
```

因为我们需要使用的是Tomcat，所以只需要加入Tomcat的依赖；假如我们以后需要支持Jetty服务器，那么我们可以在建一个starter项目
把依赖修改成Jetty，这样就让我们的配置很灵活，用户可以自由选择