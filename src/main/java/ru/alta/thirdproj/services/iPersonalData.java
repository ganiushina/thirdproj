package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.PersonalData;

import java.time.LocalDate;
import java.util.List;

public interface iPersonalData {
    List<PersonalData> getPersonalData(LocalDate date1, LocalDate date2);
}
