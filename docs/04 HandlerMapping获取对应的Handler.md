本节我们开始来开发`HandlerMapping`接口中主要的方法`getHandler`，通过请求request找到需要执行Handler，涉及到的新类不多
![类图](https://image-static.segmentfault.com/394/210/3942105762-5fc3aaaa3277e_articlex)

#### 开发步骤讲解


##### HandlerExecutionChain

该类的主要包含了两个对象
1. HandlerMethod: 根据request中的path找到匹配的`HandlerMethod`，也就是控制器中的某个方法
2. List<HandlerInterceptor> : 根据request中的path找到所有对本次请求生效的拦截器`HandlerInterceptor`

三个方法：
1. applyPreHandle: 执行所有拦截器的preHandle方法，如果preHandle返回的是false，那么就执行triggerAfterCompletion
2. applyPostHandle: 执行所有拦截器的postHandle方法
3. triggerAfterCompletion: `HandlerExecutionChain`中还定义了一个变量`interceptorIndex`，
当每执行一个`HandlerInterceptor`的preHandle方法后`interceptorIndex`的值就会被修改成当前执行拦截器的下标，
`triggerAfterCompletion`中根据`interceptorIndex`记录的下标值反向执行拦截器的`afterCompletion`方法；

举例说明：假如有三个拦截器，第一个拦截器正常执行完成preHandle方法，在执行第二个拦截器的preHandle返回了false，
那么当调用`triggerAfterCompletion`只会执行第一个拦截器的afterCompletion


完整代码如下：

```
public class HandlerExecutionChain {
    private HandlerMethod handler;
    private List<HandlerInterceptor> interceptors = new ArrayList<>();
    private int interceptorIndex = -1;

    public HandlerExecutionChain(HandlerMethod handler, List<HandlerInterceptor> interceptors) {
        this.handler = handler;
        if (!CollectionUtils.isEmpty(interceptors)) {
            this.interceptors = interceptors;
        }
    }

    public boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (CollectionUtils.isEmpty(interceptors)) {
            return true;
        }
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            if (!interceptor.preHandle(request, response, this.handler)) {
                triggerAfterCompletion(request, response, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    public void applyPostHandle(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) throws Exception {
        if (CollectionUtils.isEmpty(interceptors)) {
            return;
        }
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            interceptor.postHandle(request, response, this.handler, mv);
        }
    }

    public void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex)
            throws Exception {
        if (CollectionUtils.isEmpty(interceptors)) {
            return;
        }
        for (int i = this.interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptors.get(i);
            interceptor.afterCompletion(request, response, this.handler, ex);
        }
    }

    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }

    public HandlerMethod getHandler() {
        return handler;
    }
}
```

##### NoHandlerFoundException

在通过HandlerMapping.getHandler获取对应request处理器的时候，可能会遇到写错了请求的路径导致找不到匹配的Handler情况，
这个时候需要抛出指定的异常，方便我们后续处理，比如说跳转到错误页面

```
public class NoHandlerFoundException extends ServletException {
    private String httpMethod;
    private String requestURL;

    public NoHandlerFoundException(HttpServletRequest request) {
        this.httpMethod = request.getMethod();
        this.requestURL = request.getRequestURL().toString();
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }
}

```

##### RequestMappingHandlerMapping.getHandler

1. 我们需要先在`RequestMappingHandlerMapping`中添加拦截器的代码，

```
private List<MappedInterceptor> interceptors = new ArrayList<>();

public void setInterceptors(List<MappedInterceptor> interceptors) {
    this.interceptors = interceptors;
}
```

为什么我们还需要在`RequestMappingHandlerMapping`保存拦截器的集合呢？与`HandlerExecutionChain`中拦截器的集合有什么区别？
1. `RequestMappingHandlerMapping`中拦截器的集合包含了容器中所有的拦截器，而`HandlerExecutionChain`中拦截器集合只包含了匹配请求path的拦截器
2. `RequestMappingHandlerMapping`是获取Handler的工具，构建`HandlerExecutionChain`的过程中需要从所有拦截器中找到
与本次请求匹配的拦截器，所以把所有拦截器的集合放到`RequestMappingHandlerMapping`中是合理的

2. 接下来我们看看`RequestMappingHandlerMapping.getHandler`的具体实现

```
@Override
public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    String lookupPath = request.getRequestURI();
    HandlerMethod handler = mappingRegistry.getHandlerMethodByPath(lookupPath);
    if (Objects.isNull(handler)) {
        throw new NoHandlerFoundException(request);
    }
    return createHandlerExecutionChain(lookupPath, handler);
}
```

通过调用`request.getRequestURI()`获取到本次请求的path，然后根据path从`MappingRegistry`找到对应的`HandlerMethod`，
如果找不到就抛出之前我们定义的异常`NoHandlerFoundException`；


```
private HandlerExecutionChain createHandlerExecutionChain(String lookupPath, HandlerMethod handler) {
    List<HandlerInterceptor> interceptors = this.interceptors.stream()
            .filter(mappedInterceptor -> mappedInterceptor.matches(lookupPath))
            .collect(toList());
    return new HandlerExecutionChain(handler, interceptors);
}

```
从所有拦截器中过滤出匹配本次请求path的拦截器，然后创建`HandlerExecutionChain`对象。到此getHandler的功能开发完成


#### 6.2 单元测试
我们开始写单元测试，本节的测试用例：

1. 测试getHandler返回的HandlerExecutionChain数据是否正确：
    a) HandlerMethod中的bean是正确的Controller实例；
    b) interceptors是否是匹配请求的path
