上一篇由于篇幅问题，在DispatcherServlet中还留了一个方法未实现，主要是处理出现异常情况该如何处理，
本篇我们将来完成这个功能，本篇内容稍多，我们先来看看类图：

![uml](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0a8d9afddb9d498abb22f55cb123c4ed~tplv-k3u1fbpfcp-watermark.image)

本篇我们主要需要实现框架的全局异常处理器，通过注解`ControllerAdvice`标注的类表示支持处理异常，
在这个类中通过注解`ExceptionHandler`标识出支持处理哪些异常。

#### 开发步骤讲解

##### ControllerAdvice、ExceptionHandler
先定义出我们需要使用的注解：

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ControllerAdvice {
}
```

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {

    Class<? extends Throwable>[] value() default {};

}
```
允许指定多个异常类，表示被标注的方法能处理指定的异常

#### ControllerAdviceBean

```
public class ControllerAdviceBean {
    private String beanName;
    private Class<?> beanType;
    private Object bean;

    public ControllerAdviceBean(String beanName, Object bean) {
        Assert.notNull(bean, "Bean must not be null");
        this.beanType = bean.getClass();
        this.beanName = beanName;
        this.bean = bean;
    }


    public static List<ControllerAdviceBean> findAnnotatedBeans(ApplicationContext context) {
        Map<String, Object> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, Object.class);
        return beanMap.entrySet().stream()
                .filter(entry -> hasControllerAdvice(entry.getValue()))
                .map(entry -> new ControllerAdviceBean(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    private static boolean hasControllerAdvice(Object bean) {
        Class<?> beanType = bean.getClass();
        return (AnnotatedElementUtils.hasAnnotation(beanType, ControllerAdvice.class));
    }
    //省略getter  setter
}
```
1. 该类用于表示被`ControllerAdvice`标注的类，比如`TestController`被标注了`ControllerAdvice`，那么就需要构建一个ControllerAdviceBean对象，beanType为`TestController`;bean就是`TestController`的实例对象
2. hasControllerAdvice: 判断类上是否有注解`ControllerAdvice`，在开发handlerMapping的初始化是也有类似的操作
3. findAnnotatedBeans: 从容器中找出被`ControllerAdvice`标注的所有类，构建一个`ControllerAdviceBean`集合返回

- ExceptionHandlerMethodResolver
当找出了所有被`ControllerAdvice`标注的类之后，我们还需要解析出这些类中哪些方法被注解`ExceptionHandler`标注过，`ExceptionHandlerMethodResolver`就是来做这个事的。

```
public class ExceptionHandlerMethodResolver {
    public static final ReflectionUtils.MethodFilter EXCEPTION_HANDLER_METHODS = method ->
            AnnotatedElementUtils.hasAnnotation(method, ExceptionHandler.class);

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new ConcurrentReferenceHashMap<>(16);

    public ExceptionHandlerMethodResolver(Class<?> handlerType) {
        for (Method method : MethodIntrospector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS)) {
            for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
                this.mappedMethods.put(exceptionType, method);
            }
        }
    }

    private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
        ExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);
        Assert.state(ann != null, "No ExceptionHandler annotation");
        return Arrays.asList(ann.value());
    }

    public Map<Class<? extends Throwable>, Method> getMappedMethods() {
        return mappedMethods;
    }

    public boolean hasExceptionMappings() {
        return !this.mappedMethods.isEmpty();
    }

    public Method resolveMethod(Exception exception) {
        Method method = resolveMethodByExceptionType(exception.getClass());
        if (method == null) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                method = resolveMethodByExceptionType(cause.getClass());
            }
        }
        return method;
    }

    private Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionClass) {
        return mappedMethods.get(exceptionClass);
    }
}
```

1. 首先定义了`EXCEPTION_HANDLER_METHODS`静态变量，判断方法是否有注解`ExceptionHandler`
2. detectExceptionMappings: 解析出方法上`ExceptionHandler`配置的所有异常
3. 构造方法中传入了Bean的类型，使用`MethodIntrospector.selectMethods`过滤出所有被`ExceptionHandler`标注的类（在HanderMapping的初始化也使用过同样的方法），保存异常类型和方法的对应关系
4. resolveMethod: 通过异常类型找出对应的方法

#### ExceptionHandlerExceptionResolver

