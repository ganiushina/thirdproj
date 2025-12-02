package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;

import java.util.List;

public interface BonusSchemeService {

    List<BonusGap> getGaps();

    List<BonusPosition> getPositions();

    List<BonusSchemeLimit> getLimits();

    List<BonusSchemeEntry> getSchemes();

    void addScheme(BonusSchemeEntry entry);
}
