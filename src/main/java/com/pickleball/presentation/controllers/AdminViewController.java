package com.pickleball.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/login")
    public String login() {
        return "admin/login/loginAdmin";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/venues")
    public String venues() {
        return "admin/venues";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "admin/bookings";
    }

    @GetMapping("/finance")
    public String finance() {
        return "admin/finance";
    }

    @GetMapping("/users")
    public String users() {
        return "admin/user";
    }

    @GetMapping("/approvals")
    public String approvals() {
        return "admin/approval";
    }

    @GetMapping("/disputes")
    public String disputes() {
        return "admin/dispute";
    }

    @GetMapping("/programs")
    public String programs() {
        return "admin/program";
    }

    @GetMapping("/system")
    public String system() {
        return "admin/system";
    }
}
