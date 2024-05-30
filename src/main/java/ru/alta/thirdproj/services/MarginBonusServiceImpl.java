package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.MarginBonusBDM;
import ru.alta.thirdproj.repositories.UserBonusKPIRepositoryImpl;
import ru.alta.thirdproj.repositories.UserSalaryRepImplRep;

import java.time.LocalDate;
import java.util.List;
@Service
public class MarginBonusServiceImpl implements iMarginBonusService {

    private UserSalaryRepImplRep userSalaryRepImplRep;

    @Autowired
    public void setMarginBonusServiceImpl(UserSalaryRepImplRep userSalaryRepImplRep){
        this.userSalaryRepImplRep = userSalaryRepImplRep;
    }

    @Override
    public List<MarginBonusBDM> getAllMarginBonus(LocalDate date1, LocalDate date2) {
        return userSalaryRepImplRep.getMarginBonus(date1, date2);
    }
}
