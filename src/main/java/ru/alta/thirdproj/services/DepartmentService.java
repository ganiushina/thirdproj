package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.repositories.UserSalaryRepImplRep;

import java.util.Collections;
import java.util.List;

import ru.alta.thirdproj.entites.Department;

@Service
public class DepartmentService {

    private final UserSalaryRepImplRep userSalaryRepImplRep;

    @Autowired
    public DepartmentService(UserSalaryRepImplRep userSalaryRepImplRep) {
        this.userSalaryRepImplRep = userSalaryRepImplRep;
    }

    public List<Department> getDepartments() {
        List<Department> departments = userSalaryRepImplRep.getAllDepartments();
        return departments != null ? Collections.unmodifiableList(departments) : Collections.emptyList();
    }
}
