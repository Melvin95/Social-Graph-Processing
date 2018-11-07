package com.project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
 public class Home {

    @RequestMapping(value="/")
    public String showWelcomePage(ModelMap model){
        return "crew/index";
    }
    
}
