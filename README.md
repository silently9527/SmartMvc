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

文档备份地址：[https://silently9527.cn/categories/smartmvc](https://silently9527.cn/categories/smartmvc)

- [01 SmartMVC总体架构规划](https://silently9527.cn/archives/smartmvc%E6%95%B4%E4%BD%93%E8%A7%84%E5%88%92)
- [02 RequestMappingHandlerMapping初始化过程](https://silently9527.cn/archives/02handlermapping-%E5%88%9D%E5%A7%8B%E5%8C%96%E8%BF%87%E7%A8%8B)
- [03 拦截器HandlerInterceptor](https://silently9527.cn/archives/03handlermapping-%E6%8B%A6%E6%88%AA%E5%99%A8)
- [04 HandlerMapping获取对应的Handler](https://silently9527.cn/archives/04handlermapping-%E9%80%9A%E8%BF%87request%E8%8E%B7%E5%8F%96handler)
- [05 参数解析器HandlerMethodArgumentResolver](https://silently9527.cn/archives/05handleradapter-%E5%8F%82%E6%95%B0%E8%A7%A3%E6%9E%90%E5%99%A8handlermethodargumentresolver)
- [06 返回解析器HandlerMethodReturnValueHandler](https://silently9527.cn/archives/06handleradapter-%E8%BF%94%E5%9B%9E%E8%A7%A3%E6%9E%90%E5%99%A8handlermethodreturnvaluehandler)
- [07 Handler执行器InvocableHandlerMethod](https://silently9527.cn/archives/07handleradapter-handler%E6%89%A7%E8%A1%8C%E5%99%A8invocablehandlermethod)
- [08 实现RequestMappingHandlerAdapter](https://silently9527.cn/archives/08handleradapter-%E5%AE%9E%E7%8E%B0requestmappinghandleradapter)
- [09 视图InternalResourceView、RedirectView](https://silently9527.cn/archives/09view-jsp%E8%A7%86%E5%9B%BEinternalresourceviewredirectview)
- [10 视图解析器ViewResolver](https://silently9527.cn/archives/10viewresolver-%E8%A7%86%E5%9B%BE%E8%A7%A3%E6%9E%90%E5%99%A8)
- [11 DispatcherServlet实现doDispatch来完成请求逻辑](https://silently9527.cn/archives/11dispatcherservlet-%E5%AE%9E%E7%8E%B0dodispatch%E5%AE%8C%E6%88%90%E8%AF%B7%E6%B1%82%E9%80%BB%E8%BE%91)
- [12 全局异常处理器HandlerExceptionResolver](https://silently9527.cn/archives/12handlerexceptionresolver%E5%85%A8%E5%B1%80%E5%BC%82%E5%B8%B8%E5%A4%84%E7%90%86%E5%99%A8)
- [13 核心配置类WebMvcConfigurationSupport](https://silently9527.cn/archives/13webmvcconfigurationsupport-%E6%A0%B8%E5%BF%83%E9%85%8D%E7%BD%AE%E7%B1%BB)
- [14 SmartMvc与SpringBoot集成(一)](https://silently9527.cn/archives/14smartmvc%E4%B8%8Espringboot%E9%9B%86%E6%88%90%E4%B8%80)
- [15 SmartMvc与SpringBoot集成(二)](https://silently9527.cn/archives/15smartmvc-yu-springboot-ji-cheng--er-)
- [16 SmartMvc项目实战](https://silently9527.cn/archives/16smartmvc-xiang-mu-shi-zhan)


#### 期待你的加入

<img src="http://cdn.silently9527.cn/weixhao_gongzonghao_1629032267170.jpg?imageView2/1/w/400/h/400" alt="公众号">


## 我的技术博客
[https://silently9527.cn/](https://silently9527.cn/)

## 其他项目推荐
* [前后端完全开源高颜值淘客APP](https://github.com/silently9527/coupons)
* [深入解析SpringMVC核心原理：从手写简易版MVC框架开始(SmartMvc)](https://github.com/silently9527/SmartMvc)
* [Java程序员自我学习的书单](https://github.com/silently9527/ProgrammerBooks)
* [技术文章以及代码收录仓库](https://github.com/silently9527/ProgrammerNotes)
* [高颜值可定制化的简介导航网站](http://nav.silently9527.cn/)
