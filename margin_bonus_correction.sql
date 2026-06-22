-- ============================================================================
--  Таблица-корректировка маржи: реверс заработанных бонусов из ЗАКРЫТЫХ кварталов
--  при failed-probation (Модель A — предыдущий квартал заморожен после выплаты).
-- ----------------------------------------------------------------------------
--  ЗАЧЕМ:
--    Когда кандидат не прошёл испытательный срок и акт переделывается другим
--    сотрудником, заработанный бонус СНЯТОГО участника уже учтён расходом в
--    закрытом (выплаченном) квартале. Перенести его нельзя — квартал заморожен.
--    Чтобы один акт не «оплачивался» двумя бонусами, в квартале изменения
--    проводим РЕВЕРС этого бонуса (уменьшаем расход → увеличиваем маржу).
--
--  ПРИМЕР (акт 1633, Industrial):
--    Q1 (заморожен): доход 194 400, расход бонус Черновой B_ч = 54 432.
--    Q2 (изменение): доход 0 (override), расход бонус Пивовар B_п ≈ 14 872.
--    Без реверса: 194 400 − 54 432 − 14 872  → переплата маржей на 54 432.
--    Реверс +54 432 в Q2  → итог 194 400 − 14 872. ВЕРНО.
--
--  ЗНАК: bonus_amount > 0 — это сумма бонуса, которую НАДО ВЕРНУТЬ
--        (т.е. в марже квартала correction_* прибавляется к марже /
--         вычитается из расхода).
--
--  ВАЖНО (предусловие Модели A):
--    Корректировка корректна ТОЛЬКО если закрытые кварталы НЕ пересчитываются
--    функциями вживую (используются замороженные/выплаченные снапшоты).
--    Если функцию запустить за source-квартал заново, акт из него выпадет
--    (override увёл дату на date_update) → бонус снятого там уже не учтётся,
--    и реверс станет двойным. Поэтому за закрытые кварталы отчёт берём из
--    снапшота, а не из повторного прогона.
-- ============================================================================

IF OBJECT_ID('dbo.margin_bonus_correction', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.margin_bonus_correction (
        id                int IDENTITY(1,1) NOT NULL CONSTRAINT PK_margin_bonus_correction PRIMARY KEY,
        act_id            int            NOT NULL,                 -- акт
        user_id           int            NOT NULL,                 -- снятый/заменённый участник
        user_name         nvarchar(200)  NULL,
        depatment_id      int            NOT NULL,                 -- отдел, к которому относится реверс
        bonus_amount      money          NOT NULL,                 -- заработанный бонус из закрытого квартала (>0), сумма реверса
        source_month      int            NOT NULL,                 -- месяц исходного акта (где бонус заморожен)
        source_year       int            NOT NULL,
        source_quarter    AS (((source_month - 1) / 3) + 1) PERSISTED,
        correction_month  int            NOT NULL,                 -- месяц изменения (date_update) — куда ставим реверс
        correction_year   int            NOT NULL,
        correction_quarter AS (((correction_month - 1) / 3) + 1) PERSISTED,
        date_update       datetime       NOT NULL,                 -- дата правки (failed probation)
        reason            varchar(100)   NULL CONSTRAINT DF_mbc_reason DEFAULT('failed_probation'),
        created_at        datetime       NOT NULL CONSTRAINT DF_mbc_created DEFAULT(GETDATE())
    );

    CREATE INDEX IX_mbc_dep_corr ON dbo.margin_bonus_correction (depatment_id, correction_year, correction_month);
    CREATE INDEX IX_mbc_act      ON dbo.margin_bonus_correction (act_id, user_id);
END
GO

-- ----------------------------------------------------------------------------
--  АГРЕГАТ ДЛЯ МАРЖИ: сумма реверсов по отделу / месяцу и по отделу / кварталу.
--  Маржинальные функции прибавляют это к марже (уменьшают расход).
-- ----------------------------------------------------------------------------
-- По месяцу (для fn_marginality_by_month):
--   SELECT depatment_id, correction_month mon, correction_year ya, SUM(bonus_amount) reversal
--   FROM dbo.margin_bonus_correction GROUP BY depatment_id, correction_month, correction_year;
--
-- По кварталу (для fn_margin_bonus_by_quarter):
--   SELECT depatment_id, correction_quarter qrt, correction_year ya, SUM(bonus_amount) reversal
--   FROM dbo.margin_bonus_correction GROUP BY depatment_id, correction_quarter, correction_year;
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  ХЕЛПЕР: эффективный квартал/год по дате с учётом правила закрытия.
--  Правило: квартал закрывается 20-го числа первого месяца СЛЕДУЮЩЕГО квартала
--    (Q1→20 апр, Q2→20 июл, Q3→20 окт, Q4→20 янв). До этой даты изменения ещё
--    попадают в предыдущий (отчётный) квартал, после — в текущий.
--  Грейс-период действует только в первом месяце квартала (1,4,7,10), дни 1..20.
-- ----------------------------------------------------------------------------
IF OBJECT_ID('dbo.fn_effective_quarter', 'IF') IS NOT NULL
    DROP FUNCTION dbo.fn_effective_quarter;
GO
CREATE FUNCTION dbo.fn_effective_quarter(@d datetime)
RETURNS TABLE
AS RETURN
(
    SELECT
        CASE WHEN MONTH(@d) IN (1,4,7,10) AND DAY(@d) <= 20
             THEN CASE WHEN DATEPART(qq,@d) = 1 THEN 4 ELSE DATEPART(qq,@d) - 1 END
             ELSE DATEPART(qq,@d)
        END AS eff_quarter,
        CASE WHEN MONTH(@d) = 1 AND DAY(@d) <= 20
             THEN YEAR(@d) - 1
             ELSE YEAR(@d)
        END AS eff_year
);
GO

-- Пример строки для акта 1633 (создаётся приложением при сохранении правки):
-- INSERT INTO dbo.margin_bonus_correction
--   (act_id, user_id, user_name, depatment_id, bonus_amount,
--    source_month, source_year, correction_month, correction_year, date_update, reason)
-- VALUES
--   (1633, 9670177, N'Чернова Юлия', 3, 54432,
--    3, 2026, 6, 2026, CONVERT(datetime,'20260622'), 'failed_probation');
