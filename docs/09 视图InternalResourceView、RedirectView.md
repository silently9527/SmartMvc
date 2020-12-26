上一篇我们结束了`HandlerAdapter`各个组件的开发任务，本篇我们将开始研发视图的渲染；先看看类图

![uml](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/71c3c04a7b9941e6970489ac1fa3c0ab~tplv-k3u1fbpfcp-watermark.image)

本篇我们先完成jsp视图的渲染以及重定向视图的渲染；

#### 开发步骤讲解

##### View

```
public interface View {
    default String getContentType() {
        return null;
    }
    void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
```
首先我们开定义出视图的接口`View`
- getContentType: 控制视图支持的ContentType是什么，默认是返回空
- render: 通过response把model中的数据渲染成视图返回给用户

##### AbstractView
因为视图可以有很多的实现类，比如：JSON、JSP、HTML、各类模板等等，所以我们定义一个抽象类`AbstractView`，通过模板方法定义出渲染的基本流程

```
public abstract class AbstractView implements View {

    @Override
    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.prepareResponse(request, response);
        this.renderMergedOutputModel(model, request, response);
    }

    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
    }

    protected abstract void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
```

- prepareResponse: 在实施渲染之前需要做的一些工作放入到这个方法中，比如：设置响应的头信息
- renderMergedOutputModel: 执行渲染的逻辑都将放入到这个方法中

##### RedirectView

当在控制器中返回的视图名是以`redirect:`开头的都将视为重定向视图；

```
public class RedirectView extends AbstractView {
    private String url;

    public RedirectView(String url) {
        this.url = url;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String targetUrl = createTargetUrl(model, request);
        response.sendRedirect(targetUrl);
    }

    /**
     * model中的数据添加到URL后面作为参数
     *
     * @param model
     * @param request
     * @return
     */
    private String createTargetUrl(Map<String, Object> model, HttpServletRequest request) {
        Assert.notNull(this.url, "url can not null");

        StringBuilder queryParams = new StringBuilder();
        model.forEach((key, value) -> {
            queryParams.append(key).append("=").append(value).append("&");
        });
        if (queryParams.length() > 0) {
            queryParams.deleteCharAt(queryParams.length() - 1);
        }
        StringBuilder targetUrl = new StringBuilder();
        if (this.url.startsWith("/")) {
            // Do not apply context path to relative URLs.
            targetUrl.append(getContextPath(request));
        }

        targetUrl.append(url);

        if (queryParams.length() > 0) {
            targetUrl.append("?").append(queryParams.toString());
        }
        return targetUrl.toString();
    }

    private String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        while (contextPath.startsWith("//")) {
            contextPath = contextPath.substring(1);
        }
        return contextPath;
    }

    public String getUrl() {
        return url;
    }
}
```

1. 重定向视图需要继承于`AbstractView`
2. 定义url，表示重定向的地址，实际也就是控制器中返回的视图名截取`redirect:`之后的字符串
3. createTargetUrl: 根据url拼接出重定向的地址，如果有设置`contentPath`，需要把`contentPath`拼接到链接的前面；如果Model中有属性值，需要把model中的属性值拼接到链接后面



##### InternalResourceView

```
public class InternalResourceView extends AbstractView {
    private String url;

    public InternalResourceView(String url) {
        this.url = url;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        exposeModelAsRequestAttributes(model, request);

        RequestDispatcher rd = request.getRequestDispatcher(this.url);
        rd.forward(request, response);
    }

    /**
     * 把model中的数据放入到request
     *
     * @param model
     * @param request
     */
    private void exposeModelAsRequestAttributes(Map<String, Object> model, HttpServletRequest request) {
        model.forEach((name, value) -> {
            if (Objects.nonNull(value)) {
                request.setAttribute(name, value);
            } else {
                request.removeAttribute(name);
            }
        });
    }

    public String getUrl() {
        return url;
    }
}
```
1. `InternalResourceView`需要支持JSP、HTML的渲染
2. url: 表示JSP文件的路径
3. exposeModelAsRequestAttributes: 该方法把Model中的数据全部设置到了request中，方便在JSP中通过el表达式取值

#### 11.2 单元测试

本次单元测试我们先只测试`RedirectView`，`InternalResourceView`放在后面整体测试；

```
@Test
public void test() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/path");

    MockHttpServletResponse response = new MockHttpServletResponse();

    Map<String, Object> model = new HashMap<>();
    model.put("name", "silently9527");
    model.put("url", "http://silently9527.cn");

    RedirectView redirectView = new RedirectView("/redirect/login");
    redirectView.render(model, request, response);

    response.getHeaderNames().forEach(headerName ->
            System.out.println(headerName + ":" + response.getHeader(headerName)));
}
```

1. 检查重定向地址是否有拼接上ContextPath
2. 检查重定向地址是否有拼接上model中的数据

输出结果：

![result](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fc647a07ac884d6b9f990c1a1ffc900d~tplv-k3u1fbpfcp-watermark.image)

#### 总结
本篇我们完成了`RedirectView`、`InternalResourceView`视图，后期通过自定义视图的方式实现excel视图；
下一篇我们将开始开发视图的解析器`ViewResolver`


#### 延展
springMVC中的视图`MappingJackson2JsonView`、`FreeMarkerView`、`MustacheView`实现逻辑类似，可以对照着看看源码