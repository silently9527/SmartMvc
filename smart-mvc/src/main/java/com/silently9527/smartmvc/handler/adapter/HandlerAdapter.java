package com.silently9527.smartmvc.handler.adapter;

import com.silently9527.smartmvc.ModelAndView;
import com.silently9527.smartmvc.handler.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
                        HandlerMethod handler) throws Exception;
}
