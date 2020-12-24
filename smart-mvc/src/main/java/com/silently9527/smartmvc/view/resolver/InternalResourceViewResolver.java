package com.silently9527.smartmvc.view.resolver;

import com.silently9527.smartmvc.view.InternalResourceView;
import com.silently9527.smartmvc.view.View;

public class InternalResourceViewResolver extends UrlBasedViewResolver {
    @Override
    protected View buildView(String viewName) {
        String url = getPrefix() + viewName + getSuffix();
        return new InternalResourceView(url);
    }
}
