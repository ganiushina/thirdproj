package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.DepartmentUserSales;
import ru.alta.thirdproj.entites.MarginBonus;
import ru.alta.thirdproj.entites.UserSale;
import ru.alta.thirdproj.repositories.UserSalaryRepImplRep;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserSalesServiceImpl {

    private UserSalaryRepImplRep userSalaryRepImplRep;

    @Autowired
    public void setUserSalesServiceImpl(UserSalaryRepImplRep userSalaryRepImplRep){
        this.userSalaryRepImplRep = userSalaryRepImplRep;
    }


    public List<DepartmentUserSales> getUserSalesList(LocalDate date1, LocalDate date2){
        return userSalaryRepImplRep.getAllUsersSales(date1, date2);
    }

    public List<MarginBonus> getMarginBonusByMonth(LocalDate date1, LocalDate date2){
        return userSalaryRepImplRep.getMarginBonusByMonth(date1, date2);
    }


}
