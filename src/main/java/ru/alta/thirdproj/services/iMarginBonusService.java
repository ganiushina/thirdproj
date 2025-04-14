package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.MarginBonusBDM;
import ru.alta.thirdproj.entites.UserSalary;
import ru.alta.thirdproj.entites.UserSalaryDetail;

import java.time.LocalDate;
import java.util.List;

public interface iMarginBonusService {
    List<MarginBonusBDM> getAllMarginBonus(LocalDate date1, LocalDate date2);
    List<UserSalary> getUserSalary(LocalDate date1, LocalDate date2, Integer departmentId);
    List<UserSalaryDetail> getUserSalaryInterpreter(LocalDate date1, LocalDate date2);

}
