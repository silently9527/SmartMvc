package com.silently9527.smartmvc.handler.mapping;

import com.silently9527.smartmvc.handler.HandlerExecutionChain;

import javax.servlet.http.HttpServletRequest;

public interface HandlerMapping {

    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
