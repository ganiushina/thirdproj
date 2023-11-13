package ru.alta.thirdproj.repositories;

import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.UserSalary;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface iUserSalaryRep {

    List<UserSalary> getAllUserSalary(LocalDate date1, LocalDate date2);

    UserSalary findByFio(String fio);
}
