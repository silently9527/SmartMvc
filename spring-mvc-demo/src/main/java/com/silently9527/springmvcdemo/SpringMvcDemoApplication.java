package com.silently9527.springmvcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
@SpringBootApplication
public class SpringMvcDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMvcDemoApplication.class, args);
    }


    @ResponseBody
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public List<String> index(@RequestParam("name") String name, HttpServletRequest request) {
        return Arrays.asList("A", "B", "C");
    }

    @RequestMapping(value = "/viewName", method = RequestMethod.GET)
    public String viewName() {
        return "silently9527";
    }

    @RequestMapping(value = "/viewName2", method = RequestMethod.GET)
    public String viewName2() {
        return "redirect:/silently9527";
    }
}
