package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.*;

import java.util.List;
import java.util.Map;

public interface BonusSchemeService {
    Map<String, List<SchemeRow>> getSchemesGroupedByPosition();

    Map<String, List<SchemeRow>> getSchemesGroupedByScheme();

    List<Gap> getGaps();

    List<Position> getPositions();

    List<SchemeName> getSchemeNames();

    void addScheme(SchemeEntry entry);

    SchemeEntry createEmptyEntry();


}
