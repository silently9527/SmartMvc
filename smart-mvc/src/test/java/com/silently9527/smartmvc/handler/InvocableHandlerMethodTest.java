package com.silently9527.smartmvc.handler;

import com.alibaba.fastjson.JSON;
import com.silently9527.smartmvc.controller.TestInvocableHandlerMethodController;
import com.silently9527.smartmvc.handler.argument.HandlerMethodArgumentResolver;
import com.silently9527.smartmvc.handler.argument.HandlerMethodArgumentResolverComposite;
import com.silently9527.smartmvc.handler.argument.ModelMethodArgumentResolver;
import com.silently9527.smartmvc.handler.argument.ServletRequestMethodArgumentResolver;
import com.silently9527.smartmvc.handler.argument.ServletResponseMethodArgumentResolver;
import com.silently9527.smartmvc.handler.returnvalue.HandlerMethodReturnValueHandler;
import com.silently9527.smartmvc.handler.returnvalue.HandlerMethodReturnValueHandlerComposite;
import com.silently9527.smartmvc.handler.returnvalue.ModelMethodReturnValueHandler;
import com.silently9527.smartmvc.handler.returnvalue.ViewNameMethodReturnValueHandler;
import org.junit.Test;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

import static org.junit.Assert.*;

public class InvocableHandlerMethodTest {

    @Test
    public void test1() throws Exception {
        TestInvocableHandlerMethodController controller = new TestInvocableHandlerMethodController();

        Method method = controller.getClass().getMethod("testRequestAndResponse",
                HttpServletRequest.class, HttpServletResponse.class);

        HandlerMethod handlerMethod = new HandlerMethod(controller, method);
        HandlerMethodArgumentResolverComposite argumentResolver = new HandlerMethodArgumentResolverComposite();
        argumentResolver.addResolver(new ServletRequestMethodArgumentResolver());
        argumentResolver.addResolver(new ServletResponseMethodArgumentResolver());


        InvocableHandlerMethod inMethod = new InvocableHandlerMethod(handlerMethod, argumentResolver, null, null);

        ModelAndViewContainer mvContainer = new ModelAndViewContainer();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name", "Silently9527");
        MockHttpServletResponse response = new MockHttpServletResponse();

        inMethod.invokeAndHandle(request, response, mvContainer);

        System.out.println("输出到前端的内容:");
        System.out.println(response.getContentAsString());
    }

    @Test
    public void test2() throws Exception {
        TestInvocableHandlerMethodController controller = new TestInvocableHandlerMethodController();

        Method method = controller.getClass().getMethod("testViewName", Model.class);

        HandlerMethod handlerMethod = new HandlerMethod(controller, method);
        HandlerMethodArgumentResolverComposite argumentResolver = new HandlerMethodArgumentResolverComposite();
        argumentResolver.addResolver(new ModelMethodArgumentResolver());

        HandlerMethodReturnValueHandlerComposite returnValueHandler = new HandlerMethodReturnValueHandlerComposite();
        returnValueHandler.addReturnValueHandler(new ViewNameMethodReturnValueHandler());

        InvocableHandlerMethod inMethod = new InvocableHandlerMethod(handlerMethod, argumentResolver,
                returnValueHandler, null);

        ModelAndViewContainer mvContainer = new ModelAndViewContainer();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        inMethod.invokeAndHandle(request, response, mvContainer);

        System.out.println("ModelAndViewContainer:");
        System.out.println(JSON.toJSONString(mvContainer.getModel()));
        System.out.println("viewName: " + mvContainer.getViewName());
    }


}