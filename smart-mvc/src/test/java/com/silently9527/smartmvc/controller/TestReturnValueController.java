package com.silently9527.smartmvc.controller;

import com.silently9527.smartmvc.annotation.ResponseBody;
import com.silently9527.smartmvc.view.View;
import com.silently9527.smartmvc.vo.UserVo;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestReturnValueController {

    @ResponseBody
    public UserVo testResponseBody() {
        UserVo userVo = new UserVo();
        userVo.setBirthday(new Date());
        userVo.setAge(20);
        userVo.setName("Silently9527");
        return userVo;
    }

    public String testViewName() {
        return "/jsp/index.jsp";
    }

    public View testView() {
        return new View() {
            @Override
            public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            }
        };
    }

    public Model testModel(Model model) {
        model.addAttribute("testModel", "Silently9527");
        return model;
    }

    public Map<String, Object> testMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("testMap", "Silently9527");
        return params;
    }

}
