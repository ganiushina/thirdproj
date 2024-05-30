package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.MarginBonusBDM;

import java.time.LocalDate;
import java.util.List;

public interface iMarginBonusService {
    List<MarginBonusBDM> getAllMarginBonus(LocalDate date1, LocalDate date2);

}
