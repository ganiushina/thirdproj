package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.Role;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserLogin;

import java.util.List;

@Component
public class UserRepositorySlqO2
       // extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>
{

    private final Sql2o sql2o;

    private static final String SELECT_USER_QUERY =
            "SELECT DISTINCT man_id \n" +
                    "      ,man_fio, l.login_name      \n" +

                    "\t  , isnull(ubm.user_position, 0) user_position, \n" +
                    "\t   isnull(ubm.user_department, 0) user_department, \n" +
                    "\t  l.login_hash_bcript" +
                    "\t  from man m \n" +
                    "     JOIN login l ON l.login_user_id = m.man_id\n" +
                    "     left JOIN dbo.userplanByMonth ubm ON ubm.user_id = l.login_user_id AND ubm.userplan_month = DATEPART(mm, GETDATE()) AND ubm.userplan_year = DATEPART(yy, GETDATE())\n" +
                    "     left JOIN dbo.position p ON ubm.user_position = p.id\n" +
                    "     left JOIN dbo.depatment d ON d.id = ubm.user_department\n" +
                    "     where l.login_active = 1 and l.login_name = :user_login ";

    private String SELECT_USER_ROLE_QUERY =
            " SELECT r.id, r.name\n" +
                    "FROM dbo.Login_Role lr\n" +
                    "JOIN dbo.Role r ON r.id = lr.role_Id\n" +
                    "WHERE login_Id = :user_Id";

    public UserRepositorySlqO2(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public User getUser(String userName) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_USER_QUERY)
                    .throwOnMappingFailure(false)
                    .addParameter("user_login", userName)
                    .setColumnMappings(User.COLUMN_MAPPINGS)
                    .executeAndFetchFirst(User.class);
        }
    }

    public List<User> getUsers() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_USER_QUERY, false)
                    .setColumnMappings(User.COLUMN_MAPPINGS)
                    .executeAndFetch(User.class);
        }
    }

    public List<Role> getUserRole(Long user_id) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_USER_ROLE_QUERY)
                    .throwOnMappingFailure(false)
                    .addParameter("user_Id", user_id)
                    .setColumnMappings(Role.COLUMN_MAPPINGS)
                    .executeAndFetch(Role.class);
        }
    }


}