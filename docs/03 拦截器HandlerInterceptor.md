本小节的内容设置相对简单，主要来实现SmartMVC中的拦截器部分，首先我还是来看下本小节涉及到的类图，以及这些类需要提供的方法

![拦截器类图](https://image-static.segmentfault.com/205/681/2056813523-5fc35eb3cd06c_articlex)

#### 开发步骤讲解


##### HandlerInterceptor

首先我们来定义`HandlerInterceptor`接口，提供了三个方法：
1. preHandle: 在执行Handler之前被调用，如果返回的是false，那么Handler就不会在执行
2. postHandle: 在Handler执行完成之后被调用，可以获取Handler返回的结果`ModelAndView`
3. afterCompletion: 该方法是无论什么情况下都会被调用，比如：`preHandle`返回false，Handler执行过程中抛出异常，Handler正常执行完成

```
public interface HandlerInterceptor {

    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return true;
    }

    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                            @Nullable ModelAndView modelAndView) throws Exception {
    }

    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable Exception ex) throws Exception {
    }
}

```

##### MappedInterceptor

我们都知道通常拦截器是需要设置对哪些URL生效的，但是从上面的拦截器接口定义我们没看到类设置，为了达到配置与业务的分离，
所以我们又建立`MappedInterceptor`。`MappedInterceptor`所需要完成的功能：

1. 作为真正`HandlerInterceptor`的代理类，所以需要继承于`HandlerInterceptor`，实现`HandlerInterceptor`的三个接口，
并且内部需要包含真正`HandlerInterceptor`的实例
2. 管理`interceptor`对哪些URL生效，排除哪些URL
3. 提供match功能，调用方传入path，判断当前`HandlerInterceptor`是否支持本次请求。该功能简单实现，只支持path的完整匹配，
需要了解更复杂的匹配请查看SpringMVC中的`MappedInterceptor`

完整代码如下：

```
public class MappedInterceptor implements HandlerInterceptor {
    private List<String> includePatterns = new ArrayList<>();
    private List<String> excludePatterns = new ArrayList<>();

    private HandlerInterceptor interceptor;

    public MappedInterceptor(HandlerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * 添加支持的path
     *
     * @param patterns
     * @return
     */
    public MappedInterceptor addIncludePatterns(String... patterns) {
        this.includePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    /**
     * 添加排除的path
     *
     * @param patterns
     * @return
     */
    public MappedInterceptor addExcludePatterns(String... patterns) {
        this.excludePatterns.addAll(Arrays.asList(patterns));
        return this;
    }


    /**
     * 根据传入的path, 判断当前的interceptor是否支持
     *
     * @param lookupPath
     * @return
     */
    public boolean matches(String lookupPath) {
        if (!CollectionUtils.isEmpty(this.excludePatterns)) {
            if (excludePatterns.contains(lookupPath)) {
                return false;
            }
        }
        if (ObjectUtils.isEmpty(this.includePatterns)) {
            return true;
        }
        if (includePatterns.contains(lookupPath)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return this.interceptor.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        this.interceptor.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        this.interceptor.afterCompletion(request, response, handler, ex);
    }
}
```

##### InterceptorRegistry

现在我们已经开发完了处理拦截业务逻辑的接口`HandlerInterceptor`，管理`HandlerInterceptor`与请求路径的映射关联类
`MappedInterceptor`，我们还缺少一个拦截器的注册中心管理所有的拦截器，试想下如果没有这个，
那么当需要获取项目中所有拦截器的时候就会很难受，所以我们还需要建了一个`InterceptorRegistry`

```
public class InterceptorRegistry {
    private List<MappedInterceptor> mappedInterceptors = new ArrayList<>();

    /**
     * 注册一个拦截器到Registry
     * @param interceptor
     * @return
     */
    public MappedInterceptor addInterceptor(HandlerInterceptor interceptor) {
        MappedInterceptor mappedInterceptor = new MappedInterceptor(interceptor);
        mappedInterceptors.add(mappedInterceptor);
        return mappedInterceptor;
    }

    public List<MappedInterceptor> getMappedInterceptors() {
        return mappedInterceptors;
    }
}

```

#### 4.2 单元测试
到此我们拦截器的功能都开发完，虽然简单，但是我们还是需要做一些单元测试，测试用例：
1. 创建一个拦截器的实现，能够正常的注册到`InterceptorRegistry`
2. 能够为注册的拦截器设置支持URL和排除的URL
3. 测试拦截器的match方法是否正确

拦截器的实现类`TestHandlerInterceptor`

```
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
```


根据刚才写的单元测试用例编写单元测试：

```
public class HandlerInterceptorTest {

    private InterceptorRegistry interceptorRegistry = new InterceptorRegistry();

    @Test
    public void test() throws Exception {
        TestHandlerInterceptor interceptor = new TestHandlerInterceptor();

        interceptorRegistry.addInterceptor(interceptor)
                .addExcludePatterns("/ex_test")
                .addIncludePatterns("/in_test");

        List<MappedInterceptor> interceptors = interceptorRegistry.getMappedInterceptors();

        Assert.assertEquals(interceptors.size(), 1);

        MappedInterceptor mappedInterceptor = interceptors.get(0);

        Assert.assertTrue(mappedInterceptor.matches("/in_test"));
        Assert.assertFalse(mappedInterceptor.matches("/ex_test"));

        mappedInterceptor.preHandle(null, null, null);
        mappedInterceptor.postHandle(null, null, null, null);
        mappedInterceptor.afterCompletion(null, null, null, null);
    }

}

```

运行的结果：

![拦截器单元测试结果](https://image-static.segmentfault.com/282/379/2823792745-5fc36b8194b78_articlex)


#### 本节小结
本节我们完成了拦截器相关逻辑的开发，`HandlerInterceptor`中的`afterCompletion`方法不管什么情况下都会被执行

#### 延展
本节实现的拦截器和SpringMVC提供的拦截器有些许差别，功能也不如SpringMVC的强大，大家可以对比着看看，加深对SpringMVC拦截器的理解