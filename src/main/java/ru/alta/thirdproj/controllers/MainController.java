package ru.alta.thirdproj.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.repositories.UserRepositorySlqO2;
import ru.alta.thirdproj.services.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("*") //http://localhost:8181/userbonus/api/v1/userbonus/all?date1=2021-12-01&date2=2021-12-31&userId=-1&departmentId=-1
@Api("User controller")
public class MainController {

    @Autowired
    UserRepositorySlqO2 userR;


    @GetMapping("/user1")
    public ResponseEntity<?> getFakeResult(@AuthenticationPrincipal User sUser) {
        try {
            Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
            String userName = loggedInUser.getName();
            return ResponseEntity.ok(userName + " success");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

//    @GetMapping("/user")
//    public String getIndex(@AuthenticationPrincipal User sUser) {
//
//            Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
//            String userName = loggedInUser.getName();
//        return "index";
//    }

    @RequestMapping("/user")
    public String handleRequest2(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        model.addAttribute("uri", request.getRequestURI())
                .addAttribute("firstName", auth.getName());
        return "index";
    }
}
