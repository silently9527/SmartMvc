package com.silently9527.smartmvc;

import com.silently9527.smartmvc.handler.HandlerExecutionChain;
import com.silently9527.smartmvc.handler.adapter.HandlerAdapter;
import com.silently9527.smartmvc.handler.exception.HandlerExceptionResolver;
import com.silently9527.smartmvc.handler.mapping.HandlerMapping;
import com.silently9527.smartmvc.utils.RequestContextHolder;
import com.silently9527.smartmvc.view.View;
import com.silently9527.smartmvc.view.resolver.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public class DispatcherServlet extends HttpServlet implements ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    private HandlerMapping handlerMapping;
    private HandlerAdapter handlerAdapter;
    private ViewResolver viewResolver;
    private Collection<HandlerExceptionResolver> handlerExceptionResolvers;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void init() {
        this.handlerMapping = this.applicationContext.getBean(HandlerMapping.class);
        this.handlerAdapter = this.applicationContext.getBean(HandlerAdapter.class);
        this.viewResolver = this.applicationContext.getBean(ViewResolver.class);
        this.handlerExceptionResolvers =
                this.applicationContext.getBeansOfType(HandlerExceptionResolver.class).values();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("DispatcherServlet.service => uri:{}", request.getRequestURI());
        RequestContextHolder.setRequest(request);

        try {
            doDispatch(request, response);
        } catch (Exception e) {
            logger.error("Handler the request fail", e);
        } finally {
            RequestContextHolder.resetRequest();
        }

    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Exception dispatchException = null;
        HandlerExecutionChain executionChain = null;
        try {
            ModelAndView mv = null;
            try {
                executionChain = this.handlerMapping.getHandler(request);

                if (!executionChain.applyPreHandle(request, response)) {
                    return;
                }
                // Actually invoke the handler.
                mv = handlerAdapter.handle(request, response, executionChain.getHandler());

                executionChain.applyPostHandle(request, response, mv);
            } catch (Exception e) {
                dispatchException = e;
            }
            processDispatchResult(request, response, mv, dispatchException);
        } catch (Exception ex) {
            dispatchException = ex;
            throw ex;
        } finally {
            if (Objects.nonNull(executionChain)) {
                executionChain.triggerAfterCompletion(request, response, dispatchException);
            }
        }

    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       ModelAndView mv, Exception ex) throws Exception {
        if (Objects.nonNull(ex)) {
            //error ModelAndView
            mv = processHandlerException(request, response, ex);
        }

        if (Objects.nonNull(mv)) {
            render(mv, request, response);
            return;
        }

        logger.info("No view rendering, null ModelAndView returned.");
    }

    private void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        View view;
        String viewName = mv.getViewName();
        if (!StringUtils.isEmpty(viewName)) {
            view = this.viewResolver.resolveViewName(viewName);
        } else {
            view = (View) mv.getView();
        }

        if (mv.getStatus() != null) {
            response.setStatus(mv.getStatus().getValue());
        }
        view.render(mv.getModel().asMap(), request, response);
    }

    //出现异常后的ModelAndView
    private ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
                                                 Exception ex) throws Exception {
        if (CollectionUtils.isEmpty(this.handlerExceptionResolvers)) {
            throw ex;
        }
        for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
            ModelAndView exMv = resolver.resolveException(request, response, ex);
            if (exMv != null) {
                return exMv;
            }
        }

        throw ex;
    }


}
