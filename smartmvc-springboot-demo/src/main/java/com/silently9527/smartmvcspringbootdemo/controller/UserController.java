package com.silently9527.smartmvcspringbootdemo.controller;

import com.silently9527.smartmvc.annotation.RequestMapping;
import com.silently9527.smartmvc.annotation.RequestParam;
import com.silently9527.smartmvc.annotation.ResponseBody;
import com.silently9527.smartmvc.http.RequestMethod;
import com.silently9527.smartmvcspringbootdemo.vo.UserVo;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    @ResponseBody
    @RequestMapping(path = "/getUser", method = RequestMethod.GET)
    public UserVo getUser(@RequestParam(name = "userId") Long userId) {
        UserVo userVo = new UserVo();
        userVo.setName("silently9527");
        userVo.setAge(25);
        userVo.setBirthday(new Date());
        return userVo;
    }

}
