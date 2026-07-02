/* ============================================================================
   ФИКС: учёт возвратов/взаимозачётов (отрицательных платежей payment_buh)
         в маржинальной прибыли направлений — помесячно И поквартально.
   Применимо к ЛЮБОМУ направлению (не только Pharma).
   ----------------------------------------------------------------------------
   ПРОБЛЕМА (баг):
     Маржа (fn_marginality_by_month / fn_margin_bonus_by_quarter) считает доход
     из project_buh.summ_responsible_user, датируя его по act_buh.date_act, и
     НЕ читает payment_buh вообще. Возврат клиенту (взаимозачёт) заводится как
     ОТРИЦАТЕЛЬНЫЙ платёж в payment_buh (по payment_date текущего периода), а в
     project_buh акт УЖЕ уменьшают до нетто. Итог: уменьшение попадает в ЗАКРЫТЫЙ
     период самого акта, а в текущем периоде (когда реально вернули деньги) —
     не отражается. Страница «Акты»/выплата бонуса минус видят (они читают
     payment_buh), а маржа — нет.

   МОДЕЛЬ ИСПРАВЛЕНИЯ (перенос, total-preserving):
     Возврат R (без НДС) по акту A, распределённый на отдел d по доле d в акте:
       (1) период ВОЗВРАТА (по payment_date):  маржа d += (-R)   -- вычет сейчас
       (2) период АКТА     (по date_act):       маржа d += (+R)   -- восстановление
     Сумма (1)+(2) по направлению за всё время = 0 → лайфтайм-итог направления
     не меняется; возврат лишь ПЕРЕНОСИТСЯ в правильный период, а период акта
     возвращается к ИСХОДНОЙ (полной) сумме, с которой считали бонус BDM.

   ПРЕДПОСЫЛКА (проверена для всех 6 возвратов в базе на дату написания):
     сумма акта = net всех платежей  =>  project_buh хранит НЕТТО, поэтому
     add-back (+R) корректно восстанавливает исходную сумму. Если появится акт,
     где эта предпосылка нарушена (акт НЕ уменьшали), add-back даст завышение —
     такие акты ловит валидатор в конце файла.

   НДС: сумма возврата приводится к «без НДС» той же логикой, что в
        fn_GetPaymentDone (акты до 01.01.2026 — /1.2, с 01.01.2026 — /1.05,
        безНДС-акты total=total_no_nds — как есть).

   БЕЗОПАСНОСТЬ: изменения чисто АДДИТИВНЫЕ (+ ISNULL(retc.amt,0)). Если
        возвратов нет — представление пустое, retc.amt = NULL → +0 → вывод
        побитно равен прежнему. Состав/порядок выходных колонок НЕ меняется.
   ============================================================================ */


/* ============================================================================
   ШАГ 1. Представление-источник коррекций.
   Две строки на каждый возврат: RETURN (минус, период платежа) и
   ADDBACK (плюс, период акта). Распределение на отделы — по доле отдела
   в акте (по summ_responsible_user).
   ============================================================================ */
CREATE OR ALTER VIEW dbo.v_margin_return_corrections
AS
WITH ret AS (
    -- один возврат = одна отрицательная строка payment_buh; сумма БЕЗ НДС, положительная
    SELECT
        pb.id        AS payment_id,
        pb.act_id,
        ab.date_act,
        pb.payment_date,
        ABS(ROUND(
            CASE WHEN ab.total = ab.total_no_nds THEN pb.sum
                 WHEN CONVERT(date, ab.date_act) <= CONVERT(date, '20260101') THEN pb.sum / 1.2
                 ELSE pb.sum / 1.05 END
        , 2)) AS ret_no_nds
    FROM dbo.payment_buh pb
    JOIN dbo.act_buh ab ON ab.id = pb.act_id
    WHERE pb.sum < 0
      AND ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО'
      AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО'
),
share AS (
    -- доля отдела в акте (по summ_responsible_user); поддержка мультиотдельных актов
    SELECT act_id, depatment_id,
           CAST(SUM(summ_responsible_user) AS float)
             / NULLIF(SUM(SUM(summ_responsible_user)) OVER (PARTITION BY act_id), 0) AS dep_fraction
    FROM dbo.project_buh
    WHERE responsible_user_id IS NOT NULL
    GROUP BY act_id, depatment_id
)
-- (1) ВЫЧЕТ возврата в периоде платежа-возврата
SELECT s.depatment_id,
       DATEPART(yy, r.payment_date) AS ya,
       DATEPART(mm, r.payment_date) AS mon,
       DATEPART(qq, r.payment_date) AS quat,
       CONVERT(money, -1 * ROUND(r.ret_no_nds * s.dep_fraction, 2)) AS amount,
       r.act_id, r.payment_id,
       CONVERT(varchar(24), 'RETURN_IN_PAYMENT_PERIOD') AS kind
