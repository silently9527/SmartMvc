本节我们将开始开发在HandlerAdapter中需要使用到的组件`HandlerMethodArgumentResolver`，
原本计划是在本节之前先聊聊SpringMVC中的数据绑定，毕竟数据绑定在SpringMVC，甚至是Spring框架中都有重要的地位，
后来发现想要深入源码讲清楚数据绑定有些费劲，对于理解SpringMVC的核心原理无太多作用，所以决定在最后做大概三节番外篇来聊聊数据绑定。

在我们享受SpringMVC给我带来的便利的时候，不知道大家有没有想过，Controller中方法的参数是如何完成自动注入的，
在添加上注解`@PathVariable`、`@RequestParam`、`@RequestBody`就能够把请求中的参数主动注入，
甚至在方法参数任意位置写`HttpServletRequest`、`HttpSession`等类型的参数，它自动就有值了便可直接使用；
现在我们就开始来逐步实现这个功能，本节主要实现参数的解析。首先还是需要先看看类图

![UML](https://image-static.segmentfault.com/819/027/819027688-5fcba2e425b21_articlex)

本节我们简单实现解析`HttpServletRequest`、`HttpServletResponse`、`Model`以及注解`@RequestParam`、`@RequestBody`的功能，
SpringMVC提供其他参数解析器实现类似，可以查看SpringMVC源码

#### 开发步骤讲解


##### ModelAndViewContainer

```
public class ModelAndViewContainer {
    private Object view;
    private Model model;
    private HttpStatus status;
    private boolean requestHandled = false;

    public void setView(Object view) {
        this.view = view;
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }
    
    public void setViewName(String viewName) {
        this.view = viewName;
    }
    
    public Object getView() {
        return this.view;
    }

    public boolean isViewReference() {
        return (this.view instanceof String);
    }

    public Model getModel() {
        if (Objects.isNull(this.model)) {
            this.model = new ExtendedModelMap();
        }
        return this.model;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
    
    public boolean isRequestHandled() {
        return requestHandled;
    }

    public void setRequestHandled(boolean requestHandled) {
        this.requestHandled = requestHandled;
    }

}
```
该类的使用场景是每个请求进来都会新建一个对象，主要用于保存Handler处理过程中Model以及返回的View对象；该类将会用于参数解析器`HandlerMethodArgumentResolver`和Handler返回值解析器`HandlerMethodReturnValueHandler`；

- view: 定义的类型是Object，是因为Handler既可以返回一个String表示视图的名字，也可以直接返回一个视图对象View
- Model、ExtendedModelMap: 都是Spring中定义的类，可以直接看做是Map
- requestHandled: 标记本次请求是否已经处理完成，后期在处理注解`@ResponseBody`将会使用到

##### HandlerMethodArgumentResolver

```
public interface HandlerMethodArgumentResolver {
    boolean supportsParameter(MethodParameter parameter);
    Object resolveArgument(MethodParameter parameter, HttpServletRequest request, 
                    HttpServletResponse response, ModelAndViewContainer container,
                           ConversionService conversionService) throws Exception;
}

```
该接口是一个策略接口，作用是把请求中的数据解析为Controller中方法的参数值。
有了它才能会让Spring MVC处理入参显得那么高级、那么自动化。定义了两个方法

- supportsParameter: 此方法判断当前的参数解析器是否支持传入的参数，返回true表示支持
- resolveArgument: 从request对象中解析出parameter需要的值，除了`MethodParameter`和`HttpServletRequest`参数外，
还传入了`ConversionService`，用于在把request中取出的值需要转换成`MethodParameter`参数的类型。
这个方法的参数定义和SpringMVC中的方法稍有不同，主要是为了简化数据转换的过程


##### ServletRequestMethodArgumentResolver、ServletResponseMethodArgumentResolver
首先我们来实现两个简单的参数解析器，当我们Handler参数类型是` HttpServletRequest`、`HttpServletResponse`，需要自动注入。代码如下：

```
public class ServletRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return ServletRequest.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        return request;
    }

}
```
1. 在`supportsParameter`先取出Handler参数的类型，判断该类型是不是`ServletRequest`的子类，如果是返回true
2. 当`supportsParameter`返回true的时候执行`resolveArgument`方法，在该方法中直接返回request对象


```
public class ServletResponseMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return ServletResponse.class.isAssignableFrom(parameterType);
    }
    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response,
                                  ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        return response;
    }
}
```


##### ModelMethodArgumentResolver

```
public class ModelMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Model.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {

        Assert.state(container != null, "ModelAndViewContainer is required for model exposure");
        return container.getModel();
    }
}
```
该解析器主要是从解析出Model对象，方便后期对Handler中的Model参数进行注入

##### RequestParamMethodArgumentResolver
接下来再来实现注解`@RequestParam`的功能，当Handler中的参数被`@RequestParam`标注，需要从request中取出对应的参数，
然后调用`ConversionService`转换成正确的类型。

1. 定义注解`@RequestParam`

```
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String name();

    boolean required() default true;

    String defaultValue() default "";

}
```

- name: 从request取参数的名字，该参数必填
- required: 说明该参数是否必填，默认是true
- defaultValue: 如果request中找不到对应的参数，那么就用默认值

2. 实现解析器`RequestParamMethodArgumentResolver`

```
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {

        RequestParam param = parameter.getParameterAnnotation(RequestParam.class);
        if (Objects.isNull(param)) {
            return null;
        }
        String value = request.getParameter(param.name());
        if (StringUtils.isEmpty(value)) {
            value = param.defaultValue();
        }
        if (!StringUtils.isEmpty(value)) {
            return conversionService.convert(value, parameter.getParameterType());
        }
        
        if (param.required()) {
            throw new MissingServletRequestParameterException(parameter.getParameterName(),
                    parameter.getParameterType().getName());
        }
        return null;
    }

}
```

- supportsParameter: 判断Handler的参数是否有添加注解`@RequestParam`
- resolveArgument: 从request中找指定name的参数，如果找不到用默认值赋值，如果默认值也没有，当required=true时抛出异常，
否知返回null; 如果从request中找到了参数值，那么调用`conversionService.convert`方法转换成正确的类型


##### RequestBodyMethodArgumentResolver 
我们继续实现最后一个注解`@RequestBody`，当被这个注解的参数，需要把request流中的数据转换成对象

1. 定义注解

```
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    boolean required() default true;
}
```

2. 实现解析器`RequestBodyMethodArgumentResolver`

由于我们需要使用到JSON的转换，所以我们引入fastjson

```
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.60</version>
</dependency>
```

完整代码如下：

```
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        String httpMessageBody = this.getHttpMessageBody(request);
        if (!StringUtils.isEmpty(httpMessageBody)) {
            return JSON.parseObject(httpMessageBody, parameter.getParameterType());
        }

        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        if (Objects.isNull(requestBody)) {
            return null;
        }
        if (requestBody.required()) {
            throw new MissingServletRequestParameterException(parameter.getParameterName(),
                    parameter.getParameterType().getName());
        }
        return null;
    }

    private String getHttpMessageBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        char[] buff = new char[1024];
        int len;
        while ((len = reader.read(buff)) != -1) {
            sb.append(buff, 0, len);
        }
        return sb.toString();
    }
}

```
- getHttpMessageBody: 从request对象流中读取出数据转换成字符串
- resolveArgument: 把取出来的字符串通过fastjson转换成参数类型的对象

##### HandlerMethodArgumentResolverComposite
接下来我们创建参数解析器的组合类`HandlerMethodArgumentResolverComposite`，这也是策略模式的常用方式

```
public class HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver {
    private List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return true;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        for (HandlerMethodArgumentResolver resolver : argumentResolvers) {
            if (resolver.supportsParameter(parameter)) {
                return resolver.resolveArgument(parameter, request, conversionService);
            }
        }
        throw new IllegalArgumentException("Unsupported parameter type [" +
                parameter.getParameterType().getName() + "]. supportsParameter should be called first.");
    }

    public void addResolver(HandlerMethodArgumentResolver resolver) {
        this.argumentResolvers.add(resolver);
    }

    public void addResolver(HandlerMethodArgumentResolver... resolvers) {
        Collections.addAll(this.argumentResolvers, resolvers);
    }

    public void clear() {
        this.argumentResolvers.clear();
    }
}

```
同样也实现接口`HandlerMethodArgumentResolver`，内部定义List，在`resolveArgument`中循环所有的解析器，
找到支持参数的解析器就开始解析，找不到就抛出异常


#### 单元测试
到此，三个解析器都已经开发完成，我们来做一些单元测试，先定义测试用例：
- 验证`@RequestParam`: 创建TestController，方法test4的参数name, age, birthday, request，
验证解析器是否能够正常处理类型为String、Integer、Date、HttpServletRequest的解析
- 验证`@RequestBody`:创建TestController，方法user的参数UserVo, 验证解析器能够正确的把JSON字符串解析成UserVo对象

创建TestController，代码如下：

```
@Service
public class TestController {

    @RequestMapping(path = "/test4", method = RequestMethod.POST)
    public void test4(@RequestParam(name = "name") String name,
                      @RequestParam(name = "age") Integer age,
                      @RequestParam(name = "birthday") Date birthday,
                      HttpServletRequest request) {
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public void user(@RequestBody UserVo userVo) {
    }

}
```

创建UserVo对象
```
public class UserVo {
    private String name;
    private Integer age;
    private Date birthday;

    //省略getter setter toString
}
```

1. 编写单元测试1，验证第一个测试用例

```
@Test
public void test1() throws NoSuchMethodException {
    TestController testController = new TestController();
    Method method = testController.getClass().getMethod("test4",
            String.class, Integer.class, Date.class, HttpServletRequest.class);

    //构建HandlerMethod对象
    HandlerMethod handlerMethod = new HandlerMethod(testController, method);

    //构建模拟请求的request
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("name", "Silently9527");
    request.setParameter("age", "25");
    request.setParameter("birthday", "2020-11-12 13:00:00");

    //添加支持的解析器
    HandlerMethodArgumentResolverComposite resolverComposite = new HandlerMethodArgumentResolverComposite();
    resolverComposite.addResolver(new RequestParamMethodArgumentResolver());
    resolverComposite.addResolver(new ServletRequestMethodArgumentResolver());

    //定义转换器
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
    DateFormatter dateFormatter = new DateFormatter();
    dateFormatter.setPattern("yyyy-MM-dd HH:mm:ss"); 
    conversionService.addFormatter(dateFormatter);
    
    MockHttpServletResponse response = new MockHttpServletResponse();

    //用于查找方法参数名
    DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    handlerMethod.getParameters().forEach(methodParameter -> {
        try {
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            
            Object value = resolverComposite.resolveArgument(methodParameter, request,response, null, conversionService);
            System.out.println(methodParameter.getParameterName() + " : " + value + "   type: " + value.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}
```
该单元测试中有两点说明一下：
1. `DefaultFormattingConversionService`: 该类是Spring中的一个数据转换器服务，默认已经添加了很多转换器，
这里我们还设置了日期转换的格式`yyyy-MM-dd HH:mm:ss`
2. `DefaultParameterNameDiscoverer`: 该类是用于查找参数名的类，因为一般来说，通过反射是很难获得参数名的，只能取到参数类型，
因为在编译时，参数名有可能是会改变的，所以需要这样一个类，Spring已经实现了多种解析，我们这里直接引用就行

最后打印出解析出来的参数名字、值、类型

![执行结果](https://image-static.segmentfault.com/276/501/276501005-5fcb7c433e81d_articlex)

2. 编写单元测试验证用例2

```
@Test
public void test2() throws NoSuchMethodException {
    TestController testController = new TestController();
    Method method = testController.getClass().getMethod("user", UserVo.class);

    HandlerMethod handlerMethod = new HandlerMethod(testController, method);

    MockHttpServletRequest request = new MockHttpServletRequest();
    UserVo userVo = new UserVo();
    userVo.setName("Silently9527");
    userVo.setAge(25);
    userVo.setBirthday(new Date());
    request.setContent(JSON.toJSONString(userVo).getBytes()); //模拟JSON参数

    HandlerMethodArgumentResolverComposite resolverComposite = new HandlerMethodArgumentResolverComposite();
    resolverComposite.addResolver(new RequestBodyMethodArgumentResolver());

    MockHttpServletResponse response = new MockHttpServletResponse();

    DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    handlerMethod.getParameters().forEach(methodParameter -> {
        try {
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            Object value = resolverComposite.resolveArgument(methodParameter, request, response, null, null);
            System.out.println(methodParameter.getParameterName() + " : " + value + "   type: " + value.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}
```

执行的结果如下：

![](https://image-static.segmentfault.com/214/005/2140052655-5fcb7cf7bba5b_articlex)


#### 总结
本小节我们完成了三个参数解析器，了解到SpringMVC中Handler参数的解析过程。
下一节我们将开始研发返回值解析器`HandlerMethodReturnValueHandler`

#### 延展
本节我们开发的解析器只实现了参数的自动封装；而SpringMVC的参数解析器还包含了参数的校验等，并且SpringMVC已经提供了很丰富的解析器，
比如：`PathVariableMethodArgumentResolver`、`SessionAttributeMethodArgumentResolver`、`ServletCookieValueMethodArgumentResolver`等等，建议都了解一下
