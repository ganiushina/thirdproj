package ru.alta.thirdproj.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.alta.thirdproj.entites.UserBonus;

import java.time.LocalDate;
import java.util.List;

public interface iUserBonusRepository
        //extends JpaRepository<UserBonus, Long>, JpaSpecificationExecutor<UserBonus>
{
    List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId);
}
