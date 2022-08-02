package ru.alta.thirdproj.repositories;

import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.UserBonus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Repository
public interface iUserBonusRepository
{
    List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId);
    double getAllMoney(HashMap<String, Object> mapMoney);

}