```
public class ExceptionHandlerExceptionResolver implements HandlerExceptionResolver, ApplicationContextAware, InitializingBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache =
            new LinkedHashMap<>();
    private ConversionService conversionService;
    private List<HandlerMethodArgumentResolver> customArgumentResolvers;
    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        InvocableHandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod(ex);
        if (exceptionHandlerMethod == null) {
            return null;
        }

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();

        try {
            Throwable cause = ex.getCause();
            if (Objects.nonNull(cause)) {
                exceptionHandlerMethod.invokeAndHandle(request, response, mavContainer, cause);
            } else {
                exceptionHandlerMethod.invokeAndHandle(request, response, mavContainer, ex);
            }
        } catch (Exception e) {
            logger.error("exceptionHandlerMethod.invokeAndHandle fail", e);
            return null;
        }

        if (mavContainer.isRequestHandled()) {
            return null;
        }

        ModelAndView mav = new ModelAndView();
        mav.setStatus(mavContainer.getStatus());
        mav.setModel(mavContainer.getModel());
        mav.setView(mavContainer.getView());
        return mav;
    }

    private InvocableHandlerMethod getExceptionHandlerMethod(Exception exception) {
        for (Map.Entry<ControllerAdviceBean, ExceptionHandlerMethodResolver> entry : this.exceptionHandlerAdviceCache.entrySet()) {
            ControllerAdviceBean advice = entry.getKey();
            ExceptionHandlerMethodResolver resolver = entry.getValue();
            Method method = resolver.resolveMethod(exception);
            if (method != null) {
                return new InvocableHandlerMethod(advice.getBean(),
                        method,
                        this.argumentResolvers,
                        this.returnValueHandlers,
                        this.conversionService);
            }
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.conversionService, "conversionService can not null");
        initExceptionHandlerAdviceCache();
        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolver(resolvers);
        }
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addReturnValueHandler(handlers);
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

    private void initExceptionHandlerAdviceCache() {
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(applicationContext);
        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }
            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(beanType);
            if (resolver.hasExceptionMappings()) {
                this.exceptionHandlerAdviceCache.put(adviceBean, resolver);
            }
        }
    }
    //省略getter  setter
}
```

该类是处理全局异常的核心类，主要分为两部分：

1. 初始化
由于需要通过反射调用被`ExceptionHandler`标注的方法处理异常，与HandlerAdapter类型需要参数解析器和返回值处理，所以在`afterPropertiesSet`需要对参数解析器和返回值处理进行初始化；
其次还需要调用`initExceptionHandlerAdviceCache`完成`exceptionHandlerAdviceCache`变量的初始化，建立起`ControllerAdviceBean`和`ExceptionHandlerMethodResolver`的关系

2. resolveException处理异常返回ModelAndView
- 先通过调用`getExceptionHandlerMethod`找到处理异常`ControllerAdviceBean`、`ExceptionHandlerMethodResolver`，构建出`InvocableHandlerMethod`
- 执行方法的调用`exceptionHandlerMethod.invokeAndHandle`，这里你会发现编译出现异常，我们多写了最后一个参数，先不急，下一步我们来处理
- 通过`ModelAndViewContainer`构建ModelAndView对象

---

在之前我们创建的`InvocableHandlerMethod`未考虑到需要手动传入参数不需要通过参数解析器的情况，比如这里我们需要传入一个异常参数，所以我们需要修改一下`InvocableHandlerMethod`的invokeAndHandle方法

1. 添加一个参数providedArgs

```
    public void invokeAndHandle(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelAndViewContainer mavContainer,
                                Object... providedArgs) throws Exception {
        ....
    }
```
2. 修改方法getMethodArgumentValues，在执行解析器之前判断是否传入的参数满足

```
private List<Object> getMethodArgumentValues(HttpServletRequest request,
                                             HttpServletResponse response,
                                             ModelAndViewContainer mavContainer,
                                             Object... providedArgs) throws Exception {
    Assert.notNull(argumentResolver, "HandlerMethodArgumentResolver can not null");

    List<MethodParameter> parameters = this.getParameters();
    List<Object> args = new ArrayList<>(parameters.size());
    for (MethodParameter parameter : parameters) {
        parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
        //新增start
        Object arg = findProvidedArgument(parameter, providedArgs);
        if (Objects.nonNull(arg)) {
            args.add(arg);
            continue;
        }
        //新增end
        args.add(argumentResolver.resolveArgument(parameter, request, response, mavContainer, conversionService));
    }
    return args;
}
```

3. 添加方法findProvidedArgument

```
protected static Object findProvidedArgument(MethodParameter parameter, Object... providedArgs) {
    if (!ObjectUtils.isEmpty(providedArgs)) {
        for (Object providedArg : providedArgs) {
            if (parameter.getParameterType().isInstance(providedArg)) {
                return providedArg;
            }
        }
    }
    return null;
}
```