FROM ret r
JOIN share s ON s.act_id = r.act_id

UNION ALL

-- (2) ВОССТАНОВЛЕНИЕ исходного начисления в периоде акта
SELECT s.depatment_id,
       DATEPART(yy, r.date_act) AS ya,
       DATEPART(mm, r.date_act) AS mon,
       DATEPART(qq, r.date_act) AS quat,
       CONVERT(money, ROUND(r.ret_no_nds * s.dep_fraction, 2)) AS amount,
       r.act_id, r.payment_id,
       CONVERT(varchar(24), 'ADDBACK_IN_ACT_PERIOD') AS kind
FROM ret r
JOIN share s ON s.act_id = r.act_id;
GO


/* ============================================================================
   ШАГ 2a. fn_marginality_by_month — добавлен вычет/восстановление возвратов.
   Отличия от текущей фикс-версии помечены -- [RET].
   ============================================================================ */
CREATE OR ALTER FUNCTION [dbo].[fn_marginality_by_month]
(
    @date1 date,
    @date2 date
)
RETURNS TABLE
AS
RETURN
(
    select
        dep.dep_name,
        isnull(cast(z1.summ_zarabotano_depatment as money), 0) summ_zarabotano_depatment,
        isnull(cast(z1.sum_salary_depatment as money), 0) sum_salary_depatment,
        -- [CORR] + реверс месяца  -- [RET] + перенос возвратов месяца
        isnull(cast((z1.summ_zarabotano_depatment - z1.sum_salary_depatment
                     + isnull(corr.reversal,0) + isnull(retc.amt,0)) as money), 0) margin,
        (SELECT [dbo].[fn_get_month_name_by_int](z1.salary_month)) salary_month,
        z1.salary_year,
        isnull(cast(SUM(z1.summ_zarabotano_depatment) over (partition by z1.dep_id) as money), 0) summ_vsego_zarabotano,
        isnull(cast(SUM(z1.sum_salary_depatment) over (partition by z1.dep_id) as money), 0) summ_vsego_potracheno,
        -- [CORR] + суммарный реверс по отделу  -- [RET] + суммарный перенос возвратов по отделу
        isnull(cast((SUM(z1.summ_zarabotano_depatment) over (partition by z1.dep_id)
                     - SUM(z1.sum_salary_depatment) over (partition by z1.dep_id)
                     + SUM(isnull(corr.reversal,0)) over (partition by z1.dep_id)
                     + SUM(isnull(retc.amt,0)) over (partition by z1.dep_id)) as money), 0) vsego_margin
    from (
        select
            dep_id,
            salary_month,
            salary_year,
            sum(sum_salary_depatment) as sum_salary_depatment,
            sum(summ_zarabotano_depatment) as summ_zarabotano_depatment
        from (
            select
                dep_id,
                salary_month,
                salary_year,
                sum(allsumm_with_coef) as sum_salary_depatment,
                isnull(max(tdepq.summ_total), 0) as summ_zarabotano_depatment
            from (
            -- исправления - убрала бонус бдм из маржи
                SELECT z.*,
                    ISNULL(z.allsumm, 0)
                    - ISNULL(z.bonus_bdm_kpi, 0)
                      * (ISNULL(z.allsumm, 0) / NULLIF(ISNULL(z.allsumm_without_coef, 0), 0))
                    AS allsumm_with_coef
                FROM [dbo].[fn_salary_for_all_user](@date1, @date2, DEFAULT) z
            ) sel
            left join (
                select
                    s.summ_total + isnull(sum_by_invoce, 0) as summ_total,
                    s.depatment_id,
                    s.mon,
                    s.ya
                from (
                    SELECT DISTINCT
                        sum(summ_responsible_user) over(PARTITION BY datepart(mm, date_act), datepart(yy,date_act), z1.depatment_id) as summ_total,
                        (isnull(sum_by_invoce, 0)) as sum_by_invoce,
                        datepart(yy,date_act) as ya,
                        z1.depatment_id,
                        datepart(mm, date_act) as mon
                    FROM ( -- добавила таблицу с измененными актами в связи с непрошедшими испытательный срок
                            SELECT
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.responsible_user_id   ELSE pb.responsible_user_id   END AS responsible_user_id,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.summ_responsible_user ELSE pb.summ_responsible_user END AS summ_responsible_user,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.date_update ELSE ab.date_act END AS date_act,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.depatment_id  ELSE pb.depatment_id END AS depatment_id
                            FROM dbo.act_buh ab
                            JOIN dbo.project_buh pb  ON pb.act_id = ab.id
                            LEFT JOIN dbo.v_project_buh_actual pba ON pba.act_id = pb.act_id
                            WHERE
                                  (
                                      CONVERT(date, ab.date_act) BETWEEN @date1 AND @date2
                                      OR CONVERT(date, pba.date_update) BETWEEN @date1 AND @date2
                                  )
                            AND pb.responsible_user_id IS NOT NULL
                            AND pb.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                    ) z1
                    left join (
                        select distinct
                             (sum(inv.summ*pb.percent_responsible_user_complicity/100) over(
                                PARTITION BY datepart(mm, inv.date_issue), datepart(yy,inv.date_issue), pb.depatment_id))/1.2 as sum_by_invoce,
                            pb.depatment_id,
                            datepart(mm, inv.date_issue) as mm,
                            datepart(yy, inv.date_issue) as ya
                        from invoice inv
                        join project_buh pb on pb.act_id = inv.act_id
                        where inv.date_issue between @date1 AND @date2
                    ) inv_by_dep on inv_by_dep.depatment_id = z1.depatment_id
                        and inv_by_dep.mm = datepart(mm, date_act)
                        and inv_by_dep.ya = datepart(yy,date_act)
                    WHERE convert(date, date_act) BETWEEN @date1 AND @date2
                        AND z1.depatment_id not in (7,9,10)
                ) s
            ) tdepq on tdepq.depatment_id = dep_id
                and tdepq.mon = salary_month
                and tdepq.ya = salary_year
            group by dep_id, salary_month, salary_year
        ) z
        group by dep_id, salary_month, salary_year
    ) z1
    join depatment dep on dep.id = z1.dep_id
    -- [CORR] агрегат реверсов по отделу/месяцу/году (пустая таблица → нет строк → reversal 0)
    left join (
        SELECT depatment_id, correction_month, correction_year, SUM(bonus_amount) AS reversal
        FROM dbo.margin_bonus_correction
        GROUP BY depatment_id, correction_month, correction_year
    ) corr
        on corr.depatment_id = z1.dep_id
       and corr.correction_month = z1.salary_month
       and corr.correction_year = z1.salary_year
    -- [RET] агрегат переносов возвратов по отделу/месяцу/году (пусто → amt 0)
    left join (
        SELECT depatment_id, ya, mon, SUM(amount) AS amt
        FROM dbo.v_margin_return_corrections
        GROUP BY depatment_id, ya, mon
    ) retc
        on retc.depatment_id = z1.dep_id
       and retc.mon = z1.salary_month
       and retc.ya  = z1.salary_year
)
GO


