package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.UserBonusKPI;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;
import ru.alta.thirdproj.repositories.UserBonusKPIRepositoryImpl;
import ru.alta.thirdproj.repositories.iUserBonusKPI;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserBonusKPIServiceImpl implements iUserBonusKPI {

    private UserBonusKPIRepositoryImpl bonusKPIRepository;

    @Autowired
    public void setUserBonusKPIProvider(UserBonusKPIRepositoryImpl bonusKPIRepository){
        this.bonusKPIRepository = bonusKPIRepository;
    }

    @Override
    public List<UserBonusKPI> getUserBonusKPIList(LocalDate date1, LocalDate date2) {

        List<UserBonusKPI> userBonusKPIList =   bonusKPIRepository.userBonusKPIList(date1, date2);

        return userBonusKPIList;
    }
}
