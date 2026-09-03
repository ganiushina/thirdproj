package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.MoneyByFinalist;
import ru.alta.thirdproj.repositories.ExpectedMoneyByFinalistRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpectedMoneyByFinalistService implements iExpectedMoneyByFinalistService {
    @Autowired
    ExpectedMoneyByFinalistRepository moneyByFinalistRepo;

    @Override
    public List<MoneyByFinalist> getMoneyByFinalistList() {
        return moneyByFinalistRepo.getMoneyByFinalist();
    }
    public List<BigDecimal> addItem(BigDecimal item) {
        List<BigDecimal> bigDecimalList = new ArrayList<>();
        bigDecimalList.add(item);
        return bigDecimalList;
    }
}
