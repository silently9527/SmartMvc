前面两篇我们开发完成了参数的解析器和返回值的处理器，本篇我们将开始开发`InvocableHandlerMethod`，
`InvocableHandlerMethod`是对`HandlerMethod`的扩展，
基于一组`HandlerMethodArgumentResolver`从请求上下文中解析出控制器方法的参数值，然后调用控制器方法。

`InvocableHandlerMethod` 与 `HandlerMethod` 有区别呢？
- `HandlerMethod` 在容器在启动过程中搜集控制器的方法，用于定义每个控制器方法
- `InvocableHandlerMethod` 用于处理用户的请求调用控制器方法，包装处理所需的各种参数和执行处理逻辑

![UML](https://image-static.segmentfault.com/454/993/454993797-5fce2d5be2fd8_articlex)



#### 开发步骤讲解


##### InvocableHandlerMethod

```
public class InvocableHandlerMethod extends HandlerMethod {
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private HandlerMethodArgumentResolverComposite argumentResolver;
    private HandlerMethodReturnValueHandlerComposite returnValueHandler;
    private ConversionService conversionService;

    public InvocableHandlerMethod(HandlerMethod handlerMethod,
                                  HandlerMethodArgumentResolverComposite argumentResolver,
                                  HandlerMethodReturnValueHandlerComposite returnValueHandler,
                                  ConversionService conversionService) {
        super(handlerMethod);
        this.argumentResolver = argumentResolver;
        this.returnValueHandler = returnValueHandler;
        this.conversionService = conversionService;
    }

    /**
     * 调用handler
     *
     * @param request
     * @param mavContainer
     * @throws Exception
     */
    public void invokeAndHandle(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelAndViewContainer mavContainer) throws Exception {

        List<Object> args = this.getMethodArgumentValues(request, response, mavContainer);
        Object resultValue = doInvoke(args);
        //返回为空
        if (Objects.isNull(resultValue)) {
            if (response.isCommitted()) {
                mavContainer.setRequestHandled(true);
                return;
            } else {
                throw new IllegalStateException("Controller handler return value is null");
            }
        }

        mavContainer.setRequestHandled(false);
        Assert.state(this.returnValueHandler != null, "No return value handler");

        MethodParameter returnType = new MethodParameter(this.getMethod(), -1);  //-1表示方法的返回值
        this.returnValueHandler.handleReturnValue(resultValue, returnType, mavContainer, request, response);

    }

    private Object doInvoke(List<Object> args) throws InvocationTargetException, IllegalAccessException {
        return this.getMethod().invoke(this.getBean(), args.toArray());
    }

    private List<Object> getMethodArgumentValues(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 ModelAndViewContainer mavContainer) throws Exception {
        Assert.notNull(argumentResolver, "HandlerMethodArgumentResolver can not null");

        List<MethodParameter> parameters = this.getParameters();
        List<Object> args = new ArrayList<>(parameters.size());
        for (MethodParameter parameter : parameters) {
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args.add(argumentResolver.resolveArgument(parameter, request, response, mavContainer, conversionService));
        }
        return args;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }
}
```

1. `InvocableHandlerMethod`需要继承`HandlerMethod`，因为调用控制器的方法需要知道实例以及调用那个方法，
并且在开篇也讲到它是`HandlerMethod`的扩展。
2. `ParameterNameDiscoverer`在前面的章节中我们已经见过了，主要是用来查找方法名的，
这里我们直接初始化了一个默认的实现`DefaultParameterNameDiscoverer`
3. 由于本身`InvocableHandlerMethod`是实现调用控制器方法的，所以包含了两个对象参数的解析器`HandlerMethodArgumentResolverComposite`和返回值的处理器`HandlerMethodReturnValueHandlerComposite`；因为在参数解析器中会用到数据的转换，所以又定义了一个`ConversionService`
4. 在调用方法之前我们需要先获取到方法的参数

```
private List<Object> getMethodArgumentValues(HttpServletRequest request,
                                             HttpServletResponse response,
                                             ModelAndViewContainer mavContainer) throws Exception {
    Assert.notNull(argumentResolver, "HandlerMethodArgumentResolver can not null");

    List<MethodParameter> parameters = this.getParameters();
    List<Object> args = new ArrayList<>(parameters.size());
    for (MethodParameter parameter : parameters) {
        parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
        args.add(argumentResolver.resolveArgument(parameter, request, response, mavContainer, conversionService));
    }
    return args;
}
```
遍历方法所有的参数，处理每个参数之前需要先调用`initParameterNameDiscovery`，然后在通过参数解析器去找到想要的参数

5. 解析完所有的参数后，通过反射调用控制器中的方法
6. 执行完成后判断返回值是否为空，如果为空需要判断当前的response是否已经提交（有可能用户直接在控制的方法中使用response输出内容到前端），已提交标记本次请求已经处理完成` mavContainer.setRequestHandled(true);`
7. 如果返回值不为空，条件返回值处理器


#### 单元测试
本节开发内容较为简单，到此已经开发完成，接下来单元测试一下是否能够正常的调用控制器的方法

创建控制器`TestInvocableHandlerMethodController`

```
public class TestInvocableHandlerMethodController {

    public void testRequestAndResponse(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request can not null");
        Assert.notNull(response, "response can not null");
        try (PrintWriter writer = response.getWriter()) {
            String name = request.getParameter("name");
            writer.println("Hello InvocableHandlerMethod, params:" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String testViewName(Model model) {
        model.addAttribute("blogURL", "http://silently9527.cn");
        return "/silently9527.jsp";
    }

}

```
- testRequestAndResponse: 两个参数`HttpServletRequest`、`HttpServletResponse`能正常注入；通过`response`能正常输出内容给前端
- testViewName: 能正常注入`Model`类型的参数；执行完成之后能够在`ModelAndViewContainer`中拿到视图名称和`Model`中的数据


1. 创建单元测试，先测试testRequestAndResponse

```
@Test
public void test1() throws Exception {
    TestInvocableHandlerMethodController controller = new TestInvocableHandlerMethodController();

    Method method = controller.getClass().getMethod("testRequestAndResponse",
            HttpServletRequest.class, HttpServletResponse.class);

    //初始化handlerMethod、HandlerMethodArgumentResolverComposite
    HandlerMethod handlerMethod = new HandlerMethod(controller, method);
    HandlerMethodArgumentResolverComposite argumentResolver = new HandlerMethodArgumentResolverComposite();
    argumentResolver.addResolver(new ServletRequestMethodArgumentResolver());
    argumentResolver.addResolver(new ServletResponseMethodArgumentResolver());

    //本测试用例中使用不到返回值处理器和转换器，所以传入null
    InvocableHandlerMethod inMethod = new InvocableHandlerMethod(handlerMethod, argumentResolver, null, null);

    ModelAndViewContainer mvContainer = new ModelAndViewContainer();
    
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("name", "Silently9527"); //设置参数name
    
    MockHttpServletResponse response = new MockHttpServletResponse();

    //开始调用控制器的方法testRequestAndResponse
    inMethod.invokeAndHandle(request, response, mvContainer);

    System.out.println("输出到前端的内容:");
    System.out.println(response.getContentAsString());
}
```

该单元测试的结果如下：

![result1](https://image-static.segmentfault.com/250/316/250316949-5fce36f464910_articlex)

2. 测试控制器的方法testViewName

```
@Test
public void test2() throws Exception {
    TestInvocableHandlerMethodController controller = new TestInvocableHandlerMethodController();
    Method method = controller.getClass().getMethod("testViewName", Model.class);

    //初始化handlerMethod、HandlerMethodArgumentResolverComposite
    HandlerMethod handlerMethod = new HandlerMethod(controller, method);
    HandlerMethodArgumentResolverComposite argumentResolver = new HandlerMethodArgumentResolverComposite();
    argumentResolver.addResolver(new ModelMethodArgumentResolver());

    //由于testViewName的方法有返回值，所以需要设置返回值处理器
    HandlerMethodReturnValueHandlerComposite returnValueHandler = new HandlerMethodReturnValueHandlerComposite();
    returnValueHandler.addReturnValueHandler(new ViewNameMethodReturnValueHandler());
    
    InvocableHandlerMethod inMethod = new InvocableHandlerMethod(handlerMethod, argumentResolver, returnValueHandler, null);

    ModelAndViewContainer mvContainer = new ModelAndViewContainer();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    //执行调用
    inMethod.invokeAndHandle(request, response, mvContainer);

    System.out.println("ModelAndViewContainer:");
    System.out.println(JSON.toJSONString(mvContainer.getModel()));
    System.out.println("viewName: " + mvContainer.getViewName());
}
```

解析的结果如下：

![result2](https://image-static.segmentfault.com/421/976/4219761408-5fce386947159_articlex)


#### 总结
本篇的开发内容比较简单，主要是把前两篇开发的参数解析器和返回值处理器给串联起来完成方法的调用；
下一篇我们将开始完成`HandlerAdapter`这个组件的最后研发

#### 延展
开发完成本节的任务后，大家可以对照着去看看SpringMVC中的`ServletInvocableHandlerMethod`、`InvocableHandlerMethod`，
比我们的实现多了哪些功能