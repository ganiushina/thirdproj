package ru.alta.thirdproj.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.UserBonus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface iUserBonusRepository
      //  extends CrudRepository<UserBonus, Long>, PagingAndSortingRepository<UserBonus, Long>
        extends PagingAndSortingRepository<UserBonus, Long>, JpaSpecificationExecutor<UserBonus>
{
    List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId);
    
    Page<UserBonus> findByFioAndDepartment(String fio, String department, Pageable pageable);
}
