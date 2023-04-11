package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.repositories.ActPutRepository;

import java.time.LocalDate;
import java.util.List;


@Service
public class ActPutServiceImpl implements iActPutService {

    private ActPutRepository actPutRepository;

    @Autowired
    public void setActPutRepository(ActPutRepository actPutRepository){
        this.actPutRepository = actPutRepository;
    }

    @Override
    public List<Act> getAllPutAct(LocalDate date1, LocalDate date2) {
        return actPutRepository.getPutAct(date1, date2);
    }

    @Override
    public List<Act> getANoPaymentAct() {
        return actPutRepository.getNoPaymentAct();
    }

    @Override
    public Double allDebitAct(List<Act> actList) {
        return null;
    }
}
