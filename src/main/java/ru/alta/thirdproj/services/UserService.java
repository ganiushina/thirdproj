package ru.alta.thirdproj.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.alta.thirdproj.entites.User;



public interface UserService extends UserDetailsService {
    User findByUserName(String username);
    boolean save(User systemUser);
}
