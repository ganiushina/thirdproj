package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.*;

import ru.alta.thirdproj.repositories.UserRepositorySlqO2;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployerServiceImpl {

    private UserRepositorySlqO2 userProvider;


    @Autowired
    public void setUserProvider(UserRepositorySlqO2 userProvider) {
        this.userProvider = userProvider;
    }

    public List<Employer> getAll(){
        return userProvider.getEmployer();
    }

    public Employer findByUserName(String username) {
        return userProvider.getEmployerByName(username);
    }

}
