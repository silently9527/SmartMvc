SmartMVC基本都功能都已完成，接下来我们就来做一个SmartMVC的项目实战，测试下SmartMVC是否能正常的工作。


### SpringBoot项目中引入SmartMVC的步骤

#### 1. 新建一个SpringBoot项目，在pom.xml中加入SmartMVC的starter

```
<dependency>
    <groupId>com.silently9527</groupId>
    <artifactId>smartmvc-springboot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 2. 修改SpringBoot生成的启动类，指定SmartMVC的`ApplicationContextClass`

```
@SpringBootApplication
public class SmartmvcSpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SmartmvcSpringbootDemoApplication.class);
        application.setApplicationContextClass(ServletWebServerApplicationContext.class);
        application.run(args);
    }
}
```

### 功能验证

1. 验证重定向

```
//http://localhost:7979/user/redirect
@RequestMapping(path = "/redirect", method = RequestMethod.GET)
public String redirect() {
    return "redirect:http://silently9527.cn";
}
```


2. 自定义参数解析器

```
@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new MyHandlerMethodArgumentResolver());
    }
}
```

```
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyUserParam {

    String name();
}
```

```
public class MyHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MyUserParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request,
                                  HttpServletResponse response, ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        MyUserParam annotation = parameter.getParameterAnnotation(MyUserParam.class);
        String param = request.getParameter(annotation.name());
        String[] split = param.split(",");

        UserVo userVo = new UserVo();
        userVo.setName(split[0]);
        userVo.setAge(Integer.valueOf(split[1]));
        return userVo;
    }
}

```

在控制器中的使用自定义参数解析器

```

    //http://localhost:7979/user/build?user=silently9527,123
    @ResponseBody
    @RequestMapping(path = "/build", method = RequestMethod.GET)
    public UserVo build(@MyUserParam(name = "user") UserVo vo) {
        return vo;
    }

```

3. 验证RequestParam

```
//http://localhost:7979/user/get?userId=123
@ResponseBody
@RequestMapping(path = "/get", method = RequestMethod.GET)
public UserVo get(@RequestParam(name = "userId") Long userId) {
    UserVo userVo = new UserVo();
    userVo.setName(userId + "_silently9527");
    userVo.setAge(25);
    userVo.setBirthday(new Date());
    return userVo;
}
```







