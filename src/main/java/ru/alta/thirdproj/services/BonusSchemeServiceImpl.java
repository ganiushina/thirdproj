package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.repositories.BonusSchemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class BonusSchemeServiceImpl implements BonusSchemeService {

    private final BonusSchemeRepository repository;

    @Autowired
    public BonusSchemeServiceImpl(BonusSchemeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, List<SchemeRow>> getSchemesGroupedByPosition() {
        List<SchemeRow> rows = repository.fetchSchemeRowsFromProc();

        // Группируем по positionName (Москва, Самара и т.д.)
        Map<String, List<SchemeRow>> byPosition = new java.util.LinkedHashMap<>();

        for (SchemeRow row : rows) {
            String posName = row.getPositionName();   // ВАЖНО: именно positionName, не schemeName!

            List<SchemeRow> list = byPosition.get(posName);
            if (list == null) {
                list = new java.util.ArrayList<>();
                byPosition.put(posName, list);
            }
            list.add(row);
        }

        return byPosition;
    }

    public Map<String, List<SchemeRow>> getSchemesGroupedByScheme() {
        List<SchemeRow> rows = repository.fetchSchemeRowsFromProc();

        Map<String, List<SchemeRow>> byScheme = new java.util.LinkedHashMap<>();
        for (SchemeRow row : rows) {
            String schemeName = row.getSchemeName();
            byScheme.computeIfAbsent(schemeName, k -> new java.util.ArrayList<>())
                    .add(row);
        }
        return byScheme;
    }




    @Override
    public List<Gap> getGaps() {
        return repository.findAllGaps();
    }

    @Override
    public List<Position> getPositions() {
        return repository.findAllPositions();
    }

    @Override
    public List<SchemeName> getSchemeNames() {
        return repository.findAllSchemeNames();
    }

    @Override
    public void addScheme(SchemeEntry entry) {
        repository.insertScheme(entry);
    }

    @Override
    public SchemeEntry createEmptyEntry() {
        SchemeEntry entry = new SchemeEntry();
        entry.setDateScheme(LocalDate.now());
        return entry;
    }



}
