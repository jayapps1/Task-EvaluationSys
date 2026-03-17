package com.evaluationsys.taskevaluationsys;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/main")
    public String testPage() {
        return "layout/main";
    }
}
