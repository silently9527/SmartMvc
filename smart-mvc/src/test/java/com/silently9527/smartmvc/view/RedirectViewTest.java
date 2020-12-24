package com.silently9527.smartmvc.view;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public class RedirectViewTest {

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

}