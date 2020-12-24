package com.silently9527.smartmvc.utils;

import org.springframework.core.NamedInheritableThreadLocal;

import javax.servlet.http.HttpServletRequest;

public abstract class RequestContextHolder {
    private static final ThreadLocal<HttpServletRequest> inheritableRequestHolder =
            new NamedInheritableThreadLocal<>("Request context");

    /**
     * Reset the HttpServletRequest for the current thread.
     */
    public static void resetRequest() {
        inheritableRequestHolder.remove();
    }

    public static void setRequest(HttpServletRequest request) {
        inheritableRequestHolder.set(request);
    }

    public static HttpServletRequest getRequest() {
        return inheritableRequestHolder.get();
    }
}
