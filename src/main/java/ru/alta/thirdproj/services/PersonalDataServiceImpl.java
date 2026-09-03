package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.PersonalData;
import ru.alta.thirdproj.repositories.PersonalDataRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PersonalDataServiceImpl implements iPersonalData {

    private PersonalDataRepository personalDataRepository;

    @Autowired
    public void setPersonalDataRepository(PersonalDataRepository personalDataRepository) {
        this.personalDataRepository = personalDataRepository;
    }

    @Override
    public List<PersonalData> getPersonalData(LocalDate date1, LocalDate date2) {
        return personalDataRepository.getPersonalData(date1, date2);
    }
}
