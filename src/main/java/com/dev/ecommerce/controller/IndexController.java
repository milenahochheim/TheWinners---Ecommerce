package com.dev.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dev.ecommerce.repository.ProdutoRepository;

@Controller
public class IndexController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/")
    public ModelAndView homePage() {
        ModelAndView mv = new ModelAndView("/index");
        mv.addObject("listaProdutos", produtoRepository.findAll());
        return mv;
    }
}
