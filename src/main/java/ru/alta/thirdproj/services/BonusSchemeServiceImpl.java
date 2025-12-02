package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeBdmLimit;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;
import ru.alta.thirdproj.entites.BonusSchemeLimitView;
import ru.alta.thirdproj.entites.BonusSchemeName;
import ru.alta.thirdproj.entites.BonusSchemeRangeView;
import ru.alta.thirdproj.repositories.BonusSchemeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<BonusSchemeLimitView> getSchemeLimitDetails() {
        return repository.findAllLimitDetails();
    }

    @Override
    public List<BonusSchemeRangeView> getSchemeRanges() {
        List<BonusSchemeLimitView> details = repository.findAllLimitDetails();
        details.sort(Comparator.comparing(BonusSchemeLimitView::getPositionName)
                .thenComparing(BonusSchemeLimitView::getSchemeName)
                .thenComparing(BonusSchemeLimitView::getDateScheme)
                .thenComparing(BonusSchemeLimitView::getLimits));

        Map<String, List<BonusSchemeLimitView>> grouped = new HashMap<>();
        for (BonusSchemeLimitView detail : details) {
            String key = detail.getPositionName() + "|" + detail.getSchemeName() + "|" + detail.getDateScheme();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(detail);
        }

        List<BonusSchemeRangeView> ranges = new ArrayList<>();
        for (List<BonusSchemeLimitView> group : grouped.values()) {
            group.sort(Comparator.comparing(BonusSchemeLimitView::getLimits));
            for (int i = 0; i < group.size(); i++) {
                BonusSchemeLimitView current = group.get(i);
                BigDecimal start = current.getLimits();
                BigDecimal end = (i + 1 < group.size()) ? group.get(i + 1).getLimits() : null;

                BonusSchemeRangeView view = new BonusSchemeRangeView();
                view.setPositionName(current.getPositionName());
                view.setSchemeName(current.getSchemeName());
                view.setDateScheme(current.getDateScheme());
                view.setPercentRange(buildRangeLabel(start, end, current.getGapPercent()));
                ranges.add(view);
            }
        }

        ranges.sort(Comparator.comparing(BonusSchemeRangeView::getPositionName)
                .thenComparing(BonusSchemeRangeView::getSchemeName)
                .thenComparing(BonusSchemeRangeView::getDateScheme)
                .thenComparing(BonusSchemeRangeView::getPercentRange));
        return ranges;
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

    private String buildRangeLabel(BigDecimal start, BigDecimal end, BigDecimal percent) {
        String startValue = formatNumber(start != null ? start : BigDecimal.ZERO);
        String range;
        if (end != null) {
            range = startValue + "-" + formatNumber(end);
        } else {
            range = startValue + "+";
        }
        return range + " - " + formatNumber(percent) + " %";
    }

    private String formatNumber(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
