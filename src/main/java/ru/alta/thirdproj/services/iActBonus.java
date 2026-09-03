package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ExtraAct;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface iActBonus {

    List<Act> getAllAct(LocalDate date1, LocalDate date2, Integer employerId);
//    void saveActBonus(int employerId, int userId, List<String> percents, String actId);

    void saveActBonus(Integer employerId, Integer userId, Map<Integer, Double> actPercentMap);


    void deleteActBonus(int employerId, List<String> actId);
    List<String> getDeletedExtraBonus(List<String> extraBonusList, List<String> newExtraBonusList);

    List<ExtraAct> getAllExtraAct(LocalDate date1, LocalDate date2, Integer employerId);
}
