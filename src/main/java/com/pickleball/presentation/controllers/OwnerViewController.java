package com.pickleball.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
public class OwnerViewController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "owner/dashboardOwner";
    }

    @GetMapping("/venues")
    public String venues() {
        return "owner/venuesOwner";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "owner/bookingOwner";
    }

    @GetMapping("/bookingstaff")
    public String bookingstaff() {
        return "owner/bookingstaff";
    }

    @GetMapping("/finance")
    public String finance() {
        return "owner/financeOwner";
    }

    @GetMapping("/staff")
    public String staff() {
        return "owner/staff";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
