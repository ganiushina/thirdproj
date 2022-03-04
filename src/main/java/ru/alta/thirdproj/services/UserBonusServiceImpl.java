package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserBonusServiceImpl  {

    private BonusRepositoryImpl userBonusProvider;

    @Autowired
    public void setUserBonusProvider(BonusRepositoryImpl userBonusProvider){
        this.userBonusProvider = userBonusProvider;
    }

    public List<UserBonus> findAll(LocalDate date1, LocalDate date2, Integer userId, Integer departmentId){
        return userBonusProvider.getUserBonuses(date1, date2, userId, departmentId);
    }


    public List<UserBonus> findByFioAndDepartment(String userName,  String departmentName){
        return userBonusProvider.findByFioAndDepartment(userName,  departmentName);
    }

}
