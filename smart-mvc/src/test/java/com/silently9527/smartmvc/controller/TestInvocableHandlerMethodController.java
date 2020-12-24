package com.silently9527.smartmvc.controller;

import org.springframework.ui.Model;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class TestInvocableHandlerMethodController {


    public void testRequestAndResponse(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request can not null");
        Assert.notNull(response, "response can not null");
        try (PrintWriter writer = response.getWriter()) {
            String name = request.getParameter("name");
            writer.println("Hello InvocableHandlerMethod, params:" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String testViewName(Model model) {
        model.addAttribute("blogURL", "http://silently9527.cn");
        return "/silently9527.jsp";
    }


}
