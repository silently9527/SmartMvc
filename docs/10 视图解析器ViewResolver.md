在SpringMVC中ViewResolver组件会将viewName解析成View对象，View对象再调用render完成结果的渲染。
在上一篇已经完成了View的开发，本篇来完成ViewResolver研发。

![uml](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/309bc21a52834936aaa26f27229b932e~tplv-k3u1fbpfcp-watermark.image)

我们主要开发两个视图的解析器`InternalResourceViewResolver`和`ContentNegotiatingViewResolver`；


#### 开发步骤讲解

#### ViewResolver 

```
public interface ViewResolver {
    View resolveViewName(String viewName) throws Exception;
}
```
首先我们来定义视图解析器的接口`ViewResolver`，因为`ViewResolver`组件的作用是将viewName解析成View对象，所以参数是viewName，
处理完成后返回的对象是View


#### AbstractCachingViewResolver
因为启动一直一般会运行很长时间，很多用户都会请求同一个视图名称，为了避免每次都需要把viewName解析成View，所以我们需要做一层缓存，
当有一次成功解析了viewName之后我们把返回的`View`缓存起来，下次直接先从缓存中取

```
public abstract class AbstractCachingViewResolver implements ViewResolver {
    private final Object lock = new Object();
    private static final View UNRESOLVED_VIEW = (model, request, response) -> {
    };
    private Map<String, View> cachedViews = new HashMap<>();

    @Override
    public View resolveViewName(String viewName) throws Exception {
        View view = cachedViews.get(viewName);
        if (Objects.nonNull(view)) {
            return (view != UNRESOLVED_VIEW ? view : null);
        }

        synchronized (lock) {
            view = cachedViews.get(viewName);
            if (Objects.nonNull(view)) {
                return (view != UNRESOLVED_VIEW ? view : null);
            }

            view = createView(viewName);
            if (Objects.isNull(view)) {
                view = UNRESOLVED_VIEW;
            }
            cachedViews.put(viewName, view);
        }
        return (view != UNRESOLVED_VIEW ? view : null);
    }

    protected abstract View createView(String viewName);

}
```
1. 定义一个默认的空视图`UNRESOLVED_VIEW`，当通过`viewName`解析不到视图返回null时，把默认的视图放入到缓存中
2. 由于可能存在同一时刻多个用户请求到同一个视图，所以需要使用`synchronized`加锁
3. 如果缓存中获取到的视图是`UNRESOLVED_VIEW`，那么就返回null


#### UrlBasedViewResolver

```
public abstract class UrlBasedViewResolver extends AbstractCachingViewResolver {
    public static final String REDIRECT_URL_PREFIX = "redirect:";
    public static final String FORWARD_URL_PREFIX = "forward:";

    private String prefix = "";
    private String suffix = "";


    @Override
    protected View createView(String viewName) {
        if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
            String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
            return new RedirectView(redirectUrl);
        }

        if (viewName.startsWith(FORWARD_URL_PREFIX)) {
            String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length());
            return new InternalResourceView(forwardUrl);
        }

        return buildView(viewName);
    }

    protected abstract View buildView(String viewName);

    //getter setter省略
}
```
1. 当viewName以`redirect:`开头，那么返回`RedirectView`视图
2. 当viewName以`forward:`开头，那么返回`InternalResourceView`视图
3. 如果都不是，那么就执行模板方法`buildView`

#### InternalResourceViewResolver

```
public class InternalResourceViewResolver extends UrlBasedViewResolver {
    @Override
    protected View buildView(String viewName) {
        String url = getPrefix() + viewName + getSuffix();
        return new InternalResourceView(url);
    }
}
```
实现了`UrlBasedViewResolver`中的模板方法`buildView`，拼接了url的前缀和后缀，返回视图`InternalResourceView`

#### ContentNegotiatingViewResolver
视图协同器`ContentNegotiatingViewResolver`定义了所有`ViewResolver`以及默认支持的`View`，
当接收到用户的请求后根据头信息中的`Accept`匹配出最优的视图


