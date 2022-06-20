package ru.alta.thirdproj.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.services.UserServiceImpl;

import java.time.LocalDate;
import java.util.List;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/api/v1/user") //http://localhost:8181/userbonus/api/v1/userbonus/all?date1=2021-12-01&date2=2021-12-31&userId=-1&departmentId=-1
@Api("Set of endpoints for CRUD operations for User")
public class RestUserController {

    private UserServiceImpl userService;

    @Autowired
    public void setUserService(UserServiceImpl userService){this.userService= userService;}

    @GetMapping("/users")
    @ApiOperation("Returns list of all products data transfer objects")
    public User getAllUser(@RequestParam(value = "name") String userName) {
        return userService.findByUserName(userName);
    }


}