4. `InvocableHandlerMethod`新增构造方法

```
public InvocableHandlerMethod(Object bean, Method method,
                              HandlerMethodArgumentResolverComposite argumentResolver,
                              HandlerMethodReturnValueHandlerComposite returnValueHandler,
                              ConversionService conversionService) {
    super(bean, method);
    this.argumentResolver = argumentResolver;
    this.returnValueHandler = returnValueHandler;
    this.conversionService = conversionService;
}
```


##### 完成DispatcherServlet中的异常处理方法
1. 定义变量

```
private Collection<HandlerExceptionResolver> handlerExceptionResolvers;
```
2. 在init中完成初始化

```
this.handlerExceptionResolvers =
                this.applicationContext.getBeansOfType(HandlerExceptionResolver.class).values();
```
3. 完成processHandlerException中的处理逻辑

```
private ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
                                             Exception ex) throws Exception {
    if (CollectionUtils.isEmpty(this.handlerExceptionResolvers)) {
        throw ex;
    }
    for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
        ModelAndView exMv = resolver.resolveException(request, response, ex);
        if (exMv != null) {
            return exMv;
        }
    }
    //未找到对应的异常处理器，就继续抛出异常
    throw ex;
}
```


#### 单元测试
1. 在AppConfig.java中创建一个异常处理器

```
@Bean
public HandlerExceptionResolver handlerExceptionResolver(ConversionService conversionService) {
    ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
    resolver.setConversionService(conversionService);
    return resolver;
}
```
2. 自定义异常类`TestException`

```
public class TestException extends RuntimeException {
    private String name;

    public TestException(String message, String name) {
        super(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```
3. 定义接口通用返回对象`ApiResponse`

```

public class ApiResponse {
    private int code;
    private String message;
    private String data;

    public ApiResponse(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    //getter setter
}
```

4. 修改上篇中的`DispatcherController`，新增注解`@ControllerAdvice`，添加两个方法`dispatch2`，`exceptionHandler`，代码如下

```
@ControllerAdvice  //新增
@Controller
@RequestMapping(path = "/test")
public class DispatcherController {

    @RequestMapping(path = "/dispatch", method = RequestMethod.GET)
    public String dispatch(@RequestParam(name = "name") String name, Model model) {
        System.out.println("DispatcherController.dispatch: name=>" + name);
        model.addAttribute("name", name);
        return "redirect:/silently9527.cn";
    }

    @RequestMapping(path = "/dispatch2", method = RequestMethod.GET)
    public String dispatch2(@RequestParam(name = "name") String name) {
        System.out.println("DispatcherController.dispatch2: name=>" + name);
        //处理请求的过程中抛异常
        throw new TestException("test exception", name);
    }

    //当出现TestException异常会执行此方法
    @ResponseBody
    @ExceptionHandler({TestException.class})
    public ApiResponse exceptionHandler(TestException ex) {
        System.out.println("exception message:" + ex.getMessage());
        return new ApiResponse(200, "exception handle complete", ex.getName());
    }
}
```

5. 在`DispatcherServletTest`创建单元测试，验证出现异常后是否会执行`exceptionHandler`方法

```
@Test
public void test2() throws ServletException, IOException {
    dispatcherServlet.init();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("name", "silently9527");
    request.setRequestURI("/test/dispatch2");

    MockHttpServletResponse response = new MockHttpServletResponse();

    dispatcherServlet.service(request, response);

    System.out.println("响应到客户端的数据：");
    System.out.println(response.getContentAsString());
}
```

执行结果如下：

![result](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e5c5e7536cb84053b715fbbe0c1a5384~tplv-k3u1fbpfcp-watermark.image)


#### 总结
本篇我们开发完成了全局异常处理器`ExceptionHandlerExceptionResolver`，SmartMVC处理用户请求的整个流程我们已开发完成，
这其实也是SpringMVC的处理流程，到这里基本已经把SpringMVC如何处理请求的过程都实现了。为了驱动我们的SmartMVC项目能够很方便的运
行起来以及Springboot进行整合，我们下篇将开始开发SmartMVC的配置器。

#### 延展
虽然本次我们通过`ControllerAdvice`实现了处理全局异常，但是springmvc中`ControllerAdvice`注解的作用不止是用来处理全局异常，
比如：全局数据绑定、全局数据预处理；有兴趣的同学可去了解一下