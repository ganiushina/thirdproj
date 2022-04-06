package ru.alta.thirdproj.repositories;

import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.User;

import java.util.List;

@Repository
public interface IUserRepository  {

        User findByUserName(String userName);
        List<User> findAllBy();
}
