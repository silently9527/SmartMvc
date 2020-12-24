package com.silently9527.smartmvc.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface View {

    default String getContentType() {
        return null;
    }

    void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
