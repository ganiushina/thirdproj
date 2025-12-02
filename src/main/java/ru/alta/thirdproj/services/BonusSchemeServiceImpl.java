package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeBdmLimit;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;
import ru.alta.thirdproj.entites.BonusSchemeName;
import ru.alta.thirdproj.repositories.BonusSchemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class BonusSchemeServiceImpl implements BonusSchemeService {

    private final BonusSchemeRepository repository;

    @Autowired
    public BonusSchemeServiceImpl(BonusSchemeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<BonusGap> getGaps() {
        return repository.findAllGaps();
    }

    @Override
    public List<BonusPosition> getPositions() {
        return repository.findAllPositions();
    }

    @Override
    public List<BonusSchemeLimit> getSchemeLimits() {
        return repository.findAllLimits();
    }

    @Override
    public List<BonusSchemeBdmLimit> getBdmLimits() {
        return repository.findAllBdmSchemes();
    }

    @Override
    public List<BonusSchemeName> getSchemeNames() {
        return repository.findAllSchemeNames();
    }

    @Override
    public void addScheme(BonusSchemeEntry entry) {
        if (entry.getDateScheme() == null) {
            entry.setDateScheme(LocalDate.now());
        }
        repository.saveSchemeEntry(entry);
    }
}
