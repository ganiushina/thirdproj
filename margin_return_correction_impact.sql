/* ============================================================================
   ОЦЕНКА ВЛИЯНИЯ фикса возвратов на маржу и бонус BDM (до/после).
   Пара к margin_return_correction.sql.
   ----------------------------------------------------------------------------
   ПОРЯДОК ЗАПУСКА:
     1) ЧАСТЬ 1 — выполнить ДО применения margin_return_correction.sql
        (снимает базовые значения текущих функций в staging-таблицы).
     2) Применить margin_return_correction.sql (view + ALTER обеих функций).
     3) ЧАСТЬ 2 — выполнить ПОСЛЕ применения (сравнение до/после + очистка).

   Периоды берутся автоматически из payment_buh (возвраты): затрагиваются
   и период акта (add-back), и период платежа-возврата. Функции вызываются
   по одному кварталу/месяцу через CROSS APPLY, результат раскладывается по
   направлениям. Строки с нулевой дельтой в итоговый отчёт не попадают.
   ============================================================================ */


/* ========================= ЧАСТЬ 1 — СНЯТЬ БАЗУ (ДО ФИКСА) ================== */

IF OBJECT_ID('dbo.z_margin_impact_baseline_q') IS NOT NULL DROP TABLE dbo.z_margin_impact_baseline_q;
IF OBJECT_ID('dbo.z_margin_impact_baseline_m') IS NOT NULL DROP TABLE dbo.z_margin_impact_baseline_m;

-- --- квартальная база ---
WITH neg AS (
    SELECT ab.date_act, pb.payment_date
    FROM dbo.payment_buh pb JOIN dbo.act_buh ab ON ab.id = pb.act_id
    WHERE pb.sum < 0
      AND ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО'
      AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО'
),
periods AS (
    SELECT DATEPART(yy, date_act)     AS ya, DATEPART(qq, date_act)     AS quat FROM neg
    UNION
    SELECT DATEPART(yy, payment_date) AS ya, DATEPART(qq, payment_date) AS quat FROM neg
),
q AS (
    SELECT ya, quat,
           DATEFROMPARTS(ya, (quat-1)*3+1, 1)        AS qstart,
           EOMONTH(DATEFROMPARTS(ya, quat*3, 1))     AS qend
    FROM periods
)
SELECT q.ya, q.quat, f.department_name,
       f.margin_department AS margin_before,
       f.margin_bonus      AS bonus_before
INTO dbo.z_margin_impact_baseline_q
FROM q
CROSS APPLY dbo.fn_margin_bonus_by_quarter(CONVERT(datetime, q.qstart), CONVERT(datetime, q.qend)) f;

-- --- месячная база ---
WITH neg AS (
    SELECT ab.date_act, pb.payment_date
    FROM dbo.payment_buh pb JOIN dbo.act_buh ab ON ab.id = pb.act_id
    WHERE pb.sum < 0
      AND ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО'
      AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО'
),
periods AS (
    SELECT DATEPART(yy, date_act)     AS ya, DATEPART(mm, date_act)     AS mon FROM neg
    UNION
    SELECT DATEPART(yy, payment_date) AS ya, DATEPART(mm, payment_date) AS mon FROM neg
),
m AS (
    SELECT ya, mon,
           DATEFROMPARTS(ya, mon, 1)            AS mstart,
           EOMONTH(DATEFROMPARTS(ya, mon, 1))   AS mend
    FROM periods
)
SELECT m.ya, m.mon, f.dep_name,
       f.margin AS margin_before
INTO dbo.z_margin_impact_baseline_m
FROM m
CROSS APPLY dbo.fn_marginality_by_month(m.mstart, m.mend) f;

PRINT 'ЧАСТЬ 1 выполнена: база снята. Теперь применить margin_return_correction.sql, затем ЧАСТЬ 2.';


/* ========================= ЧАСТЬ 2 — СРАВНЕНИЕ (ПОСЛЕ ФИКСА) ================
   Выполнять ПОСЛЕ применения margin_return_correction.sql.
   Раскомментировать блок ниже.
   =========================================================================== */
