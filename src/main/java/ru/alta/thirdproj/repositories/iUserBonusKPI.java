package ru.alta.thirdproj.repositories;


import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.UserBonusKPI;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface iUserBonusKPI {

    List<UserBonusKPI> getUserBonusKPIList (LocalDate date1, LocalDate date2);

}
