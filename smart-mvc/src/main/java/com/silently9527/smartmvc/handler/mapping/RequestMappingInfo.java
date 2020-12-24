package com.silently9527.smartmvc.handler.mapping;

import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.http.RequestMethod;

public class RequestMappingInfo {
    private String path;
    private RequestMethod httpMethod;

    public RequestMappingInfo(String prefix, RequestMapping requestMapping) {
        this.path = prefix + requestMapping.path();
        this.httpMethod = requestMapping.method();
    }

    public String getPath() {
        return path;
    }

    public RequestMethod getHttpMethod() {
        return httpMethod;
    }

}
