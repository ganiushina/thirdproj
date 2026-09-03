/* ============================================================================
   Удаление дубля акта 00АП-00090 2026 (СЕРВЬЕ АО, Семилетова, 22.06.2026)
   Дата: 28.08.2026. Автор: Claude по задаче Е. Ганюшиной (запрос бухгалтерии).

   ЧТО ЗА ДУБЛЬ
     act_buh id = 1688  «00АП-00090 2026»  160 650,00 / 153 000,00 без НДС
     act_buh id = 1689  «00АП-00091 2026»  160 650,00 / 153 000,00 без НДС
     Одна и та же компания, проект 16862409, кандидат и дата.
     Оплата пришла ТОЛЬКО по 1689 (payment_buh, 27.08.2026, 160 650,00).
     Значит лишний — 1688.

   ЧЕМ ОН МЕШАЕТ СЕЙЧАС
     1. «Всего неоплачено актов» завышено на 160 650,00.
     2. fn_marginality_by_month считает выручку по act_buh + project_buh
        (по дате акта, не по оплате) -> июнь 2026, Consumer Goods Production
        завышен на 15 300,00 (строка project_buh id = 19573).
        Q2'2026 CGP: маржа 1 362 543,65 -> 1 347 243,65, бонус БДМ 5%
        68 127,18 -> 67 362,18 (-765,00). Квартал закрыт — учесть при сверке.
     3. НЕ мешает: бонусов по акту 1688 никто не получил (paymentSuccess пуст),
        строка ресечера Пивовар и так посчитана по акту 1689.

   ВАЖНО — почему нельзя просто удалить акт
     На акт 1688 повешен доп. бонус 5% Ивановой Лины (extra_bonus id = 303,
     заведён 23.06.2026). Он ЗАКОННЫЙ — просто привязан к дублю.
     fn_User_Bonus_by_Details_New джойнит extra_bonus с act_buh, поэтому после
     удаления акта строка «Семилетова 5% доп» = 7 650,00 за июнь 2026 у Ивановой
     ПРОПАДЁТ. Поэтому сначала перевешиваем доп.бонус на реальный акт 1689
     (у него та же дата и та же сумма без НДС -> 5% = те же 7 650,00).

   ЧТО ЕЩЁ ССЫЛАЕТСЯ НА 1688 (проверено): project_buh — 1 строка (19573),
     extra_bonus — 1 строка (303). В payment_buh, paymentSuccess, invoice,
     project_buh_failed_probation_period, margin_bonus_correction — пусто.

   ОТКАТ — в конце файла.
   ============================================================================ */

USE recruting;
GO

-- ---------------------------------------------------------------------------
-- ШАГ 0. Контроль ДО правки
-- ---------------------------------------------------------------------------
SELECT 'act_buh'     AS src, id, act_num, date_act, company_name, total, total_no_nds, candidate
FROM   dbo.act_buh WHERE id IN (1688, 1689);

SELECT 'project_buh' AS src, id, act_id, responsible_user_name, resecher_name,
       summ_responsible_user, summ_resecher, depatment
FROM   dbo.project_buh WHERE act_id IN (1688, 1689);

SELECT 'extra_bonus' AS src, * FROM dbo.extra_bonus WHERE act_id IN (1688, 1689);

SELECT 'payment_buh' AS src, * FROM dbo.payment_buh WHERE act_id IN (1688, 1689);

-- Бонус Ивановой Лины за июнь 2026 ДО правки (ожидаем 7 650,00 по акту 1688)
SELECT 'bonus_before' AS src, man_fio, dep_name, money_by_candidate, act_id, candidate
FROM   dbo.fn_User_Bonus_by_Details_New('20260601','20260630',0,0)
WHERE  act_id IN (1688, 1689);
GO

-- ---------------------------------------------------------------------------
-- ШАГ 1. Правка
-- ---------------------------------------------------------------------------
BEGIN TRANSACTION;

-- 1.1 доп. бонус 5% Ивановой Лины перевешиваем с дубля на реальный акт
UPDATE dbo.extra_bonus
SET    act_id = 1689
WHERE  id = 303 AND act_id = 1688;          -- ожидается 1 строка

