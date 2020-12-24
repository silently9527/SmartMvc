package com.silently9527.smartmvc.view.resolver;

import com.silently9527.smartmvc.view.View;

public interface ViewResolver {
    View resolveViewName(String viewName) throws Exception;
}
