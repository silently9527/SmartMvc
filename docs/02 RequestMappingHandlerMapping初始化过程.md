不知道大家有没有好奇过，SpringMVC是如何通过request就可以找到我们写的Controller中的一个方法，它是怎么做到的，什时候做的呢？
本节我们就来揭开它的面纱，实现HandlerMapping的初始化过程，把Controller中的方法转换成我们定义的`HandlerMethod`对象（也就是架构图中画的Handler），
根据注解`RequestMapping`来映射url和`HandlerMethod`的对应关系。首先我们先来看下本节中涉及到的类

![](https://image-static.segmentfault.com/280/516/2805161289-5fc25a04be986_articlex)

#### 研发步骤讲解

##### `HandlerMapping`接口
 
`HandlerMapping`接口中只有一个方法，通过request找个需要执行的handler，包装成`HandlerExecutionChain`，
改方法本节中暂时不实现，下一节再来开发这部分代码，现在主要是实现`HandlerMapping`的初始化过程

```
public interface HandlerMapping {
    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
}

```

##### `RequestMapping`注解

`RequestMapping`注解，只提供了两个属性`path`，`method`
1. `path` 表示url中的路径
2. `method` 表示http请求的方式 ` GET`，`POST`

```
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String path();

    RequestMethod method() default RequestMethod.GET;

}

```

##### `RequestMappingInfo`

主要是对应配置在控制器方法上的`RequestMapping`注解，把`RequestMapping`注解转换成`RequestMappingInfo`对象

```
public class RequestMappingInfo {
    private String path;
    private RequestMethod httpMethod;

    public RequestMappingInfo(String prefix, RequestMapping requestMapping) {
        this.path = prefix + requestMapping.path();
        this.httpMethod = requestMapping.method();
    }

    public String getPath() {
        return path;
    }

    public RequestMethod getHttpMethod() {
        return httpMethod;
    }

}
```

##### `HandlerMethod`

`HandlerMethod`是个很重要的对象，主要是对应控制器中的方法（Controller中的每个方法），也就是实际处理业务的handler；这里我们暂时定义了四个属性

1. bean: 表示该方法的实例对象，也就是Controller的实例对象
2. beanType: 表示的是我们写的Controller的类型；比如我们常定义的：`IndexController`、`UserController`等等
3. method: 表示Controller中的方法
4. parameters: 表示方法中的所有参数的定义，这里引用了Spring中提供的`MethodParameter`工具类，里面封装了一些实用的方法，比如说后面会用到获取方法上的注解等等

`HandlerMethod`只暂时提供了一个构造方法，通bean和method构建一个`HandlerMethod`对象

```
public class HandlerMethod {

    private Object bean;
    private Class<?> beanType;
    private Method method;

    private List<MethodParameter> parameters;

    public HandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.beanType = bean.getClass();
        this.method = method;

        this.parameters = new ArrayList<>();
        int parameterCount = method.getParameterCount();
        for (int index = 0; index < parameterCount; index++) {
            parameters.add(new MethodParameter(method, index));
        }
    }
}
```

##### `MappingRegistry`

`MappingRegistry`是`RequestMappingInfo`、`HandlerMethod`的注册中心，当解析完一个控制器的method后就会向`MappingRegistry`
中注册一个；最后当接收到用户请求后，根据请求的url在`MappingRegistry`找到对应的`HandlerMethod`；

本节写了三个方法:

1. register: 把解析完成的`RequestMappingInfo`注册到Map中；通过`method`，`handler`构建`HandlerMethod`对象，然后也加入到Map中
2. getMappingByPath: 通过path查找出`RequestMappingInfo`
3. getHandlerMethodByPath: 通过path查找出`HandlerMethod`

```
/**
 * 所有映射的注册中心
 */
public class MappingRegistry {
    private Map<String, RequestMappingInfo> pathMappingInfo = new ConcurrentHashMap<>();
    private Map<String, HandlerMethod> pathHandlerMethod = new ConcurrentHashMap<>();


    /**
     * 注册url和Mapping/HandlerMethod的对应关系
     *
     * @param mapping
     * @param handler
     * @param method
     */
    public void register(RequestMappingInfo mapping, Object handler, Method method) {
        pathMappingInfo.put(mapping.getPath(), mapping);

        HandlerMethod handlerMethod = new HandlerMethod(handler, method);
        pathHandlerMethod.put(mapping.getPath(), handlerMethod);
    }

    public RequestMappingInfo getMappingByPath(String path) {
        return this.pathMappingInfo.get(path);
    }

    public HandlerMethod getHandlerMethodByPath(String path) {
        return this.pathHandlerMethod.get(path);
    }

}

```

##### `RequestMappingHandlerMapping`

本节需要使用的组件类都已经写完了，现在就开始开发主要类`RequestMappingHandlerMapping`的初始化过程，完整代码如下：

```
public class RequestMappingHandlerMapping extends ApplicationObjectSupport implements HandlerMapping, InitializingBean {

    private MappingRegistry mappingRegistry = new MappingRegistry();


    public MappingRegistry getMappingRegistry() {
        return mappingRegistry;
    }

    public void afterPropertiesSet() throws Exception {
        initialHandlerMethods();
    }

    private void initialHandlerMethods() {
        Map<String, Object> beansOfMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(), Object.class);
        beansOfMap.entrySet().stream()
                .filter(entry -> this.isHandler(entry.getValue()))
                .forEach(entry -> this.detectHandlerMethods(entry.getKey(), entry.getValue()));
    }

    /**
     * 类上有标记Controller的注解就是我们需要找的handler
     *
     * @param handler
     * @return
     */
    private boolean isHandler(Object handler) {
        Class<?> beanType = handler.getClass();
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class));
    }

    /**
     * 解析出handler中 所有被RequestMapping注解的方法
     *
     * @param beanName
     * @param handler
     */
    private void detectHandlerMethods(String beanName, Object handler) {
        Class<?> beanType = handler.getClass();
        Map<Method, RequestMappingInfo> methodsOfMap = MethodIntrospector.selectMethods(beanType,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> getMappingForMethod(method, beanType));

        methodsOfMap.forEach((method, requestMappingInfo) -> this.mappingRegistry.register(requestMappingInfo, handler, method));
    }

    /**
     * 查找method上面是否有RequestMapping，有 => 构建RequestMappingInfo
     *
     * @param method
     * @param beanType
     * @return
     */
    private RequestMappingInfo getMappingForMethod(Method method, Class<?> beanType) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (Objects.isNull(requestMapping)) {
            return null;
        }
        String prefix = getPathPrefix(beanType);
        return new RequestMappingInfo(prefix, requestMapping);
    }

    private String getPathPrefix(Class<?> beanType) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(beanType, RequestMapping.class);
        if (Objects.isNull(requestMapping)) {
            return "";
        }
        return requestMapping.path();
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        return null;
    }

}
```

因为在初始化的过程中，我们需要获取到容器中所有的Bean对象，所以`RequestMappingHandlerMapping`需要继承于`ApplicationObjectSupport`，
`ApplicationObjectSupport`为我们提供了方便访问容器的方法；因为`RequestMappingHandlerMapping`需要在创建完对象后初始化`HandlerMethod`，
所以实现了接口`InitializingBean`(提供了`afterPropertiesSet`方法，在对象创建完成后，spring容器会调用这个方法)，
初始化代码的入口就在`afterPropertiesSet`中。


```
private void initialHandlerMethods() {
    Map<String, Object> beansOfMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(), Object.class);
    beansOfMap.entrySet().stream()
            .filter(entry -> this.isHandler(entry.getValue()))
            .forEach(entry -> this.detectHandlerMethods(entry.getKey(), entry.getValue()));
}
```
首先我们需要从容器中拿出所有的Bean，这里我们用的是Spring提供的工具类
`BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(), Object.class);`，
该方法将会返回beanName和bean实例对应的Map；

接着需要过滤出所有被标记`@Controller`的类，代码写在了`isHandler`方法中

```
/**
 * 类上有标记Controller的注解就是我们需要找的handler
 *
 * @param handler
 * @return
 */
private boolean isHandler(Object handler) {
    Class<?> beanType = handler.getClass();
    return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class));
}
```
这里也使用到了Spring中的工具类`AnnotatedElementUtils.hasAnnotation`判断类是否有添加注解`@Controller`；
找出所有的Controller之后，我们需要解析出Controller中的所有方法，构建我们需要的`HandlerMethod`

```
/**
 * 解析出handler中 所有被RequestMapping注解的方法
 *
 * @param beanName
 * @param handler
 */
private void detectHandlerMethods(String beanName, Object handler) {
    Class<?> beanType = handler.getClass();
    Map<Method, RequestMappingInfo> methodsOfMap = MethodIntrospector.selectMethods(beanType,
            (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> 
            getMappingForMethod(method, beanType));

    methodsOfMap.forEach((method, requestMappingInfo) -> this.mappingRegistry.register(requestMappingInfo, handler, method));
}

/**
 * 查找method上面是否有RequestMapping，有 => 构建RequestMappingInfo
 *
 * @param method
 * @param beanType
 * @return
 */
private RequestMappingInfo getMappingForMethod(Method method, Class<?> beanType) {
    RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
    if (Objects.isNull(requestMapping)) {
        return null;
    }
    String prefix = getPathPrefix(beanType);
    return new RequestMappingInfo(prefix, requestMapping);
}
```

使用工具类`MethodIntrospector.selectMethods`找出Controller类中所有的方法，遍历每个方法，判断方法是否有添加注解`@RequestMapping`，如果没有就返回空，如果有就通过`@RequestMapping`构建`RequestMappingInfo`对象返回；如果所Controller类上有添加注解`@RequestMapping`，那么配的path将作为前缀

```
private String getPathPrefix(Class<?> beanType) {
    RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(beanType, RequestMapping.class);
    if (Objects.isNull(requestMapping)) {
        return "";
    }
    return requestMapping.path();
}
```

当所有的方法都解析完成之后，需要把所有配置有`@RequestMapping`注解的方法注册到`MappingRegistry`,代码如下：
`methodsOfMap.forEach((method, requestMappingInfo) -> this.mappingRegistry.register(requestMappingInfo, handler, method));`


#### 单元测试
到此为止，我们把Controller中方法的解析过程已经开发完成，接下来我们来写一点简单的单元测试。

`AppConfig.java`中添加代码构建`RequestMappingHandlerMapping`

```
@Configuration
@ComponentScan(basePackages = "com.silently9527.smartmvc")
public class AppConfig {

    @Bean
    public RequestMappingHandlerMapping handlerMapping() {
        return new RequestMappingHandlerMapping();
    }

}
```

为了测试我们的Controller解析是否正确，我们需要的测试用例：

- 在IndexController中我们在类上面配置path的前缀`/index`；解析完成后的path要拼接上`/index`
- 在IndexController中添加三个方法，其中`test`、`test2`两个用`@RequestMapping`标注，`test3`不标注；解析完成后`test3`不再我们注册中心里面，`test`、`test2`两个在注册中里面，并且`@RequestMapping`中的属性正确解析成`RequestMappingInfo`对象
- 创建TestConroller类，添加一个方法`test4`，在类上面标注注解`@Service`，解析完成后`test4`不能在注册中心里面找到

`IndexController`，`TestController`代码如下：

```
@Controller
@RequestMapping(path = "/index")
public class IndexController {

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public void test(String name) {

    }

    @RequestMapping(path = "/test2", method = RequestMethod.POST)
    public void test2(String name2) {

    }

    public void test3(String name3) {

    }

}
```

```
@Service
public class TestController {

    @RequestMapping(path = "/test4", method = RequestMethod.POST)
    public void test4(String name2) {

    }

}
```

接下来建立我们的单元测试类`RequestMappingHandlerMappingTest`，继承于我们上一节写好的测试基类`BaseJunit4Test`

```
public class RequestMappingHandlerMappingTest extends BaseJunit4Test {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    public void test() {
        MappingRegistry mappingRegistry = requestMappingHandlerMapping.getMappingRegistry();

        String path = "/index/test";
        String path1 = "/index/test2";
        String path4 = "/test4";

        Assert.assertEquals(mappingRegistry.getPathHandlerMethod().size(), 2);

        HandlerMethod handlerMethod = mappingRegistry.getHandlerMethodByPath(path);
        HandlerMethod handlerMethod2 = mappingRegistry.getHandlerMethodByPath(path1);
        HandlerMethod handlerMethod4 = mappingRegistry.getHandlerMethodByPath(path4);

        Assert.assertNull(handlerMethod4);
        Assert.assertNotNull(handlerMethod);
        Assert.assertNotNull(handlerMethod2);


        RequestMappingInfo mapping = mappingRegistry.getMappingByPath(path);
        RequestMappingInfo mapping2 = mappingRegistry.getMappingByPath(path1);

        Assert.assertNotNull(mapping);
        Assert.assertNotNull(mapping2);
        Assert.assertEquals(mapping.getHttpMethod(), RequestMethod.GET);
        Assert.assertEquals(mapping2.getHttpMethod(), RequestMethod.POST);
    }

}
```
单元测试运行结果正常通过：

![result](https://image-static.segmentfault.com/243/164/2431648666-5fc340a9c54e1_articlex)




#### 本节小结
本节我们实现了HandlerMapping的初始化, 了解到了Controller中的方法是如何转换成`HandlerMethod`；

#### 延展
本节我们实现的`RequestMappingHandlerMapping`，`HandlerMethod`相比SpringMVC都比较简单，大家可以对应着去看看SpringMVC中的实现；因为在SpringMVC中不仅仅有`@RequestMapping`，还有基于xml配置的`SimpleUrlHandlerMapping`以及`BeanNameUrlHandlerMapping`等等，所有`RequestMappingHandlerMapping`在SpringMVC中还有两层抽象类，有兴趣的小伙伴可以去看看每个实现。



