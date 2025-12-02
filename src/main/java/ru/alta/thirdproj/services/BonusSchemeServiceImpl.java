package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;
import ru.alta.thirdproj.repositories.BonusSchemeRepository;

import java.time.LocalDateTime;
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
    public List<BonusSchemeLimit> getLimits() {
        return repository.findAllLimits();
    }

    @Override
    public List<BonusSchemeEntry> getSchemes() {
        return repository.findAllSchemes();
    }

    @Override
    public void addScheme(BonusSchemeEntry entry) {
        if (entry.getDateScheme() == null) {
            entry.setDateScheme(LocalDateTime.now());
        }
        repository.saveSchemeEntry(entry);
    }
}
