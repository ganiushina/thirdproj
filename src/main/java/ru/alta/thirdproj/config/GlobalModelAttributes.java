package ru.alta.thirdproj.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("firstName")
    public String addFirstName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // вернёт имя пользователя или "anonymousUser"
    }
}
