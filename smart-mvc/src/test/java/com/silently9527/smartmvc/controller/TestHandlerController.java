package com.silently9527.smartmvc.controller;

import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.http.RequestMethod;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Controller
public class TestHandlerController {

    @RequestMapping(path = "/ex_test", method = RequestMethod.POST)
    public void exTest() {
    }

    @RequestMapping(path = "/in_test", method = RequestMethod.POST)
    public void inTest() {
    }


    @RequestMapping(path = "/in_test2", method = RequestMethod.POST)
    public void inTest2() {
    }

    @RequestMapping(path = "/in_test3", method = RequestMethod.POST)
    public void inTest3() {
    }

}
