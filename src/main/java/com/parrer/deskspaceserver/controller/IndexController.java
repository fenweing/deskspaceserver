package com.parrer.deskspaceserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index(ModelMap modelMap) {

        Map<String, String> map = new HashMap<>();

        map.put("name", "aoppp");
        map.put("desc", "描述");

        // 添加属性 给模版
        modelMap.addAttribute("user", map);
//        ModelAndView modelAndView = new ModelAndView("main");
//        modelAndView.addAllObjects(map);
//        modelAndView.addObject("name", "老王");
//        return modelAndView;
        return "main";

    }
}
