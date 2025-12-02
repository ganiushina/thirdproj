package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;

import java.util.List;

@Component
public class BonusSchemeRepository {

    private final Sql2o sql2o;

    private static final String SELECT_GAPS = "SELECT gap_id, gap_percent, scheme_date FROM gap";
    private static final String SELECT_POSITIONS = "SELECT id, pos_name FROM positions";
    private static final String SELECT_LIMITS = "SELECT id, limits, position_id, gap_id, division_id, scheme_limits_date FROM scheme_limits";
    private static final String SELECT_SCHEMES = "SELECT id, scheme_id, limits, position_id, gap_id, date_scheme FROM bonus_scheme";
    private static final String INSERT_SCHEME = "INSERT INTO bonus_scheme (scheme_id, limits, position_id, gap_id, date_scheme) " +
            "VALUES (:scheme_id, :limits, :position_id, :gap_id, :date_scheme)";

    @Autowired
    public BonusSchemeRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<BonusGap> findAllGaps() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_GAPS, false)
                    .setColumnMappings(BonusGap.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusGap.class);
        }
    }

    public List<BonusPosition> findAllPositions() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_POSITIONS, false)
                    .setColumnMappings(BonusPosition.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusPosition.class);
        }
    }

    public List<BonusSchemeLimit> findAllLimits() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_LIMITS, false)
                    .setColumnMappings(BonusSchemeLimit.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeLimit.class);
        }
    }

    public List<BonusSchemeEntry> findAllSchemes() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_SCHEMES, false)
                    .setColumnMappings(BonusSchemeEntry.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeEntry.class);
        }
    }

    public void saveSchemeEntry(BonusSchemeEntry entry) {
        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(INSERT_SCHEME)
                    .addParameter("scheme_id", entry.getSchemeId())
                    .addParameter("limits", entry.getLimits())
                    .addParameter("position_id", entry.getPositionId())
                    .addParameter("gap_id", entry.getGapId())
                    .addParameter("date_scheme", entry.getDateScheme())
                    .executeUpdate();

            connection.commit();
        }
    }
}
