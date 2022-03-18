package ru.alta.thirdproj.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdditionalController {
    @GetMapping("/")
    public String homepage() {
        return "index";
    }

}