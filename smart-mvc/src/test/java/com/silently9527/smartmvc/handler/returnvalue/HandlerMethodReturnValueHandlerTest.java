package com.silently9527.smartmvc.handler.returnvalue;

import com.silently9527.smartmvc.controller.TestReturnValueController;
import com.silently9527.smartmvc.handler.ModelAndViewContainer;
import com.silently9527.smartmvc.view.View;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;

import java.lang.reflect.Method;

public class HandlerMethodReturnValueHandlerTest {

    @Test
    public void test() throws Exception {
        HandlerMethodReturnValueHandlerComposite composite = new HandlerMethodReturnValueHandlerComposite();
        composite.addReturnValueHandler(new ModelMethodReturnValueHandler());
        composite.addReturnValueHandler(new MapMethodReturnValueHandler());
        composite.addReturnValueHandler(new ResponseBodyMethodReturnValueHandler());
        composite.addReturnValueHandler(new ViewMethodReturnValueHandler());
        composite.addReturnValueHandler(new ViewNameMethodReturnValueHandler());

        ModelAndViewContainer mvContainer = new ModelAndViewContainer();
        TestReturnValueController controller = new TestReturnValueController();

        //测试方法testViewName
        Method viewNameMethod = controller.getClass().getMethod("testViewName");
        MethodParameter viewNameMethodParameter = new MethodParameter(viewNameMethod, -1);
        composite.handleReturnValue(controller.testViewName(), viewNameMethodParameter, mvContainer, null, null);
        Assert.assertEquals(mvContainer.getViewName(), "/jsp/index.jsp");

        //测试方法testView
        Method viewMethod = controller.getClass().getMethod("testView");
        MethodParameter viewMethodParameter = new MethodParameter(viewMethod, -1);
        composite.handleReturnValue(controller.testView(), viewMethodParameter, mvContainer, null, null);
        Assert.assertTrue(mvContainer.getView() instanceof View);

        //测试方法testResponseBody
        Method responseBodyMethod = controller.getClass().getMethod("testResponseBody");
        MethodParameter resBodyMethodParameter = new MethodParameter(responseBodyMethod, -1);
        MockHttpServletResponse response = new MockHttpServletResponse();
        composite.handleReturnValue(controller.testResponseBody(), resBodyMethodParameter, mvContainer, null, response);
        System.out.println(response.getContentAsString());

        //测试方法testModel
        Method modelMethod = controller.getClass().getMethod("testModel", Model.class);
        MethodParameter modelMethodParameter = new MethodParameter(modelMethod, -1);
        composite.handleReturnValue(controller.testModel(mvContainer.getModel()), modelMethodParameter, mvContainer, null, null);
        Assert.assertEquals(mvContainer.getModel().getAttribute("testModel"), "Silently9527");

        //测试方法testMap
        Method mapMethod = controller.getClass().getMethod("testMap");
        MethodParameter mapMethodParameter = new MethodParameter(mapMethod, -1);
        composite.handleReturnValue(controller.testMap(), mapMethodParameter, mvContainer, null, null);
        Assert.assertEquals(mvContainer.getModel().getAttribute("testMap"), "Silently9527");

    }

}