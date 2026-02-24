package com.pickleball.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

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

    @GetMapping("/owner-requests")
    public String ownerRequests() {
        return "admin/OwnerRequest/ownerrequest";
    }

    @GetMapping("/venue-requests")
    public String venueRequests() {
        return "admin/venuerquest";
    }

    @GetMapping("/disputes")
    public String disputes() {
        return "admin/dispute";
    }

    @GetMapping("/rankings")
    public String rankings() {
        return "admin/ranking";
    }

    @GetMapping("/system")
    public String system() {
        return "admin/system";
    }
}
