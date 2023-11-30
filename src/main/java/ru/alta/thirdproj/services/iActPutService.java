package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.Employer;

import java.time.LocalDate;
import java.util.List;

public interface iActPutService {

    List<Act> getAllPutAct(LocalDate date1, LocalDate date2);
    List<Act> getANoPaymentAct();
    Double allDebitAct(List<Act> actList);
}
