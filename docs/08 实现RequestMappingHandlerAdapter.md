本篇我们来完成`HandlerAdapter`的实现类`RequestMappingHandlerAdapter`，这也是`HandlerAdpater`的最后一节。先看看类图

![UML](https://image-static.segmentfault.com/118/198/1181981758-5fcf8588e5f60_articlex)

`RequestMappingHandlerAdapter`本身在SpringMVC中占有重要的地位，虽然它只是`HandlerAdapter`的一种实现，
但是它是使用最多的一个实现类，主要用于将某个请求适配给`@RequestMapping`类型的Handler处理


#### 开发步骤讲解

##### HandlerAdapter

```
public interface HandlerAdapter {
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
                        HandlerMethod handler) throws Exception;
}
```

该接口我们只定义了一个handle方法，但是在SpringMVC中还有一个`supports`方法，刚才我们也说过在SpringMVC中`HandlerAdapter`有多个实现，
这也是一个策略模式，所以需要这一个`supports`方法；在我们开发的SmartMVC中只打算做一个实现，所以只要一个handle方法就足够了。

我们可以看到返回值是一个`ModelAndView`对象，表示执行handle方法之后需要把控制器中的方法返回值需要封装成`ModelAndView`对象；
所以这里可以看出在HandlerAdapter中会使用到我们之前开发过的返回值处理。接下来我们看下`ModeAndView`的定义

##### ModelAndView

```
public class ModelAndView {
    private Object view;
    private Model model;
    private HttpStatus status;

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    //省略getter setter
}

```

##### RequestMappingHandlerAdapter

```
public class RequestMappingHandlerAdapter implements HandlerAdapter, InitializingBean {

    private List<HandlerMethodArgumentResolver> customArgumentResolvers;
    private HandlerMethodArgumentResolverComposite argumentResolverComposite;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;
    private HandlerMethodReturnValueHandlerComposite returnValueHandlerComposite;

    private ConversionService conversionService;

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
                               HandlerMethod handlerMethod) throws Exception {

        InvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();

        invocableMethod.invokeAndHandle(request, response, mavContainer);

        return getModelAndView(mavContainer);
    }

    private ModelAndView getModelAndView(ModelAndViewContainer mavContainer) {
        if (mavContainer.isRequestHandled()) {
            //本次请求已经处理完成
            return null;
        }

        ModelAndView mav = new ModelAndView();
        mav.setStatus(mavContainer.getStatus());
        mav.setModel(mavContainer.getModel());
        mav.setView(mavContainer.getView());
        return mav;
    }

    private InvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new InvocableHandlerMethod(handlerMethod,
                this.argumentResolverComposite,
                this.returnValueHandlerComposite,
                this.conversionService);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(conversionService, "conversionService can not null");
        if (Objects.isNull(argumentResolverComposite)) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolverComposite = new HandlerMethodArgumentResolverComposite();
            this.argumentResolverComposite.addResolver(resolvers);
        }

        if (Objects.isNull(returnValueHandlerComposite)) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlerComposite = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlerComposite.addReturnValueHandler(handlers);
        }
    }

    /**
     * 初始化默认返回值处理器
     *
     * @return
     */
    private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        handlers.add(new MapMethodReturnValueHandler());
        handlers.add(new ModelMethodReturnValueHandler());
        handlers.add(new ResponseBodyMethodReturnValueHandler());
        handlers.add(new ViewNameMethodReturnValueHandler());
        handlers.add(new ViewMethodReturnValueHandler());

        if (!CollectionUtils.isEmpty(getCustomReturnValueHandlers())) {
            handlers.addAll(getDefaultReturnValueHandlers());
        }

        return handlers;
    }

    /**
     * 初始化默认参数解析器
     *
     * @return
     */
    private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        resolvers.add(new ModelMethodArgumentResolver());
        resolvers.add(new RequestParamMethodArgumentResolver());
        resolvers.add(new RequestBodyMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());
        resolvers.add(new ServletRequestMethodArgumentResolver());

        if (!CollectionUtils.isEmpty(getCustomArgumentResolvers())) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        return resolvers;
    }

    //省略getter setter
}

```

1. 考虑到框架的扩展性，所以这里定义了`customArgumentResolvers`、`customReturnValueHandlers`两个变量，如果SmartMVC提供的参数解析器和返回值处理器不满足用户的需求，允许添加自定义的参数解析器和返回值处理器
2. 在`RequestMappingHandlerAdapter`加入到spring容器之后需要做一些初始化的工作，所以实现了接口`InitializingBean`，在`afterPropertiesSet`方法中我们需要把系统默认支持的参数解析器和返回值处理器以及用户自定义的一起添加到系统中。
3. 当`DispatcherServlet`处理用户请求的时候会调用`HandlerAdapter`的handle方法，这时候先通过传入`HandlerMethod`创建之前我们已经开发完成的组件`InvocableHandlerMethod`，然后调用`invokeAndHandle`执行控制器的方法
4. 当执行完成控制器的方法，我们需要通过`ModelAndViewContainer`创建`ModelAndView`对象返回


#### 单元测试
本篇的单元测试目标就是能够通过`RequestMappingHandlerAdapter`能成功的调用到控制器中的方法并且正确返回；本次单元测试就使用上一篇的控制器类`TestInvocableHandlerMethodController`中的方法`testViewName`

```
public String testViewName(Model model) {
    model.addAttribute("blogURL", "http://silently9527.cn");
    return "/silently9527.jsp";
}
```

测试能够正则的调用到方法`testViewName`，并且返回的`ModelAndView`中包含model包含设置的属性`blogURL`，view的值是`/silently9527.jsp`

单元测试如下：

```
@Test
public void handle() throws Exception {
    TestInvocableHandlerMethodController controller = new TestInvocableHandlerMethodController();

    Method method = controller.getClass().getMethod("testViewName", Model.class);
    HandlerMethod handlerMethod = new HandlerMethod(controller, method);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
    DateFormatter dateFormatter = new DateFormatter();
    dateFormatter.setPattern("yyyy-MM-dd HH:mm:ss");
    conversionService.addFormatter(dateFormatter);

    RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
    handlerAdapter.setConversionService(conversionService);
    handlerAdapter.afterPropertiesSet();

    ModelAndView modelAndView = handlerAdapter.handle(request, response, handlerMethod);

    System.out.println("modelAndView:");
    System.out.println(JSON.toJSONString(modelAndView));
}

```

输出的结果如下：

![result](https://image-static.segmentfault.com/101/623/1016234010-5fcf8c48aea95_articlex)

#### 总结
本节通过开发`RequestMappingHandlerAdapter`，把我们之前开发的多个组件都组合起来了，并且能够正确的工作。

#### 延展
SpringMVC中`HandlerAdapter`有多个实现类，都有不同的使用方式，而`RequestMappingHandlerAdapter`是使用最多的一个，
有兴趣的同学可以看看其他的实现类