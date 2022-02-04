package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;

import java.time.LocalDate;
import java.util.List;


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

//    public Page<UserBonus> findAll(Specification<UserBonus> spec, Integer page) {
//        if (page < 1L) {
//            page = 1;
//        }
//        return userBonusProvider.findAll(spec, PageRequest.of(page - 1, 10));
//    }




}