2. 测试getHandler找不到Handler是否会抛出异常`NoHandlerFoundException`
3. 同一个拦截器添加连个includePatterns，能正确匹配

接下来我们根据测试用例来编写测试代码：

建立两个拦截器`TestHandlerInterceptor`、`Test2HandlerInterceptor`, 一个控制器`TestHandlerController`

```
public class Test2HandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Test2HandlerInterceptor => preHandle");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("Test2HandlerInterceptor => postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("Test2HandlerInterceptor => afterCompletion");
    }
}

public class TestHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("TestHandlerInterceptor => preHandle");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("TestHandlerInterceptor => postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("TestHandlerInterceptor => afterCompletion");
    }
}

@Controller
public class TestHandlerController {

    @RequestMapping(path = "/ex_test", method = RequestMethod.POST)
    public void exTest() {
    }

    @RequestMapping(path = "/in_test", method = RequestMethod.POST)
    public void inTest() {
    }


    @RequestMapping(path = "/in_test2", method = RequestMethod.POST)
    public void inTest2() {
    }

    @RequestMapping(path = "/in_test3", method = RequestMethod.POST)
    public void inTest3() {
    }

}

```

在`AppConfig.java`中配置 RequestMappingHandlerMapping ，配置拦截器，把拦截器集合放入到RequestMappingHandlerMapping中

```
@Bean
public RequestMappingHandlerMapping handlerMapping() {
    InterceptorRegistry interceptorRegistry = new InterceptorRegistry();

    TestHandlerInterceptor interceptor = new TestHandlerInterceptor();
    interceptorRegistry.addInterceptor(interceptor)
            .addExcludePatterns("/ex_test")
            .addIncludePatterns("/in_test");

    Test2HandlerInterceptor interceptor2 = new Test2HandlerInterceptor();
    interceptorRegistry.addInterceptor(interceptor2)
            .addIncludePatterns("/in_test2", "/in_test3");

    RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
    mapping.setInterceptors(interceptorRegistry.getMappedInterceptors());
    return mapping;
}

```

单元测试代码：

```
@Test
public void testGetHandler() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    //测试TestHandlerInterceptor拦截器生效
    request.setRequestURI("/in_test");
    HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);

    HandlerMethod handlerMethod = executionChain.getHandler();
    Assert.assertTrue(handlerMethod.getBean() instanceof TestHandlerController);
    Assert.assertTrue(((MappedInterceptor) executionChain.getInterceptors().get(0)).getInterceptor()
            instanceof TestHandlerInterceptor);

    //测试TestHandlerInterceptor拦截器不生效
    request.setRequestURI("/ex_test");
    executionChain = requestMappingHandlerMapping.getHandler(request);
    Assert.assertEquals(executionChain.getInterceptors().size(), 0);

    //测试找不到Handler,抛出异常
    request.setRequestURI("/in_test454545");
    try {
        requestMappingHandlerMapping.getHandler(request);
    } catch (NoHandlerFoundException e) {
        System.out.println("异常URL:" + e.getRequestURL());
    }

    //测试Test2HandlerInterceptor拦截器对in_test2、in_test3都生效
    request.setRequestURI("/in_test2");
    executionChain = requestMappingHandlerMapping.getHandler(request);
    Assert.assertEquals(executionChain.getInterceptors().size(), 1);
    Assert.assertTrue(((MappedInterceptor) executionChain.getInterceptors().get(0)).getInterceptor()
            instanceof Test2HandlerInterceptor);

    request.setRequestURI("/in_test3");
    executionChain = requestMappingHandlerMapping.getHandler(request);
    Assert.assertEquals(executionChain.getInterceptors().size(), 1);
    Assert.assertTrue(((MappedInterceptor) executionChain.getInterceptors().get(0)).getInterceptor()
            instanceof Test2HandlerInterceptor);
}
```

运行结果如下：

![单元测试结果](https://image-static.segmentfault.com/281/205/2812054056-5fc3a45a25449_articlex)

#### 总结
本节完成了获取Handler的开发，其中主要的对象是`HandlerExecutionChain`，它包含了具体执行业务逻辑的`HandlerMethod`以及匹配的拦截器；
到此`HandlerMapping`的大部分开发工作都已完成，下一节我开始研发`HandlerAdapter`


#### 延展
完成本节的开发后，可以对比着去看看SpringMVC中`RequestMappingHandlerMapping`的getHandler方法，提供的功能更加完善，
比如：跨域配置CORS