/* ============================================================================
   ШАГ 2b. fn_margin_bonus_by_quarter — добавлен вычет/восстановление возвратов.
   Бонус BDM (margin_bonus = margin * %) считается от @margin_department,
   поэтому перенос возврата автоматически влияет и на бонус BDM.
   Отличия помечены -- [RET].
   ============================================================================ */
CREATE OR ALTER FUNCTION [dbo].[fn_margin_bonus_by_quarter]
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
               -- [CORR] + реверс квартала  -- [RET] + перенос возвратов квартала
               (z1.summ_zarabotano_depatment - z1.sum_salary_depatment
                    + ISNULL(corr.reversal,0) + ISNULL(retc.amt,0)) AS margin,
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
        -- [RET] агрегат переносов возвратов по отделу/кварталу/году (пусто → amt 0)
        LEFT JOIN (
            SELECT depatment_id, ya, quat, SUM(amount) AS amt
            FROM dbo.v_margin_return_corrections
            GROUP BY depatment_id, ya, quat
        ) retc
            ON  retc.depatment_id = z1.dep_id
            AND retc.quat         = z1.salary_quarter
            AND retc.ya           = z1.salary_year
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
GO


/* ============================================================================
   ШАГ 3 (проверки — выполнять ДО и ПОСЛЕ применения).
   ============================================================================ */

