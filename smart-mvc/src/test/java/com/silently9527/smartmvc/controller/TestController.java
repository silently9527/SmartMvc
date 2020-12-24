package com.silently9527.smartmvc.controller;

import com.silently9527.smartmvc.annotation.RequestBody;
import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.http.RequestMethod;
import com.silently9527.smartmvc.vo.UserVo;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class TestController {

    @RequestMapping(path = "/test4", method = RequestMethod.POST)
    public void test4(@RequestParam(name = "name") String name,
                      @RequestParam(name = "age") Integer age,
                      @RequestParam(name = "birthday") Date birthday,
                      HttpServletRequest request) {
    }

    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public void user(@RequestBody UserVo userVo) {
    }


}
