package ru.alta.thirdproj.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserLogin;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.repositories.UserLoginRepositorySlqO2;
import ru.alta.thirdproj.repositories.UserRepositorySlqO2;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.specification.UserSpecification;
import ru.alta.thirdproj.specification.UserSpecificationsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin("*")
@RequestMapping("*") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Api("Set of endpoints for CRUD operations for UserBonus")
@Tag(name="RestBonusController", description="Заработанные бонусы")
public class RestBonusController {

    private UserBonusServiceImpl bonusService;

    @Autowired
    UserLoginRepositorySlqO2 userR;

    @Autowired
    public RestBonusController(UserBonusServiceImpl bonusService) {
        this.bonusService = bonusService;
    }
    @GetMapping("/all") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
   // @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserBonus> getAllUserBonus(@RequestParam(value = "date1")
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                           @RequestParam(value = "date2")
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2
//                                           ,@RequestParam(value = "userId", required = false) String userName,
//                                           @RequestParam(value = "depId", required = false) Integer departmentId

    ) {

        UserSpecificationsBuilder builder = new UserSpecificationsBuilder();
        Pattern pattern = Pattern.compile("(\\w+?)(:|<|>)(\\w+?),");
//      //  Matcher matcher = pattern.matcher(search + ",");
//        while (matcher.find()) {
//            builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
        //}



//
     //   Specification<UserBonus> spec = builder.build();
                //Specification.where(null);
//
//        StringBuilder filters = new StringBuilder();
//        if (userName != null) {
//            spec = spec.and(UserSpecification.userNameEquals(userName));
//            filters.append("&userName=" + userName);
//        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserLogin user = userR.getUser(userDetails.getUsername());
   //     return new ResponseEntity(bonusService.findAll(spec, HttpStatus.OK);
       return new ResponseEntity(bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment()), HttpStatus.OK);
    }

//    @GetMapping(produces = "application/json")
//    @ApiOperation("Returns list of all products")
//    public List<Product> getAllProducts() {
//        return productsService.findAll();
//    }

//    @GetMapping(value = "/{id}", produces = "application/json")
//    @ApiOperation("Returns one product by id")
//    public ResponseEntity<?> getOneProduct(@PathVariable @ApiParam("Id of the product to be requested. Cannot be empty") Long id) {
//        if (!bonusService.existsById(id)) {
//            throw new ProductNotFoundException("Product not found, id: " + id);
//        }
//        return new ResponseEntity<>(productsService.findById(id), HttpStatus.OK);
//    }
//
//    @DeleteMapping
//    @ApiOperation("Removes all products")
//    public void deleteAllProducts() {
//        productsService.deleteAll();
//    }
//
//    @DeleteMapping("/{id}")
//    @ApiOperation("Removes one product by id")
//    public void deleteOneProducts(@PathVariable Long id) {
//        productsService.deleteById(id);
//    }
//
//    @PostMapping(consumes = "application/json", produces = "application/json")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ApiOperation("Creates a new product")
//    public Product saveNewProduct(@RequestBody Product product) {
//        if (product.getId() != null) {
//            product.setId(null);
//        }
//        return productsService.saveOrUpdate(product);
//    }
//
//    @PutMapping(consumes = "application/json", produces = "application/json")
//    @ApiOperation("Modifies an existing product")
//    public ResponseEntity<?> modifyProduct(@RequestBody Product product) {
//        if (product.getId() == null || !productsService.existsById(product.getId())) {
//            throw new ProductNotFoundException("Product not found, id: " + product.getId());
//        }
//        if (product.getPrice() < 0) {
//            return new ResponseEntity<>("Product's price can not be negative", HttpStatus.BAD_REQUEST);
//        }
//        return new ResponseEntity<>(productsService.saveOrUpdate(product), HttpStatus.OK);
//    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}