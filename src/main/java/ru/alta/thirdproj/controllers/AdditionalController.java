package ru.alta.thirdproj.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
@Controller
public class AdditionalController {
    @GetMapping("/")
    public String homepage() {
        return "index";
    }

}