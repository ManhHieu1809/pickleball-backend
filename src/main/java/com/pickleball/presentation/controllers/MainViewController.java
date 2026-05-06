package com.pickleball.presentation.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class MainViewController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}