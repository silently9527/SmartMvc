
<p align="center">
  <a href="#微信公众号"><img src="https://img.shields.io/badge/公众号-贝塔学JAVA-blue.svg" alt="公众号"></a>
  <a href="https://juejin.cn/user/2779199782521693"><img src="https://img.shields.io/badge/juejin-掘金-yellow.svg" alt="掘金"></a>
  <a href="https://blog.csdn.net/asdewq380303318"><img src="https://img.shields.io/badge/csdn-CSDN-red.svg" alt="CSDN"></a>
  <a href="https://my.oschina.net/u/3230120"><img src="https://img.shields.io/badge/oschina-开源中国-green" alt="开源中国"></a>
  <a href="https://www.zhihu.com/people/huaan9527-57/posts"><img src="https://img.shields.io/badge/zhihu-知乎-purple" alt="知乎"></a>
</p>

# SmartMvc：手写简易版MVC框架

#### 简介
SpringMVC可以说的上是当前最优秀的MVC框架，采用了松散耦合可插拔组件结构，比其他MVC框架更具扩展性和灵活性；为了提高框架的扩展性和灵活性，
设计了松耦合可插拔的组件。理解SpringMVC的原理，在面试或工作中都十分的重要。

SpringMVC的原理在网络上到处都可以找得到，但是写的都很概括、零散；对应阅读源码经验较少的人来说，
自己去看源码被很多细节所干扰阻碍，不能够很好的抽离出springMVC原理的主线；所以自己从手写简易版的SpringMVC框架出发，
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


#### 文档目录 [https://herman7z.site/Notes/No1MyProjects/](https://herman7z.site/Notes/No1MyProjects/)
- 00 深入解析SpringMVC核心原理：从手写简易版MVC框架开始(SmartMvc)
- 01 SmartMVC总体架构规划
- 02 RequestMappingHandlerMapping初始化过程
- 03 拦截器HandlerInterceptor
- 04 HandlerMapping获取对应的Handler
- 05 参数解析器HandlerMethodArgumentResolver
- 06 返回解析器HandlerMethodReturnValueHandler
- 07 Handler执行器InvocableHandlerMethod
- 08 实现RequestMappingHandlerAdapter
- 09 视图InternalResourceView、RedirectView
- 10 视图解析器ViewResolver
- 11 DispatcherServlet实现doDispatch来完成请求逻辑
- 12 全局异常处理器HandlerExceptionResolver
- 13 核心配置类WebMvcConfigurationSupport
- 14 SmartMvc与SpringBoot集成(一)
- 15 SmartMvc与SpringBoot集成(二)
- 16 SmartMvc项目实战


## 其他项目推荐
* [前后端完全开源高颜值淘客APP](https://github.com/silently9527/coupons)
* [Idea工具箱插件](https://github.com/silently9527/Toolkit)
* [Java程序员自我学习的书单](https://github.com/silently9527/ProgrammerBooks)
* [技术文章以及代码收录仓库](https://github.com/silently9527/ProgrammerNotes)


### 博客地址: [https://herman7z.site](https://herman7z.site)

### 知识星球: [Herman's Notes](https://t.zsxq.com/h2EIR)
![](https://raw.githubusercontent.com/silently9527/images/main/202408151725390.png)
