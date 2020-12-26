# SmartMvc：手写简易版MVC框架

#### 简介
SpringMVC可以说的上是当前最优秀的MVC框架，采用了松散耦合可插拔组件结构，比其他MVC框架更具扩展性和灵活性；为了提高框架的扩展性和灵活性，
设计了松耦合可插拔的组件。理解SpringMVC的原理，在面试或工作中都十分的重要。

SpringMVC的原理在网络上到处都可以找得到，但是写的都很概括、零散；对应阅读源码经验较少的小伙伴来说，
自己去看源码被很多细节所干扰阻碍，不能够很好的抽离出springMVC原理的主线；所以自己想和小伙伴一起从手写简易版的SpringMVC框架出发，
理出SpringMVC的主线并深入理解SpringMVC的原理

> **别忘记Star哟**

#### 项目结构
```
SmartMvc
├── docs -- 开发文档
├── smart-mvc -- 实现mvc功能的核心代码
├── smartmvc-springboot-autoconfigure -- SmartMvc的自动化配置
├── smartmvc-springboot-demo -- SmartMvc的demo项目
├── smartmvc-springboot-starter -- SmartMvc的starter
└── spring-mvc-demo -- SpringMVC的demo
```

#### IDE、源码、依赖版本
- JDK的版本1.8
- 整个开发过程中我使用的IDE都是IDEA，可以根据读者自己习惯选择。当然我推荐是用IDEA
- 开发SmartMVC我们需要使用到Spring，我使用的版本`5.2.9`
- SmartMVC的源码地址：
    1. Github： [https://github.com/silently9527/SmartMvc](https://github.com/silently9527/SmartMvc) 
    2. 码云：[https://gitee.com/silently9527/SmartMvc](https://gitee.com/silently9527/SmartMvc)


#### 约定
- 为了便于后期理解和使用SpringMVC，所以在SmartMVC中所有组件的名称都和SpringMVC的保持一致
- 为了让SpringMVC的核心流程更加的清晰，减少读者的干扰，我拿出了自己18米的砍刀大胆的砍掉了SpringMVC中很多细节流程，
达到去枝干立主脑，让读者能够更加顺畅的理解整个流转的过程


#### 文档目录

文档备份地址：[https://silently9527.cn/archives/category/smartmvc](https://silently9527.cn/archives/category/smartmvc)

- [01 SmartMVC总体架构规划](https://silently9527.cn/archives/71)
- [02 RequestMappingHandlerMapping初始化过程](https://silently9527.cn/archives/72)
- [03 拦截器HandlerInterceptor](https://silently9527.cn/archives/73)
- [04 HandlerMapping获取对应的Handler](https://silently9527.cn/archives/74)
- [05 参数解析器HandlerMethodArgumentResolver](https://silently9527.cn/archives/75)
- [06 返回解析器HandlerMethodReturnValueHandler](https://silently9527.cn/archives/76)
- [07 Handler执行器InvocableHandlerMethod](https://silently9527.cn/archives/77)
- [08 实现RequestMappingHandlerAdapter](https://silently9527.cn/archives/78)
- [09 视图InternalResourceView、RedirectView](https://silently9527.cn/archives/79)
- [10 视图解析器ViewResolver](https://silently9527.cn/archives/80)
- [11 DispatcherServlet实现doDispatch来完成请求逻辑](https://silently9527.cn/archives/81)
- [12 全局异常处理器HandlerExceptionResolver](https://silently9527.cn/archives/82)
- [13 核心配置类WebMvcConfigurationSupport](https://silently9527.cn/archives/83)
- [14 SmartMvc与SpringBoot集成(一)](https://silently9527.cn/archives/84)
- [15 SmartMvc与SpringBoot集成(二)](https://silently9527.cn/archives/85)
- [16 SmartMvc项目实战](https://silently9527.cn/archives/86)