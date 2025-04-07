package ru.alta.thirdproj.controllers;//package ru.alta.thirdproj.controllers;
//
//import lombok.Data;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Controller
//public class TabController {
//    @GetMapping("/tabs")
//    public String showTabs() {
//        return "tabs"; // Renders the main tabs.html template tabs_2 - остается все по старому
//
//    }
//
//    // Home Tab Content
//    @GetMapping("/tabs/home")
//    public String getHomeContent(Model model) {
//        List<User> homeUsers = Arrays.asList(
//                new User(1, "John Doe", "Admin"),
//                new User(2, "Jane Smith", "User")
//        );
//        model.addAttribute("homeUsers", homeUsers);
//    //    return "fragments/home :: homeTable"; // Return only the homeTable fragment
//        return "fragments/month :: monthTable";
//    }
//
//    // Profile Tab Content
//    @GetMapping("/tabs/profile")
//    public String getProfileContent(Model model) {
//        List<Profile> profileData = Arrays.asList(
//                new Profile(1, "john.doe@example.com", "Active"),
//                new Profile(2, "jane.smith@example.com", "Inactive")
//        );
//        model.addAttribute("profileData", profileData);
//    //    return "fragments/profile :: profileTable"; // Return only the profileTable fragment
//        return "fragments/quarter :: quarterTable";
//    }
//
//    // Inner classes for data models
//    @Data
//    public static class User {
//        private int id;
//        private String name;
//        private String role;
//
//        public User(int id, String name, String role) {
//            this.id = id;
//            this.name = name;
//            this.role = role;
//        }
//
//        // Getters and Setters
//    }
//
//    @Data
//    public static class Profile {
//        private int id;
//        private String email;
//        private String status;
//
//        public Profile(int id, String email, String status) {
//            this.id = id;
//            this.email = email;
//            this.status = status;
//        }
//
//        // Getters and Setters
//    }
//}