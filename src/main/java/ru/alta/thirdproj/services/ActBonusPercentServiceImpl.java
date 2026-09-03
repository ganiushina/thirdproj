package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ExtraAct;
import ru.alta.thirdproj.repositories.ActBonusPercentRepositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


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
    public void saveActBonus(Integer employerId, Integer userId, Map<Integer, Double> actPercentMap) {

        if (actPercentMap == null || actPercentMap.isEmpty()) {
            return; // Нет данных для сохранения
        }

        // Удаляем старые записи для этих актов и работодателя
      //  actBonusPercentRepositories.deleteExtraBonus(employerId, new ArrayList<>(actPercentMap.keySet()));

        // Сохраняем новые записи
        for (Map.Entry<Integer, Double> entry : actPercentMap.entrySet()) {
            Integer actId = entry.getKey();
            Double percent = entry.getValue();

            System.out.println("Передаваемые атрибуты10: " + "employerId " +
                    employerId + "userId." + userId +
                    "actId " +  actId + "percent " + percent);

            if (percent != null && percent > 0) {
                System.out.println("Передаваемые атрибуты8: " + "employerId " +
                        employerId.intValue() + "userId." + userId.intValue() +
                        "actId " +  actId.intValue() + "percent " + percent);

                actBonusPercentRepositories.saveExtraBonus(employerId.intValue(), userId.intValue(), actId.intValue(), percent);
            }
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

    @Override
    public List<ExtraAct> getAllExtraAct(LocalDate date1, LocalDate date2, Integer employerId) {
        return actBonusPercentRepositories.getAllExtraActs(date1, date2, employerId);
    }

}
