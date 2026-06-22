-- ============================================================================
--  Шаг 2b: fn_margin_bonus_by_quarter — потребление реверсов из margin_bonus_correction
-- ----------------------------------------------------------------------------
--  Изменение ЧИСТО АДДИТИВНОЕ:
--    margin_department = zarabotano - salary + ISNULL(reversal, 0)
--  reversal — сумма bonus_amount по отделу/кварталу/году
--  (correction_quarter/_year) из margin_bonus_correction.
--  BDM-бонус (margin_bonus = margin * %) считается от @margin_department,
--  поэтому реверс в него попадает автоматически.
--  При ПУСТОЙ таблице reversal = 0 → вывод побитно равен прежнему.
--  Состав/порядок выходных колонок НЕ изменён. Базовая логика (UNION ALL,
--  зарплата с вычетом BDM-KPI, инвойсы) — дословно как в текущей фикс-версии.
-- ============================================================================

ALTER FUNCTION [dbo].[fn_margin_bonus_by_quarter]
(
    @date1 datetime,
    @date2 datetime
)
RETURNS @t TABLE (
    department_name      varchar(255),
    earned_money_department money,
    paid_money           money,
    margin_department    money,
    quat                 int,
    ya                   int,
    margin_bonus         money
)
AS
BEGIN

    DECLARE @tabl_itog TABLE (
        dep_id                  int,
        dep_name                varchar(50),
        money_earned_department money,
        paid_money              money,
        margin_department       money,
        quat                    int,
        ya                      int,
        margin_bonus            money
    )

    DECLARE @dep_id                 int
    DECLARE @dep_name               varchar(50)
    DECLARE @summ_vsego_zarabotano  money
    DECLARE @summ_vsego_potracheno  money
    DECLARE @margin_department      money
    DECLARE @quarter                int,
            @ya                     int
    DECLARE @percent                float

    DECLARE cur1 CURSOR FOR (

        SELECT dep_id,
               dep.dep_name,
               z1.summ_zarabotano_depatment,
               z1.sum_salary_depatment,
               -- [CORR] + реверс квартала
               (z1.summ_zarabotano_depatment - z1.sum_salary_depatment + ISNULL(corr.reversal,0)) AS margin,
               z1.salary_quarter,
               z1.salary_year
        FROM (
            SELECT z.sum_salary_depatment,
                   dep_id,
                   salary_quarter,
                   SUM(z.summ_zarabotano_depatment) OVER (
                       PARTITION BY dep_id, salary_quarter, salary_year
                   ) AS summ_zarabotano_depatment,
                   salary_year
            FROM (
                SELECT DISTINCT
                    SUM(allsumm_with_coef) OVER (
                        PARTITION BY dep_id, salary_quarter
                    ) AS sum_salary_depatment,
                    sel.salary_year,
                    sel.dep_id,
                    sel.salary_quarter,
                    ISNULL(tdepq.summ_total, 0) AS summ_zarabotano_depatment

                FROM (
                    -- зарплатные данные всех сотрудников (вычет бонуса BDM из маржи)
                    SELECT DISTINCT z.*,
                        ISNULL(z.allsumm, 0)
                        - ISNULL(z.bonus_bdm_kpi, 0)
                          * (ISNULL(z.allsumm, 0) / NULLIF(ISNULL(z.allsumm_without_coef, 0), 0))
                        AS allsumm_with_coef
                    FROM [dbo].[fn_salary_for_all_user](@date1, @date2, DEFAULT) z
                ) sel

                LEFT JOIN (
                    -- БЛОК ЗАРАБОТАННОГО (UNION ALL: акты без override + с override)
                    SELECT s.summ_total + ISNULL(s.sum_by_invoce, 0) AS summ_total,
                           s.depatment_id,
                           s.qrt,
                           s.ya
                    FROM (
                        SELECT DISTINCT
                            SUM(acts.summ_responsible_user) OVER (
                                PARTITION BY DATEPART(qq, acts.date_act),
                                             DATEPART(yy, acts.date_act),
                                             acts.depatment_id
                            ) AS summ_total,
                            ISNULL(inv_by_dep.sum_by_invoce, 0) AS sum_by_invoce,
                            DATEPART(yy, acts.date_act) AS ya,
                            acts.depatment_id,
                            DATEPART(qq, acts.date_act) AS qrt
                        FROM (
                            -- Ветка 1: акты БЕЗ override (нет строки в v_project_buh_actual)
                            SELECT pb.responsible_user_id,
                                   pb.summ_responsible_user,
                                   ab.date_act,
                                   pb.depatment_id
                            FROM dbo.act_buh ab
                            JOIN dbo.project_buh pb ON pb.act_id = ab.id
                            WHERE pb.responsible_user_id IS NOT NULL
                              AND CONVERT(date, ab.date_act) BETWEEN @date1 AND @date2
                              AND NOT EXISTS (
                                      SELECT 1
                                      FROM dbo.v_project_buh_actual x
                                      WHERE x.act_id = pb.act_id
                              )

                            UNION ALL

                            -- Ветка 2: акты С override (ровно одна строка на акт), дата = date_update
                            SELECT pba.responsible_user_id,
                                   pba.summ_responsible_user,
                                   pba.date_update AS date_act,
                                   pba.depatment_id
                            FROM dbo.v_project_buh_actual pba
                            WHERE pba.responsible_user_id IS NOT NULL
                              AND CONVERT(date, pba.date_update) BETWEEN @date1 AND @date2

                        ) acts

                        LEFT JOIN (
                            SELECT DISTINCT
                                (SUM(inv.summ * pb.percent_responsible_user_complicity / 100) OVER (
                                    PARTITION BY DATEPART(qq, inv.date_issue),
                                                 DATEPART(yy, inv.date_issue),
                                                 pb.depatment_id
                                )) / 1.2 AS sum_by_invoce,
                                pb.depatment_id,
                                DATEPART(qq, inv.date_issue) AS quar,
                                DATEPART(yy, inv.date_issue) AS ya
                            FROM invoice inv
                            JOIN project_buh pb ON pb.act_id = inv.act_id
                            WHERE inv.date_issue BETWEEN @date1 AND @date2
                        ) inv_by_dep
                            ON  inv_by_dep.depatment_id = acts.depatment_id
                            AND inv_by_dep.quar          = DATEPART(qq, acts.date_act)
                            AND inv_by_dep.ya            = DATEPART(yy, acts.date_act)

                        WHERE CONVERT(date, acts.date_act) BETWEEN @date1 AND @date2
                          AND acts.depatment_id NOT IN (7, 9, 10)
                    ) s
                ) tdepq
                    ON  tdepq.depatment_id = sel.dep_id
                    AND tdepq.qrt          = sel.salary_quarter

            ) z
        ) z1
        JOIN depatment dep ON dep.id = z1.dep_id
        -- [CORR] агрегат реверсов по отделу/кварталу/году (пустая таблица → reversal 0)
        LEFT JOIN (
            SELECT depatment_id, correction_quarter, correction_year, SUM(bonus_amount) AS reversal
            FROM dbo.margin_bonus_correction
            GROUP BY depatment_id, correction_quarter, correction_year
        ) corr
            ON  corr.depatment_id     = z1.dep_id
            AND corr.correction_quarter = z1.salary_quarter
            AND corr.correction_year    = z1.salary_year
    )

    OPEN cur1
    FETCH NEXT FROM cur1
        INTO @dep_id, @dep_name, @summ_vsego_zarabotano,
             @summ_vsego_potracheno, @margin_department, @quarter, @ya

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @formula1 DEC(12,4)
        DECLARE @formula2 DEC(12,4)

        -- процент бонуса по схеме BDM, актуальной на дату начала периода
        SET @percent = (
            SELECT TOP 1 gap_percent
            FROM scheme_limits_bdm sl
            JOIN gap g                  ON g.gap_id      = sl.gap_id
            JOIN division_structure ds  ON ds.division_id = sl.division_id
            WHERE sl.limits * 3 <= @margin_department
              AND ds.department_id = @dep_id
              AND sl.scheme_limits_date = (
                    SELECT MAX(s.scheme_limits_date)
                    FROM (
                        SELECT sl2.scheme_limits_date
                        FROM scheme_limits_bdm sl2
                        GROUP BY sl2.scheme_limits_date
                    ) s
                    WHERE @date1 >= s.scheme_limits_date
              )
            ORDER BY sl.limits DESC
        )

        SET @formula1 = (CAST(@margin_department AS DEC(12,4)) * @percent) / 100

        INSERT @tabl_itog
        VALUES (
            @dep_id,
            @dep_name,
            @summ_vsego_zarabotano,
            @summ_vsego_potracheno,
            @margin_department,
            @ya,
            @quarter,
            ISNULL(@formula1, 0)
        )

        FETCH NEXT FROM cur1
            INTO @dep_id, @dep_name, @summ_vsego_zarabotano,
                 @summ_vsego_potracheno, @margin_department, @quarter, @ya
    END

    CLOSE cur1
    DEALLOCATE cur1

    INSERT @t
    SELECT ti.dep_name,
           ti.money_earned_department,
           ti.paid_money,
           ti.margin_department,
           ti.ya,
           ti.quat,
           ti.margin_bonus
    FROM @tabl_itog ti
    ORDER BY ti.margin_department DESC

    RETURN
END
