package ru.alta.thirdproj.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.User;

import java.util.List;

@Repository
public interface IUserRepository extends CrudRepository<User, Long> {

        User findByUserName(String userName);
        List<User> findAllBy();
}
