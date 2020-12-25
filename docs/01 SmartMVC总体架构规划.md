#### SpringMVC为何如此重要
SpringMVC可以说的上是当前最优秀的mvc框架，自从Spring 2.5版本发布后，由于支持注解配置，易用性有了大幅度的提高。
Spring3.0之后功能更加完善，在工作的这几年中，几乎所有的项目都使用的都是SpringMVC，SpringMVC已经完全超越了Struts2

SpringMVC通过一套MVC注解，让POJO成为处理请求的控制器，而无须实现任何接口。
可以轻松实现支持REST风格的URL请求
采用了松散耦合可插拔组件结构，比其他MVC框架更具扩展性和灵活性
为了提高框架的扩展性和灵活性，设计了松耦合可插拔的组件发理解SpringMVC的原理

#### SmartMVC总体架构
在开始撸代码之前我们需要先设计好整个框架架构，所有的设计都应该是先整体后局部的思想，如果上来就卷起袖子干，不经过仔细的设计是干不好框架的，
所以我们先画出SmartMVC的设计图，熟悉SpringMVC的小伙伴可能看出来了，这个流程和SpringMVC的一致。
![SmartMVC原理图](https://image-static.segmentfault.com/228/772/2287721208-5fbfce05acfeb_articlex)

SpringMVC之所以如此的受欢迎，其中很重要的一个原因是轻耦合可插拔的组件设计，提供很好的扩展性和灵活性。
虽然我们即将要做的SmartMVC是SpringMVC的浓缩版本，但是SpringMVC有的核心组件我们也必须的有，当我们把每个核心组件都个个击破之后相信对SpringMVC
的原理也有了清晰的认识

#### 研发流程
首先我们需要先开发`HandlerMapping`，`HandlerMapping`的主要作用是根据请求的url查找`Handler`，其中涉及到的组件有
1. HandlerMethod
2. MappingRegistry
3. HandlerInterceptor
4. RequestMappingHandlerMapping

其实是开发`HandlerAdapter`，`HandlerAdapter`的主要作用是按照特定规则（`HandlerAdapter`要求的规则）去执行`Handler`，其中涉及到的组件有
1. HandlerMethodArgumentResolver
2. HandlerMethodReturnValueHandler
3. ModelAndView
4. InvocableHandlerMethod
5. RequestMappingHandlerAdapter

> 可能大家对于`Handler`，`HandlerMethod`，`HandlerMapping`，`HandlerAdapter`有疑惑，到底有啥区别？
可以这样理解：Handler是用来干活的工具；HandlerMapping用于根据URL找到相应的干活工具；HandlerAdapter是使用工具干活的人；在SpringMVC中Handler是一个抽象的统称，HandlerMethod只代表一种Handler

然后是开发`ViewResolver`、`View`，`ViewResolver`负责根据返回的`ModeAndView`查到对应的`View`，`View`负责渲染出视图返回给客户端，其中涉及到的组件有
1. InternalResourceViewResolver
2. InternalResourceView
3. RedirectView

最后我来开发`DispatcherServlet`，负责把所有的组件都组装起来统一调度，它是整个流程控制的中心，控制其它组件执行；

到此基本SpringMVC的核心流程大部分都已实现，为了让我们的SmartMVC更加完善，我们还需要实现全局异常处理器`HandlerExceptionResolver`、静态资源映射;
为了让SmartMVC的使用更加方便，我们还需要实现核心配置类`WebMvcConfigurationSupport`、`EnableWebMvc`；
方便和SpringBoot的集成，需要开发一个SpringBoot的starter


#### 项目搭建

SmartMvc项目中的pom.xml依赖引入

```
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
        <version>5.2.9.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>5.2.9.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.2.9.RELEASE</version>
    </dependency>
</dependencies>
```

smart-mvc-parent项目中pom.xml配置

```
 <dependencies>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-test</artifactId>
        <version>5.2.9.RELEASE</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>nexus-aliyun</id>
        <name>Nexus aliyun</name>
        <layout>default</layout>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository>
</repositories>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```


#### 搭建单元测试环境
建立如下的目录结构

![测试目录结构](https://image-static.segmentfault.com/105/793/1057935185-5fc2523435782_articlex)

创建JavaConfig配置主类`AppConfig`

```
@Configuration
@ComponentScan(basePackages = "com.silently9527.smartmvc")
public class AppConfig {

}
```

创建单元测试基类，主要是配置Spring的测试环境，方便后期开发单元测试

```
@RunWith(SpringJUnit4ClassRunner.class)  // Junit提供的扩展接口，这里指定使用SpringJUnit4ClassRunner作为Junit测试环境
@ContextConfiguration(classes = AppConfig.class)  // 加载配置文件
public class BaseJunit4Test {
}
```

