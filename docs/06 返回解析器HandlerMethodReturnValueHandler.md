前一篇我们开发完成了参数的解析器，接下来我们开始开发返回值的处理器，在SpringMVC中已经内部实现了很多的返回值处理器，
我们这里不可能实现那么多，我挑选了5个常用的返回值处理器来作为本篇的开发内容，首先我们一起来看下类图

![uml](https://image-static.segmentfault.com/346/351/3463514123-5fcc422c47bf0_articlex)

本篇我们主要实现5个功能，这也是SpringMVC中常用的功能：
- `Map`: 支持Handler返回Map值，放入到上下文中，用于页面渲染使用
- `Model`: 支持Handler返回Model值，放入到上下文中，用于页面渲染使用
- `View`: 支持Handler直接返回需要渲染的`View`对象
- `viewName`: 支持返回一个String对象，表示视图的路径
- `@ResponseBody`: 如果方法上被注解`@ResponseBody`标注，那么返回JSON字符串

#### 开发步骤讲解

##### HandlerMethodReturnValueHandler

```
public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                           ModelAndViewContainer mavContainer,
                           HttpServletRequest request, HttpServletResponse response) throws Exception;
}
```
- supportsReturnType: 同参数解析器一样，判断处理器是否支持该返回值的类型
- handleReturnValue: returnValue(Handler执行之后的返回值)；该方法还需要传入`HttpServletResponse`对象，
是因为可能会在处理其中直接处理完整个请求，比如`@ResponseBody`


##### MapMethodReturnValueHandler 、 ModelMethodReturnValueHandler
先来看两个简单的实现，支持返回Map，Model

```
public class ModelMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Model.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (returnValue == null) {
            return;
        } else if (returnValue instanceof Model) {
            mavContainer.getModel().addAllAttributes(((Model) returnValue).asMap());
        } else {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }
}
```

```
public class MapMethodReturnValueHandler implements HandlerMethodReturnValueHandler {
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Map.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (returnValue instanceof Map) {
            mavContainer.getModel().addAllAttributes((Map) returnValue);
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }
}

```
在上一篇我们已经说到`ModelAndViewContainer`，它是一个ModelAndView的容器，每个请求都会新建一个对象，
它会贯穿整个Handler执行前的参数解析、执行以及返回值处理；这两个类的实现主要都是将Handler的返回值添加知道Model中，
用于后面构建`ModeAndView`对象以及实现渲染

##### ViewNameMethodReturnValueHandler、ViewMethodReturnValueHandler
刚才上面两个处理器主要负责的是`ModelAndView`中的Model部分，接下来我们要实现的是负责View部分；
由于本篇我们需要使用到View对象，所以我们需要先建了View的解析，只是不去实现，方便我们现在引用

```
public interface View {
}
```

接下来我们看看支持Handler返回ViewName和View的实现

```
public class ViewNameMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return CharSequence.class.isAssignableFrom(paramType);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (returnValue instanceof CharSequence) {
            String viewName = returnValue.toString();
            mavContainer.setViewName(viewName);
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }
}

```

```
public class ViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return View.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (returnValue instanceof View) {
            View view = (View) returnValue;
            mavContainer.setView(view);
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

}
```
`ViewNameMethodReturnValueHandler`：如果返回值是String，那么把这个返回值当做是视图的名字，放入到`ModelAndViewContainer`中
`ViewMethodReturnValueHandler`：如果返回值是View对象，那么直接把视图放入到`ModelAndViewContainer`中

##### ResponseBodyMethodReturnValueHandler
当方法或者Controller被注解`@ResponseBody`标注时，返回值需要被转换成JSON字符串输出

定义注解`@ResponseBody`

```
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
```

```
public class ResponseBodyMethodReturnValueHandler implements HandlerMethodReturnValueHandler {
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class));
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //标记本次请求已经处理完成
        mavContainer.setRequestHandled(true);

        outPutMessage(response, JSON.toJSONString(returnValue));
    }

    private void outPutMessage(HttpServletResponse response, String message) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(message);
        }
    }

}
```
1. `mavContainer.setRequestHandled(true);`标记出当前请求已经处理完成，后续的渲染无需在执行
2. 使用fastJson把返回值转换成JSON字符串，在使用response输出给前端

##### HandlerMethodReturnValueHandlerComposite

与参数解析器一样，这里也需要一个返回值处理器的聚合类

```
public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {
    private List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if (handler.supportsReturnType(returnType)) {
                handler.handleReturnValue(returnValue, returnType, mavContainer, request, response);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported parameter type [" +
                returnType.getParameterType().getName() + "]. supportsParameter should be called first.");
    }

    public void clear() {
        this.returnValueHandlers.clear();
    }

    public void addReturnValueHandler(HandlerMethodReturnValueHandler... handlers) {
        Collections.addAll(this.returnValueHandlers, handlers);
    }
}

```

#### 单元测试
到此所有的开发工作都已完成，接着继续我们的单元测试
要测试这5中处理器是否正常工作，我们需要建一个`TestReturnValueController`

```
public class TestReturnValueController {

    @ResponseBody
    public UserVo testResponseBody() {
        UserVo userVo = new UserVo();
        userVo.setBirthday(new Date());
        userVo.setAge(20);
        userVo.setName("Silently9527");
        return userVo;
    }

    public String testViewName() {
        return "/jsp/index.jsp";
    }

    public View testView() {
        return new View() {
        };
    }

    public Model testModel(Model model) {
        model.addAttribute("testModel", "Silently9527");
        return model;
    }

    public Map<String, Object> testMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("testMap", "Silently9527");
        return params;
    }

}
```

在创建单元测试类之前，我们先看看`MethodParameter`中的一个构造方法

```
/**
 * Create a new {@code MethodParameter} for the given method, with nesting level 1.
 * @param method the Method to specify a parameter for
 * @param parameterIndex the index of the parameter: -1 for the method
 * return type; 0 for the first method parameter; 1 for the second method
 * parameter, etc.
 */
public MethodParameter(Method method, int parameterIndex) {
	this(method, parameterIndex, 1);
}
```
从构造方法的注释我可以了解到，当`parameterIndex`等于-1的时候，表示构造方法返回值的`MethodParameter`；

单元测试类如下：

```
@Test
public void test() throws Exception {
    HandlerMethodReturnValueHandlerComposite composite = new HandlerMethodReturnValueHandlerComposite();
    composite.addReturnValueHandler(new ModelMethodReturnValueHandler());
    composite.addReturnValueHandler(new MapMethodReturnValueHandler());
    composite.addReturnValueHandler(new ResponseBodyMethodReturnValueHandler());
    composite.addReturnValueHandler(new ViewMethodReturnValueHandler());
    composite.addReturnValueHandler(new ViewNameMethodReturnValueHandler());

    ModelAndViewContainer mvContainer = new ModelAndViewContainer();
    TestReturnValueController controller = new TestReturnValueController();

    //测试方法testViewName
    Method viewNameMethod = controller.getClass().getMethod("testViewName");
    MethodParameter viewNameMethodParameter = new MethodParameter(viewNameMethod, -1); //取得返回值的MethodParameter
    composite.handleReturnValue(controller.testViewName(), viewNameMethodParameter, mvContainer, null, null);
    Assert.assertEquals(mvContainer.getViewName(), "/jsp/index.jsp");

    //测试方法testView
    Method viewMethod = controller.getClass().getMethod("testView");
    MethodParameter viewMethodParameter = new MethodParameter(viewMethod, -1);
    composite.handleReturnValue(controller.testView(), viewMethodParameter, mvContainer, null, null);
    Assert.assertTrue(mvContainer.getView() instanceof View);

    //测试方法testResponseBody
    Method responseBodyMethod = controller.getClass().getMethod("testResponseBody");
    MethodParameter resBodyMethodParameter = new MethodParameter(responseBodyMethod, -1);
    MockHttpServletResponse response = new MockHttpServletResponse();
    composite.handleReturnValue(controller.testResponseBody(), resBodyMethodParameter, mvContainer, null, response);
    System.out.println(response.getContentAsString()); //打印出Controller中返回的JSON字符串

    //测试方法testModel
    Method modelMethod = controller.getClass().getMethod("testModel", Model.class);
    MethodParameter modelMethodParameter = new MethodParameter(modelMethod, -1);
    composite.handleReturnValue(controller.testModel(mvContainer.getModel()), modelMethodParameter, mvContainer, null, null);
    Assert.assertEquals(mvContainer.getModel().getAttribute("testModel"), "Silently9527");

    //测试方法testMap
    Method mapMethod = controller.getClass().getMethod("testMap");
    MethodParameter mapMethodParameter = new MethodParameter(mapMethod, -1);
    composite.handleReturnValue(controller.testMap(), mapMethodParameter, mvContainer, null, null);
    Assert.assertEquals(mvContainer.getModel().getAttribute("testMap"), "Silently9527");
}
```

单元测试输出的结果：

![result](https://image-static.segmentfault.com/381/542/3815428556-5fcc56d51bf27_articlex)


#### 总结

本篇我们完成了5个常用返回值的解析器，支持Handler返回`Map`、` Modle`、 `View`、 `ViewName`以及被`@ResponseBody`标注；
下一节我们将会开发`HandlerAdapter`中使用到的最后一个组件，完成之后就可以把所有的组件组装起来完成Handler的调用过程

#### 延展
大家可以对应的去看看SpringMVC中`HandlerMethodReturnValueHandler`的实现类，了解SpringMVC支持哪些返回值处理