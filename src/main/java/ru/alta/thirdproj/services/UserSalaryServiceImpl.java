package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.UserSalary;
import ru.alta.thirdproj.repositories.BonusPaymentRepositoryImpl;
import ru.alta.thirdproj.repositories.iUserSalaryRep;

import java.time.LocalDate;
import java.util.List;
@Service
public class UserSalaryServiceImpl {

    private iUserSalaryRep userSalaryRep;

    @Autowired
    public void setUserBonusProvider(iUserSalaryRep userSalaryRep){
        this.userSalaryRep = userSalaryRep;
    }

    public List<UserSalary> getUserSalaryList(LocalDate date1, LocalDate date2){
        return userSalaryRep.getAllUserSalary(date1, date2);
    }


}
