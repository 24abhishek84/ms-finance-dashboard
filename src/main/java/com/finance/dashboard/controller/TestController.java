package com.finance.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "http://localhost:3000")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Test controller works!");
    }

    @GetMapping("/analytics-simple")
    public ResponseEntity<String> analyticsSimple() {
        return ResponseEntity.ok("Analytics controller should work too!");
    }
}

