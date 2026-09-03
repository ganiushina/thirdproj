package ru.alta.thirdproj.repositories;


import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.UserBonusKPI;
import ru.alta.thirdproj.entites.UserBonusKPIMain;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface iUserBonusKPI {

    List<UserBonusKPIMain> getUserBonusKPIList (LocalDate date1, LocalDate date2);

}