```
public class ContentNegotiatingViewResolver implements ViewResolver, InitializingBean {
    private List<ViewResolver> viewResolvers;
    private List<View> defaultViews;

    @Override
    public View resolveViewName(String viewName) throws Exception {
        List<View> candidateViews = getCandidateViews(viewName);
        View bestView = getBestView(candidateViews);
        if(Objects.nonNull(bestView)){
            return bestView;
        }
        return null;
    }

    /**
     * 根据请求找出最优视图
     *
     * @param candidateViews
     * @return
     */
    private View getBestView(List<View> candidateViews) {
        Optional<View> viewOptional = candidateViews.stream()
                .filter(view -> view instanceof RedirectView)
                .findAny();
        if (viewOptional.isPresent()) {
            return viewOptional.get();
        }

        HttpServletRequest request = RequestContextHolder.getRequest();
        Enumeration<String> acceptHeaders = request.getHeaders("Accept");
        while (acceptHeaders.hasMoreElements()) {
            for (View view : candidateViews) {
                if (acceptHeaders.nextElement().equals(view.getContentType())) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * 先找出所有候选视图
     *
     * @param viewName
     * @return
     * @throws Exception
     */
    private List<View> getCandidateViews(String viewName) throws Exception {
        List<View> candidateViews = new ArrayList<>();
        for (ViewResolver viewResolver : viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (Objects.nonNull(view)) {
                candidateViews.add(view);
            }
        }
        if (!CollectionUtils.isEmpty(defaultViews)) {
            candidateViews.addAll(defaultViews);
        }
        return candidateViews;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(viewResolvers, "viewResolvers can not null");
    }

    //getter setter 省略
}

```

1. getCandidateViews: 通过视图名字使用`ViewResolver`解析出所有不为null的视图，如果默认视图不为空，把所有视图返回作为候选视图
2. getBestView: 从request中拿出头信息`Accept`，根据视图的ContentType从候选视图中匹配出最优的视图返回

在这里我们还使用到了一个工具类`RequestContextHolder`，在当前线程中存放了当前请求的`HttpServletRequest`

```
public abstract class RequestContextHolder {
    private static final ThreadLocal<HttpServletRequest> inheritableRequestHolder =
            new NamedInheritableThreadLocal<>("Request context");

    /**
     * Reset the HttpServletRequest for the current thread.
     */
    public static void resetRequest() {
        inheritableRequestHolder.remove();
    }

    public static void setRequest(HttpServletRequest request) {
        inheritableRequestHolder.set(request);
    }

    public static HttpServletRequest getRequest() {
        return inheritableRequestHolder.get();
    }
}
```

#### 单元测试
到此本篇所有的视图解析器都已经完成，本篇的单元测试我们主要测试`ContentNegotiatingViewResolver`，检查能否正确的返回视图对象


```
@Test
public void resolveViewName() throws Exception {
    ContentNegotiatingViewResolver negotiatingViewResolver = new ContentNegotiatingViewResolver();
    negotiatingViewResolver.setViewResolvers(Collections.singletonList(new InternalResourceViewResolver()));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Accept", "text/html");
    RequestContextHolder.setRequest(request);

    View redirectView = negotiatingViewResolver.resolveViewName("redirect:/silently9527.cn");
    Assert.assertTrue(redirectView instanceof RedirectView); //判断是否返回重定向视图

    View forwardView = negotiatingViewResolver.resolveViewName("forward:/silently9527.cn");
    Assert.assertTrue(forwardView instanceof InternalResourceView); //

    View view = negotiatingViewResolver.resolveViewName("/silently9527.cn");
    Assert.assertTrue(view instanceof InternalResourceView); //通过头信息`Accept`，判断是否返回的`InternalResourceView`

}
```

执行的结果如下：

![result](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/338a80ee712d4e589f3b5e4175c1dfd4~tplv-k3u1fbpfcp-watermark.image)

#### 总结
本篇我们完成了`ViewResolver`，相信大家对springmvc的视图解析过程也有了一定的了解，下篇我们将开始研发`DispatcherServlet`，
把我们之前开发完成的HandlerMapping、HandlerAdapter等组件串联起来使用。

#### 延展
本篇完成后可以对照着去springmvc中的视图解析器，比如：`ContentNegotiatingViewResolver`、`BeanNameViewResolver`、
`XmlViewResolver`等，特别是`ContentNegotiatingViewResolver`，我们自己实现的是简版，springmvc的支持头信息，url后缀等方法。