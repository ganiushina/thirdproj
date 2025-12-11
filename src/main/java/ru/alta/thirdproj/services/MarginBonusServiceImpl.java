package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.ActByUserCheck;
import ru.alta.thirdproj.entites.MarginBonusBDM;
import ru.alta.thirdproj.entites.UserSalary;
import ru.alta.thirdproj.entites.UserSalaryDetail;
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

    @Override
    public List<UserSalary> getUserSalary(LocalDate date1, LocalDate date2, Integer departmentId) {
        return userSalaryRepImplRep.getAllUserSalary(date1, date2, departmentId);
    }

    @Override
    public List<UserSalaryDetail> getUserSalaryInterpreter(LocalDate date1, LocalDate date2) {
        return userSalaryRepImplRep.getAllUserSalaryInterpreter(date1, date2);
    }

    @Override
    public List<ActByUserCheck> getActByUserCheck(LocalDate date1, LocalDate date2) {
        return userSalaryRepImplRep.getActByUserCheck(date1, date2);
    }

    @Override
    public void saveFailedProbationAct(Integer actId, Double totalNoNds, String candidate,
                                       List<ActByUserCheck> participants) {
        userSalaryRepImplRep.saveFailedProbationAct(actId, totalNoNds, candidate, participants);
    }


}
