package ru.alta.thirdproj.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.alta.thirdproj.entites.UserLogin;


public interface UserLoginService extends UserDetailsService {
    UserLogin findLoginUserByUserName(String username);
    boolean save(UserLogin systemUser);
}