/*

-- --- 2.1 Квартальная дельта: маржа и бонус BDM ---
WITH neg AS (
    SELECT ab.date_act, pb.payment_date
    FROM dbo.payment_buh pb JOIN dbo.act_buh ab ON ab.id = pb.act_id
    WHERE pb.sum < 0
      AND ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО'
      AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО'
),
periods AS (
    SELECT DATEPART(yy, date_act) ya, DATEPART(qq, date_act) quat FROM neg
    UNION SELECT DATEPART(yy, payment_date), DATEPART(qq, payment_date) FROM neg
),
q AS (
    SELECT ya, quat, DATEFROMPARTS(ya,(quat-1)*3+1,1) qstart, EOMONTH(DATEFROMPARTS(ya,quat*3,1)) qend
    FROM periods
),
after_q AS (
    SELECT q.ya, q.quat, f.department_name,
           f.margin_department AS margin_after, f.margin_bonus AS bonus_after
    FROM q CROSS APPLY dbo.fn_margin_bonus_by_quarter(CONVERT(datetime,q.qstart), CONVERT(datetime,q.qend)) f
)
SELECT a.ya, a.quat, a.department_name,
       b.margin_before, a.margin_after, (a.margin_after - b.margin_before) AS margin_delta,
       b.bonus_before,  a.bonus_after,  (a.bonus_after  - b.bonus_before)  AS bonus_bdm_delta
FROM after_q a
JOIN dbo.z_margin_impact_baseline_q b
  ON b.ya = a.ya AND b.quat = a.quat AND b.department_name = a.department_name
WHERE ABS(a.margin_after - b.margin_before) > 0.5
   OR ABS(a.bonus_after  - b.bonus_before)  > 0.5
ORDER BY a.ya, a.quat, a.department_name;


-- --- 2.2 Месячная дельта: маржа ---
WITH neg AS (
    SELECT ab.date_act, pb.payment_date
    FROM dbo.payment_buh pb JOIN dbo.act_buh ab ON ab.id = pb.act_id
    WHERE pb.sum < 0
      AND ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО'
      AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО'
),
periods AS (
    SELECT DATEPART(yy, date_act) ya, DATEPART(mm, date_act) mon FROM neg
    UNION SELECT DATEPART(yy, payment_date), DATEPART(mm, payment_date) FROM neg
),
m AS (
    SELECT ya, mon, DATEFROMPARTS(ya,mon,1) mstart, EOMONTH(DATEFROMPARTS(ya,mon,1)) mend FROM periods
),
after_m AS (
    SELECT m.ya, m.mon, f.dep_name, f.margin AS margin_after
    FROM m CROSS APPLY dbo.fn_marginality_by_month(m.mstart, m.mend) f
)
SELECT a.ya, a.mon, a.dep_name,
       b.margin_before, a.margin_after, (a.margin_after - b.margin_before) AS margin_delta
FROM after_m a
JOIN dbo.z_margin_impact_baseline_m b
  ON b.ya = a.ya AND b.mon = a.mon AND b.dep_name = a.dep_name
WHERE ABS(a.margin_after - b.margin_before) > 0.5
ORDER BY a.ya, a.mon, a.dep_name;


-- --- 2.3 Кросс-проверка: ожидаемая дельта маржи прямо из представления ---
--     (должна совпасть с margin_delta из 2.1/2.2)
SELECT d.dep_name AS department_name, c.ya, c.quat,
       SUM(c.amount) AS expected_margin_delta_quarter
FROM dbo.v_margin_return_corrections c
JOIN dbo.depatment d ON d.id = c.depatment_id
GROUP BY d.dep_name, c.ya, c.quat
ORDER BY c.ya, c.quat, d.dep_name;


-- --- 2.4 Очистка staging ---
-- DROP TABLE dbo.z_margin_impact_baseline_q;
-- DROP TABLE dbo.z_margin_impact_baseline_m;

*/
