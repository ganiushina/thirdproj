package ru.alta.thirdproj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.services.UserService;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAttributes {
    private final UserService userService;

    @Autowired
    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("userFullName") // лучше называть осмысленно, а не просто firstName
    public String addUserFullName(Principal principal) {
        if (principal == null) {
            return "Гость";
        }

        User user = userService.findByUserName(principal.getName());
        if (user == null) {
            return principal.getName();
        }

        String fullName = user.getUserFIO(); // "Иванов Пётр" или "Иванов Пётр Михайлович"
        return extractFirstName(fullName, user); // Вернёт "Пётр"
    }

    private String extractFirstName(String fullName, User user) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        if (user.getUserId() ==9421219){
            fullName = "Новоженина Даша";
        }
        String[] parts = fullName.trim().split("\\s+");
        return (parts.length >= 2) ? parts[1] : fullName;
    }
}

