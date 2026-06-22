-- ============================================================================
--  Тест consume-стороны: одна строка реверса (как запишет Java для акта 1633)
--  → измениться должна ТОЛЬКО Industrial (реверс в Q2 / июнь). Транзакция с ROLLBACK.
-- ============================================================================
SET NOCOUNT ON;
BEGIN TRAN;

INSERT INTO dbo.margin_bonus_correction
 (act_id, user_id, user_name, depatment_id, bonus_amount,
  source_month, source_year, correction_month, correction_year, date_update, reason)
VALUES
 (1633, 9670177, N'Чернова Юлия Валерьевна', 3, 54432,
  3, 2026, 6, 2026, GETDATE(), 'failed_probation');

-- 1) Квартальная маржа. Ожидание: всё как в эталоне, КРОМЕ Industrial Q2:
--    margin 2 098 796.11 -> 2 153 228.11 (+54 432); BDM 209 879.61 -> ~215 322.81 (10%).
SELECT 'Q1' p, department_name, margin_department, margin_bonus
FROM dbo.fn_margin_bonus_by_quarter('20260101','20260331')
UNION ALL
SELECT 'Q2', department_name, margin_department, margin_bonus
FROM dbo.fn_margin_bonus_by_quarter('20260401','20260630')
ORDER BY p, department_name;

-- 2) Помесячная маржа Industrial. Ожидание: всё как в эталоне, КРОМЕ Июнь:
--    margin 907 092.42 -> 961 524.42 (+54 432). Остальные месяцы без изменений.
SELECT dep_name, salary_month, salary_year, summ_zarabotano_depatment, sum_salary_depatment, margin
FROM dbo.fn_marginality_by_month('20260101','20260630')
WHERE dep_name = 'Industrial'
ORDER BY salary_year, salary_month;

ROLLBACK;

-- 3) Контроль отката: таблица снова пуста.
SELECT 'after rollback' info, COUNT(*) rows_left FROM dbo.margin_bonus_correction;
