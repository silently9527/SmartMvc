package com.silently9527.smartmvc.view;

import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RedirectView extends AbstractView {
    private String url;

    public RedirectView(String url) {
        this.url = url;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String targetUrl = createTargetUrl(model, request);
        response.sendRedirect(targetUrl);
    }

    /**
     * model中的数据添加到URL后面作为参数
     *
     * @param model
     * @param request
     * @return
     */
    private String createTargetUrl(Map<String, Object> model, HttpServletRequest request) {
        Assert.notNull(this.url, "url can not null");

        StringBuilder queryParams = new StringBuilder();
        model.forEach((key, value) -> {
            queryParams.append(key).append("=").append(value).append("&");
        });
        if (queryParams.length() > 0) {
            queryParams.deleteCharAt(queryParams.length() - 1);
        }
        StringBuilder targetUrl = new StringBuilder();
        if (this.url.startsWith("/")) {
            // Do not apply context path to relative URLs.
            targetUrl.append(getContextPath(request));
        }

        targetUrl.append(url);

        if (queryParams.length() > 0) {
            targetUrl.append("?").append(queryParams.toString());
        }
        return targetUrl.toString();
    }

    private String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        while (contextPath.startsWith("//")) {
            contextPath = contextPath.substring(1);
        }
        return contextPath;
    }

    public String getUrl() {
        return url;
    }
}