-- 1.2 распределение дубля
DELETE FROM dbo.project_buh
WHERE  id = 19573 AND act_id = 1688;        -- ожидается 1 строка

-- 1.3 сам дубль
DELETE FROM dbo.act_buh
WHERE  id = 1688;                           -- ожидается 1 строка

-- Проверить, что затронуто ровно 3 строки (по одной на команду), затем:
COMMIT TRANSACTION;
-- ROLLBACK TRANSACTION;   -- если что-то не сошлось
GO

-- ---------------------------------------------------------------------------
-- ШАГ 2. Контроль ПОСЛЕ правки
-- ---------------------------------------------------------------------------
-- акта 1688 нет, 1689 на месте
SELECT id, act_num, total, total_no_nds FROM dbo.act_buh WHERE id IN (1688, 1689);

-- доп. бонус висит на 1689
SELECT * FROM dbo.extra_bonus WHERE id = 303;

-- Иванова Лина за июнь 2026 по-прежнему получает 7 650,00, теперь по акту 1689
SELECT man_fio, dep_name, money_by_candidate, act_id, candidate
FROM   dbo.fn_User_Bonus_by_Details_New('20260601','20260630',0,0)
WHERE  act_id = 1689;

-- маржа CGP: июнь 2026 выручка -15 300,00
SELECT dep_name, summ_zarabotano_depatment, sum_salary_depatment, margin, salary_month, salary_year
FROM   dbo.fn_marginality_by_month('20260601','20260630')
WHERE  dep_name = 'Consumer Goods Production';

-- квартал Q2'2026: ожидается маржа 1 347 243,65 и бонус БДМ 67 362,18
SELECT * FROM dbo.fn_margin_bonus_by_quarter('20260401','20260630');

-- акт 1688 ушёл из «неоплаченных»
SELECT act_id, act_num, paied, total_no_nds, sum, payment_date
FROM   dbo.fn_GetPaymentDone('20260601','20260831')
WHERE  act_id IN (1688, 1689);
GO


/* ============================================================================
   ОТКАТ (вернуть всё как было)
   ============================================================================
SET IDENTITY_INSERT dbo.act_buh ON;

INSERT INTO dbo.act_buh
      (id, date_act, act_num, company_name, company_id, total, total_no_nds,
       currency, count_employed, project_name, project_id, classification_name,
       candidate, organization)
VALUES(1688, '20260622 14:00:00', N'00АП-00090 2026', N'СЕРВЬЕ АО', 0,
       160650.0, 153000.0, N'руб.', 1,
       N'Servier Pharmaceuticals Moscow (08/08/2025 Секретарь (отдел Маркетинг))',
       16862409,
       N'Услуги по поиску и подбору персонала для СЕРВЬЕ АО (АП)',
       'Семилетова от 22.06.2026', 'АЛЬТА ПЕРСОНАЛ ООО');

SET IDENTITY_INSERT dbo.act_buh OFF;

SET IDENTITY_INSERT dbo.project_buh ON;

INSERT INTO dbo.project_buh
      (id, act_id, responsible_user_name, responsible_user_id, resecher_name, resecher_id,
       summ_responsible_user, summ_resecher, depatment, depatment_id,
       percent_responsible_user_complicity, percent_resecher_complicity,
       teame_leader_name, teame_leader_id, city_responsible_user, city_responsible_user_id,
       percent_responsible_user_by_candidate_percent, percent_reseacher_by_candidate_percent,
       depatment_resecher, depatment_resecher_id)
VALUES(19573, 1688, N'Направление CGP', 15, N'Пивовар Янна', 15865624,
       15300.0, 137700.0, N'Consumer Goods Production', 15,
       10.0, 90.0, N'Направление CGP', 8715253, N'Самара', 8715253,
       1.0, 0.9, 'Consumer Goods Production', 15);

SET IDENTITY_INSERT dbo.project_buh OFF;

UPDATE dbo.extra_bonus SET act_id = 1688 WHERE id = 303;
   ============================================================================ */
