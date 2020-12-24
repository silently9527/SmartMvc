package com.silently9527.smartmvc.controller;

import com.silently9527.smartmvc.annotation.ControllerAdvice;
import com.silently9527.smartmvc.annotation.ExceptionHandler;
import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.annotation.ResponseBody;
import com.silently9527.smartmvc.exception.TestException;
import com.silently9527.smartmvc.http.RequestMethod;
import com.silently9527.smartmvc.vo.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Controller
@RequestMapping(path = "/test")
public class DispatcherController {

    @RequestMapping(path = "/dispatch", method = RequestMethod.GET)
    public String dispatch(@RequestParam(name = "name") String name, Model model) {
        System.out.println("DispatcherController.dispatch: name=>" + name);
        model.addAttribute("name", name);
        return "redirect:/silently9527.cn";
    }

    @RequestMapping(path = "/dispatch2", method = RequestMethod.GET)
    public String dispatch2(@RequestParam(name = "name") String name) {
        System.out.println("DispatcherController.dispatch2: name=>" + name);

        throw new TestException("test exception", name);
    }

    @ResponseBody
    @ExceptionHandler({TestException.class})
    public ApiResponse exceptionHandler(TestException ex) {
        System.out.println("exception message:" + ex.getMessage());
        return new ApiResponse(200, "exception handle complete", ex.getName());
    }


}
