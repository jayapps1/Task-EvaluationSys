package com.evaluationsys.taskevaluationsys.controller.pagecontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/about")
    public String about() {
        return "layout/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "layout/contactus";
    }
}