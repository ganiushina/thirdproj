package ru.alta.thirdproj.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class EchoPostController {

    @RequestMapping(value = "/examples/echo-message", method = RequestMethod.POST)
    @ResponseBody
    public String sendPostMessage(@RequestParam("message") String message) {
        return message;
    }

    @RequestMapping("/user1234")
    public String handleRequest2(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        model.addAttribute("uri", request.getRequestURI())
                .addAttribute("firstName", auth.getName());
        return "Ajaxtest1";
    }
}