-- 3.1 Что именно скорректируется (по отделам и периодам). Сумма amount по каждому
--     act_id должна быть 0 (перенос total-preserving):
--   SELECT depatment_id, ya, mon, quat, kind, act_id, amount
--   FROM dbo.v_margin_return_corrections ORDER BY act_id, kind;

-- 3.2 Нетто-дельта маржи по отделу/месяцу (то, на сколько изменится margin помесячно):
--   SELECT depatment_id, ya, mon, SUM(amount) AS delta_month
--   FROM dbo.v_margin_return_corrections GROUP BY depatment_id, ya, mon
--   ORDER BY ya, mon, depatment_id;

-- 3.3 ВАЛИДАТОР предпосылки (акт = net платежей). Пусто = всё ок и add-back точен.
--     Строки в выводе = акты, где акт НЕ уменьшали до нетто → add-back даст
--     завышение периода акта, разобрать вручную:
--   SELECT pb.act_id, ab.total_no_nds AS act_now,
--          ROUND(SUM(CASE WHEN ab.total = ab.total_no_nds THEN pb.sum
--                         WHEN CONVERT(date, ab.date_act) <= CONVERT(date,'20260101') THEN pb.sum/1.2
--                         ELSE pb.sum/1.05 END), 2) AS net_pay_no_nds
--   FROM payment_buh pb JOIN act_buh ab ON ab.id = pb.act_id
--   WHERE pb.act_id IN (SELECT act_id FROM payment_buh WHERE sum < 0)
--   GROUP BY pb.act_id, ab.total_no_nds
--   HAVING ABS(ab.total_no_nds
--          - ROUND(SUM(CASE WHEN ab.total = ab.total_no_nds THEN pb.sum
--                           WHEN CONVERT(date, ab.date_act) <= CONVERT(date,'20260101') THEN pb.sum/1.2
--                           ELSE pb.sum/1.05 END),2)) > 1;

/* ----------------------------------------------------------------------------
   ОТКАТ (если понадобится): восстановить обе функции из их предыдущих
   фикс-версий (без блоков -- [RET]) и выполнить DROP VIEW dbo.v_margin_return_corrections.
   Так как правки аддитивны, до применения снимите скрипты текущих определений:
     SELECT OBJECT_DEFINITION(OBJECT_ID('dbo.fn_marginality_by_month'));
     SELECT OBJECT_DEFINITION(OBJECT_ID('dbo.fn_margin_bonus_by_quarter'));
   ---------------------------------------------------------------------------- */
