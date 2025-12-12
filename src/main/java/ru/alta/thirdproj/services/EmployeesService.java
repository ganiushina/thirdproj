package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Employees;
import ru.alta.thirdproj.repositories.UserSalaryRepImplRep;

import java.util.Collections;
import java.util.List;

@Service
public class EmployeesService {

    private final UserSalaryRepImplRep userSalaryRepImplRep;

    @Autowired
    public EmployeesService(UserSalaryRepImplRep userSalaryRepImplRep) {
        this.userSalaryRepImplRep = userSalaryRepImplRep;
    }

    public List<Employees> getActiveEmployeesForPlanMonth() {
        List<Employees> employees = userSalaryRepImplRep.getEmployeesForCurrentPlanMonth();
        return employees != null ? Collections.unmodifiableList(employees) : Collections.emptyList();
    }
}
