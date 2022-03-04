package ru.alta.thirdproj.controllers;


import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserLogin;
import ru.alta.thirdproj.entites.UserPaymentBonus;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.repositories.UserLoginRepositorySlqO2;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/payment") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="RestBonusPaymentController", description="Выплаты по бонусам")
public class RestBonusPaymentController {

    private UserPaymentBonusServiceImpl paymentBonusService;

    @Autowired
    UserLoginRepositorySlqO2 userR;

    @Autowired
    public RestBonusPaymentController(UserPaymentBonusServiceImpl paymentBonusService) {
        this.paymentBonusService = paymentBonusService;
    }

    @GetMapping("/allpayment") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
//    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserPaymentBonus> getAllUserBonus(@RequestParam(value = "date1")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                                            @RequestParam(value = "date2")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2
                                            ,@RequestParam(value = "userName", required = false) String userName,
                                             @RequestParam(value = "departmentName", required = false) String departmentName

    ) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserLogin user = userR.getUser(userDetails.getUsername());
        List<UserPaymentBonus> userPaymentBonuses = paymentBonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());

        if (userName != null || departmentName != null)   {
            List<UserPaymentBonus> userPaymentBonusesFilter =  paymentBonusService.findByUserFIOAnfDepartment(userName, departmentName);
            return  new ResponseEntity(userPaymentBonusesFilter, HttpStatus.OK);
        }
        else
        return new ResponseEntity(userPaymentBonuses, HttpStatus.OK);
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