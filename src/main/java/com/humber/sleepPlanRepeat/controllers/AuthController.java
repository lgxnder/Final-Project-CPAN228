package com.humber.sleepPlanRepeat.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//AuthController work in progress
public class AuthController {

    //Constructor(work in progress)
    private AuthController() {

    }

    //Injecting application name
    @Value("sleepPlanRepeat")
    private String applicationName;

    //error endpoint
    @GetMapping("/error")
    public String handleError() {

        return "";
    }

    //login endpoint
    @GetMapping("/login")
    public String login(Model model) {
        return "";
    }

    //custom logout endpoint
    @GetMapping("/logout")
    public String customLogout() {

        return "";
    }

    //custom registration endpoint
    @GetMapping("/register")
    public String register(Model model) {

        return "";
    }

    //custom registration for saving user
    @PostMapping("/register")
    public String register(){

        return "";
    }
}

