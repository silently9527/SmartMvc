package com.silently9527.smartmvcspringbootdemo.controller;

import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.annotation.ResponseBody;
import com.silently9527.smartmvc.http.RequestMethod;
import com.silently9527.smartmvcspringbootdemo.annotation.MyUserParam;
import com.silently9527.smartmvcspringbootdemo.vo.UserVo;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    //http://localhost:7979/user/redirect
    @RequestMapping(path = "/redirect", method = RequestMethod.GET)
    public String redirect() {
        return "redirect:http://silently9527.cn";
    }

    //http://localhost:7979/user/get?userId=123
    @ResponseBody
    @RequestMapping(path = "/get", method = RequestMethod.GET)
    public UserVo get(@RequestParam(name = "userId") Long userId) {
        UserVo userVo = new UserVo();
        userVo.setName(userId + "_silently9527");
        userVo.setAge(25);
        userVo.setBirthday(new Date());
        return userVo;
    }

    //http://localhost:7979/user/build?user=silently9527,123
    @ResponseBody
    @RequestMapping(path = "/build", method = RequestMethod.GET)
    public UserVo build(@MyUserParam(name = "user") UserVo vo) {
        return vo;
    }

}
