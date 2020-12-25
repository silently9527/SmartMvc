从前面的单元测试我们已经发现，要想要使用SmartMVC框架，我们需要构建很多的对象，比如：`HandlerMapping`、`HandlerAdapter`、`HandlerInterceptor`等等；为了让我们的框架能够更加方便的使用，我们需要开发一个配置器`WebMvcConfigurationSupport`，能把大部分的配置都封装起来，把个别的扩展点暴露给框架的使用者，并且用户如果没有需求扩展，直接使用注解`@EnableWebMvc`就可以完成SmartMVC框架的配置工作，现在我们就开始来开发这个功能。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b5286fbb11cd4d919eba5b2d1b857df5~tplv-k3u1fbpfcp-watermark.image)


#### 开发步骤讲解

##### WebMvcConfigurationSupport
在这个配置类中，我们需要初始化出DispatchServlet所有需要使用到的组件，并且预留一些可供用户扩展的接口。

1. 构建数据转换器`FormattingConversionService`,预留给用户可以自定义转换格式的接口供子类覆写

```
@Bean
public FormattingConversionService mvcConversionService() {
    FormattingConversionService conversionService = new DefaultFormattingConversionService();
    addFormatters(conversionService);
    return conversionService;
}

//数据转换格式化暴露对外的扩展点
protected void addFormatters(FormatterRegistry registry) {
}
```

2. 提供给用户添加自定义拦截器的扩展点，默认系统不添加任何拦截器

```
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
```

3. 构建HandlerMapping
```
@Bean
public HandlerMapping handlerMapping(FormattingConversionService mvcConversionService) {
    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.setInterceptors(getInterceptors(mvcConversionService));
    return handlerMapping;
}
```

4. 构建HandlerAdapter，预留用户自定义参数解析器和返回值处理器，如果用户设置了就添加到`RequestMappingHandlerAdapter`

```
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
```

5. 构建全局异常处理器，同样需要设置自定义参数解析器和返回值处理器

```
@Bean
public HandlerExceptionResolver handlerExceptionResolver(FormattingConversionService mvcConversionService) {
    ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
    exceptionResolver.setCustomArgumentResolvers(getArgumentResolvers());
    exceptionResolver.setCustomReturnValueHandlers(getReturnValueHandlers());
    exceptionResolver.setConversionService(mvcConversionService);
    return exceptionResolver;
}
```

6. 构建内容协同器`ContentNegotiatingViewResolver`，默认添加的视图解析器是`InternalResourceViewResolver`

```
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
```

##### WebMvcConfigurer
按理说用户通过继承上面的配置类`WebMvcConfigurationSupport`，其实已经简化了很多的配置操作，但是这样还不够；一个框架提供的SPI接口对于扩展这来说应该尽量保持透明才好；尽量能够透明到让用户连这个配置类的存在都不知道，用户需要添加拦截器，视图解析器都到指定的接口中去添加，而不需要关心添加的内容具体如何生效的，为了完成这个功能，我们需要定义一个接口`WebMvcConfigurer`，提供给用户添加所有的扩展点方法。

```
public interface WebMvcConfigurer {
    //参数解析器的扩展点
    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    //返回值解析器
    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    //拦截器暴露对外的扩展点
    default void addInterceptors(InterceptorRegistry registry) {
    }

    //数据转换格式化暴露对外的扩展点
    default void addFormatters(FormatterRegistry registry) {
    }

    //视图的扩展点
    default void addDefaultViews(List<View> views) {
    }

    //视图解析器的扩展点
    default void addViewResolvers(List<ViewResolver> viewResolvers) {
    }
}
```

为了能够允许用户配置多个`WebMvcConfigurer`，所有和之前的参数解析器一样我们实现了一个聚合实现`WebMvcConfigurerComposite`

```
public class WebMvcConfigurerComposite implements WebMvcConfigurer {
    private List<WebMvcConfigurer> delegates = new ArrayList<>();

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        delegates.forEach(configurer -> configurer.addArgumentResolvers(argumentResolvers));
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        delegates.forEach(configurer -> configurer.addReturnValueHandlers(returnValueHandlers));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        delegates.forEach(configurer -> configurer.addInterceptors(registry));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        delegates.forEach(configurer -> configurer.addFormatters(registry));
    }

    @Override
    public void addDefaultViews(List<View> views) {
        delegates.forEach(configurer -> configurer.addDefaultViews(views));
    }

    @Override
    public void addViewResolvers(List<ViewResolver> viewResolvers) {
        delegates.forEach(configurer -> configurer.addViewResolvers(viewResolvers));
    }

    public WebMvcConfigurerComposite addWebMvcConfigurers(WebMvcConfigurer... webMvcConfigurers) {
        Collections.addAll(this.delegates, webMvcConfigurers);
        return this;
    }

    public WebMvcConfigurerComposite addWebMvcConfigurers(List<WebMvcConfigurer> configurers) {
        this.delegates.addAll(configurers);
        return this;
    }
}
```

##### DelegatingWebMvcConfiguration
为了把`WebMvcConfigurer`与`WebMvcConfigurationSupport`联系起来屏蔽掉实现的细节，只暴露扩展点给用户，我们需要实现`DelegatingWebMvcConfiguration`， 它从容器中拿出所有的WebMvcConfigurer,添加到WebMvcConfigurerComposite里面，在DelegatingWebMvcConfiguration中调用`WebMvcConfigurerComposite`完成扩展点的载入

```
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
    private WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();

   
    @Autowired(required = false)
    public void setConfigurers(List<WebMvcConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addWebMvcConfigurers(configurers);
        }
    }

    @Override
    protected void addFormatters(FormatterRegistry registry) {
        configurers.addFormatters(registry);
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        configurers.addInterceptors(registry);
    }

    @Override
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        configurers.addReturnValueHandlers(returnValueHandlers);
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        configurers.addArgumentResolvers(argumentResolvers);
    }

    @Override
    protected void addDefaultViews(List<View> views) {
        configurers.addDefaultViews(views);
    }

    @Override
    protected void addViewResolvers(List<ViewResolver> viewResolvers) {
        configurers.addViewResolvers(viewResolvers);
    }
}

```

##### EnableWebMvc
所有的配置都已完成，现在我们还差一个注解`EnableWebMvc`来驱动整个配置生效，在注解上`@Import(DelegatingWebMvcConfiguration.class)`

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

#### 单元测试
为了测试我们的配置类是否正确，我们修改AppConfig.java的代码如下：

```
@Configuration
@EnableWebMvc    //添加注解
@ComponentScan(basePackages = "com.silently9527.smartmvc")
public class AppConfig {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }
}
```

直接运行之前我们开发好的`DispatcherServletTest`中的两个单元测试方法，执行结果如下：

![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bdaf2c983b874466a62166dd506346b9~tplv-k3u1fbpfcp-watermark.image)


#### 总结
本篇实现了SmartMVC通过注解`@EnableWebMvc`驱动整个框架的配置，这个也是SpringMVC的实现方式，同时也展示了SPI的设计思路


#### 延展
可以对比查看SpringMVC中提供的`WebMvcConfigurationSupport`，熟悉提供了哪些扩展点，工作中可以使用