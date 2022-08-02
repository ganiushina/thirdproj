package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.repositories.ActBonusPercentRepositories;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


@Service
public class ActBonusPercentServiceImpl implements iActBonus {

    private ActBonusPercentRepositories actBonusPercentRepositories;

    @Autowired
    public void ActBonusPercentServiceImpl(ActBonusPercentRepositories actBonusPercentRepositories) {
        this.actBonusPercentRepositories = actBonusPercentRepositories;
    }

    @Override
    public List<Act> getAllAct(LocalDate date1, LocalDate date2, Integer employerId) {
        return actBonusPercentRepositories.getAllAct(date1, date2, employerId);
    }

    @Override
    public void saveActBonus(int employerId, int userId, List<String> percents, String actId) {
        List<String> actIds = Arrays.asList(actId.split(","));
        for (int i = 0; i < actIds.size() ; i++) {
            actBonusPercentRepositories.saveExtraBonus(employerId, userId, Integer.parseInt(actIds.get(i)), Double.valueOf(percents.get(i)));
        }
    }

    @Override
    public void deleteActBonus(int employerId, List<String> actIds) {
        for (int i = 0; i < actIds.size() ; i++) {
            actBonusPercentRepositories.deleteExtraBonus(employerId, Integer.parseInt(actIds.get(i)));
        }

    }

    @Override
    public List<String> getDeletedExtraBonus(List<String> allExtraBonusList, List<String> newExtraBonusList) {
        return null;
    }

}
