package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard/user")
    public String dashboardUser(){
        return "DashboardUser";
    }
}
