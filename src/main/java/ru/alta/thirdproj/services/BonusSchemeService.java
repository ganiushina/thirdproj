package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeBdmLimit;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;
import ru.alta.thirdproj.entites.BonusSchemeLimitView;
import ru.alta.thirdproj.entites.BonusSchemeName;
import ru.alta.thirdproj.entites.BonusSchemeRangeView;

import java.util.List;

public interface BonusSchemeService {

    List<BonusGap> getGaps();

    List<BonusPosition> getPositions();

    List<BonusSchemeLimit> getSchemeLimits();

    List<BonusSchemeLimitView> getSchemeLimitDetails();

    List<BonusSchemeRangeView> getSchemeRanges();

    List<BonusSchemeBdmLimit> getBdmLimits();

    List<BonusSchemeName> getSchemeNames();

    void addScheme(BonusSchemeEntry entry);
}
