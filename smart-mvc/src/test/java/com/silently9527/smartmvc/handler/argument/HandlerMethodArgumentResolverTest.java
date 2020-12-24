package com.silently9527.smartmvc.handler.argument;

import com.alibaba.fastjson.JSON;
import com.silently9527.smartmvc.controller.TestController;
import com.silently9527.smartmvc.handler.HandlerMethod;
import com.silently9527.smartmvc.vo.UserVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Date;

public class HandlerMethodArgumentResolverTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Assert.assertFalse(response.isCommitted());
        PrintWriter writer = response.getWriter();
        writer.println("test");
        writer.close();
        Assert.assertTrue(response.isCommitted());
    }

    @Test
    public void test1() throws NoSuchMethodException {
        TestController testController = new TestController();
        Method method = testController.getClass().getMethod("test4",
                String.class, Integer.class, Date.class, HttpServletRequest.class);

        HandlerMethod handlerMethod = new HandlerMethod(testController, method);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name", "Silently9527");
        request.setParameter("age", "25");
        request.setParameter("birthday", "2020-11-12 13:00:00");

        HandlerMethodArgumentResolverComposite resolverComposite = new HandlerMethodArgumentResolverComposite();
        resolverComposite.addResolver(new RequestParamMethodArgumentResolver());
        resolverComposite.addResolver(new ServletRequestMethodArgumentResolver());

        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        DateFormatter dateFormatter = new DateFormatter();
        dateFormatter.setPattern("yyyy-MM-dd HH:mm:ss");
        conversionService.addFormatter(dateFormatter);

        MockHttpServletResponse response = new MockHttpServletResponse();

        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        handlerMethod.getParameters().forEach(methodParameter -> {
            try {
                methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
                Object value = resolverComposite.resolveArgument(methodParameter, request, response, null, conversionService);
                System.out.println(methodParameter.getParameterName() + " : " + value + "   type: " + value.getClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test2() throws NoSuchMethodException {
        TestController testController = new TestController();
        Method method = testController.getClass().getMethod("user", UserVo.class);

        HandlerMethod handlerMethod = new HandlerMethod(testController, method);

        MockHttpServletRequest request = new MockHttpServletRequest();
        UserVo userVo = new UserVo();
        userVo.setName("Silently9527");
        userVo.setAge(25);
        userVo.setBirthday(new Date());
        request.setContent(JSON.toJSONString(userVo).getBytes());

        HandlerMethodArgumentResolverComposite resolverComposite = new HandlerMethodArgumentResolverComposite();
        resolverComposite.addResolver(new RequestBodyMethodArgumentResolver());

        MockHttpServletResponse response = new MockHttpServletResponse();

        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        handlerMethod.getParameters().forEach(methodParameter -> {
            try {
                methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
                Object value = resolverComposite.resolveArgument(methodParameter, request, response, null, null);
                System.out.println(methodParameter.getParameterName() + " : " + value + "   type: " + value.getClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}