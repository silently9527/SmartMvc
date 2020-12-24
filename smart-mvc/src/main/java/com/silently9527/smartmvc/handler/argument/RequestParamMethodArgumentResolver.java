package com.silently9527.smartmvc.handler.argument;

import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.exception.MissingServletRequestParameterException;
import com.silently9527.smartmvc.handler.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response,
                                  ModelAndViewContainer container,
                                  ConversionService conversionService) throws Exception {

        RequestParam param = parameter.getParameterAnnotation(RequestParam.class);
        if (Objects.isNull(param)) {
            return null;
        }
        String value = request.getParameter(param.name());
        if (StringUtils.isEmpty(value)) {
            value = param.defaultValue();
        }
        if (!StringUtils.isEmpty(value)) {
            return conversionService.convert(value, parameter.getParameterType());
        }

        if (param.required()) {
            throw new MissingServletRequestParameterException(parameter.getParameterName(),
                    parameter.getParameterType().getName());
        }
        return null;
    }

}
