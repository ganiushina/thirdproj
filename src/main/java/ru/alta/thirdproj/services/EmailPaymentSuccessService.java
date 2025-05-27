package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.repositories.EmailUserRepositoriesImpl;

import java.time.LocalDate;

@Service
public class EmailPaymentSuccessService {

    public EmailUserRepositoriesImpl emailUserRepositories;
    @Autowired
    public void EmailPaymentSuccessService(EmailUserRepositoriesImpl emailUserRepositories){
        this.emailUserRepositories = emailUserRepositories;
    }

    public void save(int userId , LocalDate dateFrom, LocalDate dateTo, int success){
        emailUserRepositories.save(userId,dateFrom, dateTo, success);
    }
}
