package com.dev.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
    @GetMapping("/login")
    public ModelAndView loginAdmin() {
        ModelAndView mv = new ModelAndView("/login");

        return mv;
    }

    @GetMapping("/estoquista/login")
    public ModelAndView loginEstoquista() {
        ModelAndView mv = new ModelAndView("/estoquista/login");

        return mv;
    }
}
