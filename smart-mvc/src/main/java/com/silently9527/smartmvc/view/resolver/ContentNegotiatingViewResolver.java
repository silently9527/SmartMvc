package com.silently9527.smartmvc.view.resolver;

import com.silently9527.smartmvc.utils.RequestContextHolder;
import com.silently9527.smartmvc.view.RedirectView;
import com.silently9527.smartmvc.view.View;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ContentNegotiatingViewResolver implements ViewResolver, InitializingBean {
    private List<ViewResolver> viewResolvers = new ArrayList<>();
    private List<View> defaultViews = new ArrayList<>();

    @Override
    public View resolveViewName(String viewName) throws Exception {
        List<View> candidateViews = getCandidateViews(viewName);
        View bestView = getBestView(candidateViews);
        if(Objects.nonNull(bestView)){
            return bestView;
        }
        return null;
    }

    /**
     * 根据请求找出最优视图
     *
     * @param candidateViews
     * @return
     */
    private View getBestView(List<View> candidateViews) {
        Optional<View> viewOptional = candidateViews.stream()
                .filter(view -> view instanceof RedirectView)
                .findAny();
        if (viewOptional.isPresent()) {
            return viewOptional.get();
        }

        HttpServletRequest request = RequestContextHolder.getRequest();
        Enumeration<String> acceptHeaders = request.getHeaders("Accept");
        while (acceptHeaders.hasMoreElements()) {
            for (View view : candidateViews) {
                if (acceptHeaders.nextElement().contains(view.getContentType())) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * 先找出所有候选视图
     *
     * @param viewName
     * @return
     * @throws Exception
     */
    private List<View> getCandidateViews(String viewName) throws Exception {
        List<View> candidateViews = new ArrayList<>();
        for (ViewResolver viewResolver : viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (Objects.nonNull(view)) {
                candidateViews.add(view);
            }
        }
        if (!CollectionUtils.isEmpty(defaultViews)) {
            candidateViews.addAll(defaultViews);
        }
        return candidateViews;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(viewResolvers, "viewResolvers can not null");
    }

    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    public void setDefaultViews(List<View> defaultViews) {
        this.defaultViews = defaultViews;
    }
}
