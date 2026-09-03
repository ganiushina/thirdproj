package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.*;

import java.time.LocalDate;
import java.util.List;

public interface iMarginBonusService {
    List<MarginBonusBDM> getAllMarginBonus(LocalDate date1, LocalDate date2);
    List<UserSalary> getUserSalary(LocalDate date1, LocalDate date2, Integer departmentId);
    List<UserSalaryDetail> getUserSalaryInterpreter(LocalDate date1, LocalDate date2);

    List<ActByUserCheck> getActByUserCheck(LocalDate date1, LocalDate date2);

    void saveFailedProbationAct(Integer actId, Double totalNoNds, String candidate,
                                List<ActByUserCheck> participants);

}
