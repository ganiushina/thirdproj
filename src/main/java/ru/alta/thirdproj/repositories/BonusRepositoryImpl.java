package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.UserBonus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;

    private List<UserBonus> userBonusesList;


    private static final String SELECT_USER_QUERY = "SELECT * FROM fn_User_Bonus_by_Details (:date1, :date2, :user_id, :department_id)";

    public BonusRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            userBonusesList = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId)
                    .setColumnMappings(UserBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserBonus.class);
            return userBonusesList;
        }
    }

    public List<UserBonus> findByFioAndDepartment(String fio, String department) {
        List<UserBonus> userBonusList = null;
        
        if (!fio.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> (c.getFio().equals(fio)))
                    .collect(Collectors.toList());
        if (!department.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> c.getDepartment().equals(department))
                    .collect(Collectors.toList());

        if (!department.equals("") && !fio.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> (c.getFio().equals(fio) && c.getDepartment().equals(department)))
                    .collect(Collectors.toList());
        return userBonusList;
    }



//    @Override
//    public Optional<UserBonus> findOne(Specification<UserBonus> spec) {
//        return Optional.empty();
//    }
//
//    @Override
//    public List<UserBonus> findAll(Specification<UserBonus> spec) {
//        return null;
//    }
//
//    @Override
//    public Page<UserBonus> findAll(Specification<UserBonus> spec, Pageable pageable) {
//        return null;
//    }
//
//    @Override
//    public List<UserBonus> findAll(Specification<UserBonus> spec, Sort sort) {
//        return null;
//    }
//
//    @Override
//    public long count(Specification<UserBonus> spec) {
//        return 0;
//    }
}
