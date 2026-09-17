package com.devmind.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://devmind-ai-snowy.vercel.app"
})
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "DevMind Backend is UP 🚀";
    }
}