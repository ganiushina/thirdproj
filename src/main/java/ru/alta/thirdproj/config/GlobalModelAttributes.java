package ru.alta.thirdproj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.services.DepartmentService;
import ru.alta.thirdproj.services.UserService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {
    private final UserService userService;
    private final DepartmentService departmentService;

    @Autowired
    public GlobalModelAttributes(UserService userService, DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @ModelAttribute("greeting")
    public String getGreeting(Principal principal) {
        // Базовые настройки
        String strWelcome = "Здравствуй";
        String greetingTemplate = "<div class='greeting-block'>%s</div>";

        if (principal == null) {
            return String.format(greetingTemplate, strWelcome + "!");
        }

        User user = userService.findByUserName(principal.getName());
        if (user == null) {
            return String.format(greetingTemplate, strWelcome + "!");
        }

        // Получаем и обрабатываем имя
        String userFullName = extractFirstName(user.getUserFIO(), user);
        boolean isDasha = user.getUserId() == 9421219;

        // Собираем приветствие
        StringBuilder greeting = new StringBuilder(strWelcome);

        if (!userFullName.isEmpty()) {
            greeting.append(", <span class='user-name'>")
                    .append(userFullName)
                    .append("</span>!");
        } else {
            greeting.append("!");
        }

        // Добавляем спец-сообщение
        if (isDasha) {
            greeting.append(" <span class='dasha-message'>Хорощего дня и улыбайся солнышку :)</span>");
        }

        return String.format(greetingTemplate, greeting.toString());
    }

    @ModelAttribute("departments")
    public List<String> populateDepartments() {
        List<String> departments = departmentService.getDepartments();
        return departments != null ? departments : Collections.emptyList();
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

