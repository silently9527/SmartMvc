package com.silently9527.smartmvc.handler.argument;

import com.alibaba.fastjson.JSON;
import com.silently9527.smartmvc.annotation.RequestBody;
import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.exception.MissingServletRequestParameterException;
import com.silently9527.smartmvc.handler.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response,
                                  ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {
        String httpMessageBody = this.getHttpMessageBody(request);
        if (!StringUtils.isEmpty(httpMessageBody)) {
            return JSON.parseObject(httpMessageBody, parameter.getParameterType());
        }

        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        if (Objects.isNull(requestBody)) {
            return null;
        }
        if (requestBody.required()) {
            throw new MissingServletRequestParameterException(parameter.getParameterName(),
                    parameter.getParameterType().getName());
        }
        return null;
    }


    private String getHttpMessageBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        char[] buff = new char[1024];
        int len;
        while ((len = reader.read(buff)) != -1) {
            sb.append(buff, 0, len);
        }
        return sb.toString();
    }

}
