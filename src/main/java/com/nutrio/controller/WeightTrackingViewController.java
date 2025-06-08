package com.nutrio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WeightTrackingViewController {

    @GetMapping("/weight-tracking")
    public String weightTrackingPage() {
        return "weight-tracking";
    }
} 