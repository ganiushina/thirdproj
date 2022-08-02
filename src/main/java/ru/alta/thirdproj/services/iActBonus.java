package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.Act;

import java.time.LocalDate;
import java.util.List;

public interface iActBonus {

    List<Act> getAllAct(LocalDate date1, LocalDate date2, Integer employerId);
    void saveActBonus(int employerId, int userId, List<String> percents, String actId);
    void deleteActBonus(int employerId, List<String> actId);
    List<String> getDeletedExtraBonus(List<String> extraBonusList, List<String> newExtraBonusList);


}
