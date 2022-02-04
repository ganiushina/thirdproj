package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserPaymentBonus;
import ru.alta.thirdproj.repositories.BonusPaymentRepositoryImpl;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;

import java.time.LocalDate;
import java.util.List;


@Service
public class UserPaymentBonusServiceImpl {

    private BonusPaymentRepositoryImpl userBonusPaymentRepository;

    @Autowired
    public void setUserBonusProvider(BonusPaymentRepositoryImpl userBonusPaymentRepository){
        this.userBonusPaymentRepository = userBonusPaymentRepository;
    }

    public List<UserPaymentBonus> findAll(LocalDate date1, LocalDate date2, Integer userId, Integer departmentId){
        return userBonusPaymentRepository.getUserBonuses(date1, date2, userId, departmentId);
    }

//    public Page<UserBonus> findAll(Specification<UserBonus> spec, Integer page) {
//        if (page < 1L) {
//            page = 1;
//        }
//        return userBonusProvider.findAll(spec, PageRequest.of(page - 1, 10));
//    }




}
