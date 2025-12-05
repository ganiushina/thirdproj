package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Row;
import ru.alta.thirdproj.entites.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.sql2o.data.Table;
import java.util.LinkedHashMap;



@Component
public class BonusSchemeRepository {

    private final Sql2o sql2o;
    private static final String EXEC_RANGE_DETAILS = "EXEC dbo.GetRangeDetails";
    private static final String INSERT_SCHEME = "INSERT INTO scheme_limits(scheme_id, limits, position_id, gap_id, date_scheme)\n" +
            "     VALUES (:scheme_id, :limits, :position_id, :gap_id, :date_scheme)";

    @Autowired
    public BonusSchemeRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<SchemeRow> fetchSchemeRowsFromProc() {

        try (Connection con = sql2o.open()) {

            Table table = con.createQuery(EXEC_RANGE_DETAILS)
                    .executeAndFetchTable();

            List<String> columnNames = table.columns()
                    .stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList());

            // Первые три колонки всегда одинаковы:
            // [должность], [позиция + схема], [date_scheme]
            final int POSITION_INDEX = 0;
            final int SCHEME_INDEX = 1;
            final int DATE_INDEX = 2;

            List<SchemeRow> result = new ArrayList<>();

            for (Row row : table.rows()) {

                // --- 1. Базовые текстовые поля ---
                String positionName = row.getString(POSITION_INDEX);
                String schemeName   = row.getString(SCHEME_INDEX);

                // --- 2. Преобразование даты ---
                java.util.Date dateSql = row.getDate(DATE_INDEX);

                LocalDate dateScheme =
                        (dateSql != null
                                ? dateSql.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                : null);

                // --- 3. Собираем проценты и диапазоны ---
                List<BonusCell> cells = new ArrayList<>();

                // Все колонки, начиная с индекса 3 — это динамические [%]
                for (int i = 3; i < columnNames.size(); i++) {

                    Object value = row.getObject(i);

                    if (value != null) {
                        String gapLabel = columnNames.get(i);  // например "8%"
                        String rangeText = value.toString();   // например "300000 - 450000"

                        cells.add(new BonusCell(gapLabel, rangeText));
                    }
                }

                // --- 4. Собираем объект ---
                SchemeRow schemeRow = new SchemeRow(
                        positionName,
                        schemeName,
                        dateScheme,
                        cells
                );

                result.add(schemeRow);
            }

            return result;
        }
    }


    // gap
    public List<Gap> findAllGaps() {
        String sql = "SELECT gap_id AS gapId, gap_percent AS gapPercent " +
                "FROM gap ORDER BY gap_percent";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(Gap.class);
        }
    }

    // position
    public List<Position> findAllPositions() {
        String sql = "SELECT id, pos_name AS posName " +
                "FROM position ORDER BY pos_name";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(Position.class);
        }
    }

    // scheme
    public List<SchemeName> findAllSchemeNames() {
        String sql = "SELECT id, scheme_name AS schemeName " +
                "FROM scheme ORDER BY scheme_name";

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(SchemeName.class);
        }
    }

    // вставка новой записи в scheme_limits
    public void insertScheme(SchemeEntry entry) {
        try (Connection con = sql2o.open()) {
            con.createQuery(INSERT_SCHEME)
                    .addParameter("scheme_id", entry.getSchemeId())
                    .addParameter("limits", entry.getLimits())
                    .addParameter("position_id", entry.getPositionId())
                    .addParameter("gap_id", entry.getGapId())
                    .addParameter("date_scheme", entry.getDateScheme())
                    .executeUpdate();
        }
    }


}
