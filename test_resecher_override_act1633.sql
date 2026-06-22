-- ============================================================================
--  ТЕСТ: смена состава акта 1633 через «Изменить» и расчёт бонуса ресечера
--  по дате изменения (date_update). НЕДЕСТРУКТИВНЫЙ — всё в транзакции с ROLLBACK.
-- ----------------------------------------------------------------------------
--  ПРЕДУСЛОВИЕ: сначала применить правку
--      fn_User_Bonus_by_Details_New_resecher_override.sql
--  (без неё ветка ресечера не видит override и Пивовар не появится).
--
--  СЦЕНАРИЙ (как в форме):
--    Акт 1633, total_no_nds = 194 400, дата акта 13.03.2026.
--    Было: Новоженина 30% (58 320) + Чернова 70% (136 080), ресечера нет.
--    Действие: удалить консультанта Чернову, Новоженину сделать 100% (194 400),
--              добавить ресечера Пивовар Янна 85% (165 240), отдел Industrial(3).
--    date_update = 22.06.2026 → бонус считается в ИЮНЕ 2026.
--
--  ЗАПУСКАТЬ НА ТЕСТОВОЙ БАЗЕ. По завершении транзакция откатывается —
--  данные не меняются.
-- ============================================================================

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRAN;

-- 0) Исходный состав акта (для протокола)
SELECT 'BEFORE / project_buh' AS stage, id, responsible_user_name, summ_responsible_user,
       percent_responsible_user_complicity, resecher_name, summ_resecher, depatment_id, depatment_resecher_id
FROM dbo.project_buh WHERE act_id = 1633 ORDER BY id;

-- 1) Имитация сохранения формы: кнопка «Изменить» делает DELETE+INSERT в PFPP.
--    Чернова удалена, Новоженина = 100%, добавлен ресечер Пивовар.
DELETE FROM dbo.project_buh_failed_probation_period WHERE act_id = 1633;

INSERT INTO dbo.project_buh_failed_probation_period
    (act_id, depatment, depatment_id,
     responsible_user_name, responsible_user_id, summ_responsible_user,
     percent_responsible_user_by_candidate_percent, percent_responsible_user_complicity,
     resecher_name, resecher_id, summ_resecher,
     percent_reseacher_by_candidate_percent, percent_resecher_complicity,
     depatment_resecher, depatment_resecher_id,
     teame_leader_name, teame_leader_id, city_responsible_user, city_responsible_user_id, date_update)
VALUES
    (1633, N'Industrial', 3,
     N'Новоженина Дарья', 9421219, 194400,
     1.0, 100,
     N'Пивовар Янна', 15865624, 165240,
     0.85, 85,
     N'Industrial', 3,
     N'Новоженина Дарья', 8715253, NULL, NULL, CONVERT(datetime, '2026-06-22'));

-- 2) Что увидит override-view (для контроля сторно/начислений)
SELECT 'v_project_buh_actual' AS stage, act_id, responsible_user_name, summ_responsible_user,
       resecher_name, summ_resecher, depatment_id, depatment_resecher_id, date_update
FROM dbo.v_project_buh_actual WHERE act_id = 1633;

-- 3) Прогон функции за ИЮНЬ 2026 — бонус по акту 1633
--    Ожидание:
--      • Пивовар Янна: month=Июнь. Ставка зависит от её СУММАРНОГО месячного
--        ресечерского оборота (summ_total), а НЕ от одного акта:
--          база июня 1684(243 432) + 1687(193 103,4) = 436 535,4
--          + акт 1633 (165 240) = 601 775,4 → диапазон ≥470k и <720k → 9%.
--        money_by_candidate(1633) = 165 240 * 9% = 14 871,6.
--        Заодно акты 1684/1687 поднимутся с 7% до 9% — ставка общая по месяцу.
--        Пороги поз.25 «R1 Эксперт»/схема2: 0→5%, ≥350k→7%, ≥470k→9%, ≥720k→11%.
--      • Новоженина Дарья: учтена как 100% (194 400) консультанта,
--        бонус по её июньской схеме/лимитам.
--      • Чернова Юлия: в июне сторнируется (отрицательная коррекция).
SELECT 'FUNCTION June-2026' AS stage, man_fio, pos_name, dep_name, mont,
       summ_user, persent, money_by_candidate, money_itog, act_id
FROM dbo.fn_User_Bonus_by_Details_New('2026-06-01', '2026-06-30', 0, 0)
WHERE act_id = 1633
ORDER BY man_fio;

-- 4) Контроль: за МАРТ 2026 по акту 1633 ресечера быть НЕ должно
--    (начисление переехало в месяц изменения — июнь).
SELECT 'FUNCTION March-2026' AS stage, man_fio, mont, summ_user, persent, money_by_candidate, act_id
FROM dbo.fn_User_Bonus_by_Details_New('2026-03-01', '2026-03-31', 0, 0)
WHERE act_id = 1633
ORDER BY man_fio;

-- 5) Откат — ничего не сохраняем
ROLLBACK TRAN;

-- Проверка, что PFPP вернулся в исходное состояние (по акту 1633 — пусто)
SELECT 'AFTER ROLLBACK / pfpp act 1633' AS stage, COUNT(*) AS rows_left
FROM dbo.project_buh_failed_probation_period WHERE act_id = 1633;
