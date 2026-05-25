package com.medicore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {
    @RequestMapping(value = {
        "/", "/login", "/register", "/dashboard", "/patients", "/beds"
    })
    public String forward() {
        return "forward:/index.html";
    }
}