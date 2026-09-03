/* ============================================================================
   Чистка userplanByMonth — ТОЧЕЧНО по четырём сотрудникам.

   Зачем: страница «Альта Бонус» (fn_User_Bonus_by_Details_New, хвостовая
   UNION-ветка) показывает сотрудника, если у него есть строка плана на месяц.
   Фильтра по login_active / user_in_past там нет, поэтому уволенные висят
   в списке с нулями (жалоба Екатерины Якимовой по Павлюковой от 28.07.2026,
   плюс Зайнутдинова и Чернова в отчёте за июль 2026).
   Функцию НЕ трогаем — чистим данные.

   Кого чистим (только эти четверо):
     15355708  Павлюкова Ирина Константиновна  — ЗП по 12.2025
      9670177  Чернова Юлия Валерьевна         — ЗП по 02.2026
      9286052  Зайнутдинова Олеся Александровна— зарплаты нет вообще (фрилансер)
     15983935  Воинова Станислава              — ЗП по 05.2026

   Правило: удаляем строки плана за месяцы СТРОГО ПОЗЖЕ последнего месяца
   зарплаты (salary_by_user). Если зарплаты нет ни за один месяц (фрилансер) —
   граница = 0, то есть под удаление попадают все строки плана.

   ЗАЩИТЫ:
     1. Месяц, в котором у сотрудника есть акт (консультант / ресечер,
        в т.ч. правка в PFPP) или доп. бонус, НЕ удаляется — иначе поедет
        история начислений и невыплаченные бонусы по этому акту.
        Практически это отсекает у Черновой март 2026 (акт 1633).
     2. Всё удаляемое копируется в dbo.userplanByMonth_deleted_backup.
     3. Скрипт по умолчанию завершается ROLLBACK. Посмотреть отчёты →
        заменить ROLLBACK на COMMIT в конце раздела 6 → выполнить снова.

   Ожидаемый результат (проверено на данных 05.08.2026):
     Павлюкова    — 11 строк (2026: 01, 02, 04…12)
     Чернова      —  6 строк (2026: 04…09); март 2026 остаётся (есть акт)
     Зайнутдинова —  6 строк (2026: 04…09); актов и зарплаты нет вообще
     Воинова      —  0 строк (планы кончаются 03.2026, ЗП по 05.2026)
     ИТОГО        — 23 строки

   Выполнять целиком в SSMS на базе recruting.
   ============================================================================ */

USE recruting;
GO
SET NOCOUNT ON;
GO

/* ---------------------------------------------------------------------------
   0. Таблица бэкапа (создаётся один раз, накапливает историю чисток)
   --------------------------------------------------------------------------- */
IF OBJECT_ID('dbo.userplanByMonth_deleted_backup') IS NULL
BEGIN
    CREATE TABLE dbo.userplanByMonth_deleted_backup
    (
        backup_id         int IDENTITY(1,1) PRIMARY KEY,
        deleted_at        datetime      NOT NULL CONSTRAINT DF_upbm_bak_dt DEFAULT GETDATE(),
        reason            nvarchar(200) NULL,
        userplan_id       bigint,
        user_id           bigint,
        userplan_value    int,
        userplan_month    int,
        userplan_quarter  int,
        userplan_year     int,
        user_plan_date    datetime,
        user_position     int,
        user_department   int,
        user_scheme       int,
        bet               float,
        probation         int,
        [percent]         float,
        main_department   int
    );
    PRINT 'Создана таблица dbo.userplanByMonth_deleted_backup';
END
GO

/* ---------------------------------------------------------------------------
   1. Целевые сотрудники + граница (последний месяц зарплаты) в формате ГГГГММ
      NULL в last_salary_ym = зарплаты не было ни разу → граница 0
   --------------------------------------------------------------------------- */
IF OBJECT_ID('tempdb..#targets') IS NOT NULL DROP TABLE #targets;

CREATE TABLE #targets (man_id bigint PRIMARY KEY, man_fio nvarchar(200), last_salary_ym int);

INSERT #targets (man_id, man_fio, last_salary_ym)
SELECT  m.man_id,
        m.man_fio,
        (SELECT MAX(s.salary_year * 100 + s.salary_month)
         FROM dbo.salary_by_user s WHERE s.man_id = m.man_id)
FROM    dbo.man m
WHERE   m.man_id IN (15355708, 9670177, 9286052, 15983935);

PRINT '--- 1. Целевые сотрудники и граница ---';
SELECT man_id, man_fio, ISNULL(last_salary_ym, 0) AS [граница, ГГГГММ] FROM #targets ORDER BY man_fio;

/* ---------------------------------------------------------------------------
   2. Месяцы с активностью — их не удаляем ни при каких условиях
   --------------------------------------------------------------------------- */
IF OBJECT_ID('tempdb..#activity') IS NOT NULL DROP TABLE #activity;

CREATE TABLE #activity (user_id bigint, ym int);

-- акты: консультант / ресечер / доп. бонус, месяц по дате акта
INSERT #activity (user_id, ym)
SELECT DISTINCT u.user_id,
       DATEPART(yy, ab.date_act) * 100 + DATEPART(mm, ab.date_act)
FROM (
        SELECT responsible_user_id AS user_id, act_id FROM dbo.project_buh WHERE responsible_user_id IS NOT NULL
        UNION ALL
        SELECT resecher_id,                     act_id FROM dbo.project_buh WHERE resecher_id IS NOT NULL
        UNION ALL
        SELECT responsible_user_id,             act_id FROM dbo.project_buh_failed_probation_period WHERE responsible_user_id IS NOT NULL
        UNION ALL
        SELECT resecher_id,                     act_id FROM dbo.project_buh_failed_probation_period WHERE resecher_id IS NOT NULL
        UNION ALL
        SELECT employer_id,                     act_id FROM dbo.extra_bonus
     ) u
JOIN dbo.act_buh ab ON ab.id = u.act_id
JOIN #targets t ON t.man_id = u.user_id;

-- правки актов (PFPP): начисление уезжает в месяц изменения
INSERT #activity (user_id, ym)
SELECT DISTINCT f.responsible_user_id,
       DATEPART(yy, f.date_update) * 100 + DATEPART(mm, f.date_update)
FROM dbo.project_buh_failed_probation_period f
JOIN #targets t ON t.man_id = f.responsible_user_id
WHERE f.date_update IS NOT NULL
UNION
SELECT DISTINCT f.resecher_id,
       DATEPART(yy, f.date_update) * 100 + DATEPART(mm, f.date_update)
FROM dbo.project_buh_failed_probation_period f
JOIN #targets t ON t.man_id = f.resecher_id
WHERE f.date_update IS NOT NULL;

/* ---------------------------------------------------------------------------
   3. Строки-кандидаты на удаление
   --------------------------------------------------------------------------- */
IF OBJECT_ID('tempdb..#to_delete') IS NOT NULL DROP TABLE #to_delete;

SELECT  u.userplan_id,
        t.man_id,
        t.man_fio,
        ISNULL(t.last_salary_ym, 0)              AS last_salary_ym,
        u.userplan_year * 100 + u.userplan_month AS plan_ym
INTO    #to_delete
FROM    dbo.userplanByMonth u
JOIN    #targets t ON t.man_id = u.user_id
WHERE   u.userplan_year * 100 + u.userplan_month > ISNULL(t.last_salary_ym, 0)
  AND   NOT EXISTS (SELECT 1 FROM #activity a
                    WHERE a.user_id = t.man_id
                      AND a.ym      = u.userplan_year * 100 + u.userplan_month);

/* ---------------------------------------------------------------------------
   4. ОТЧЁТЫ ДО УДАЛЕНИЯ
   --------------------------------------------------------------------------- */
PRINT '--- 4.1 Будет удалено, свод по сотрудникам (ожидается: Павлюкова 11, Чернова 6, Зайнутдинова 6) ---';
SELECT  man_fio,
        last_salary_ym  AS [граница, ГГГГММ],
        COUNT(*)        AS [строк к удалению],
        MIN(plan_ym)    AS [с месяца],
        MAX(plan_ym)    AS [по месяц]
FROM    #to_delete
GROUP BY man_fio, last_salary_ym
ORDER BY man_fio;

PRINT '--- 4.2 Будет удалено, построчно ---';
SELECT userplan_id, man_fio, plan_ym AS [месяц плана] FROM #to_delete ORDER BY man_fio, plan_ym;

PRINT '--- 4.3 ПРОПУЩЕНО: месяц после границы, но в нём есть акт/доп.бонус (ожидается только Чернова, 202603) ---';
SELECT  t.man_fio,
        u.userplan_id,
        u.userplan_year * 100 + u.userplan_month AS [месяц плана]
FROM    dbo.userplanByMonth u
JOIN    #targets t ON t.man_id = u.user_id
WHERE   u.userplan_year * 100 + u.userplan_month > ISNULL(t.last_salary_ym, 0)
  AND   EXISTS (SELECT 1 FROM #activity a
                WHERE a.user_id = t.man_id
                  AND a.ym      = u.userplan_year * 100 + u.userplan_month)
ORDER BY t.man_fio, 3;

PRINT '--- 4.4 Кому чистить нечего (нет планов после границы) — ожидается Воинова ---';
SELECT  t.man_fio,
        ISNULL(t.last_salary_ym, 0) AS [граница, ГГГГММ],
        (SELECT MAX(u.userplan_year * 100 + u.userplan_month)
         FROM dbo.userplanByMonth u WHERE u.user_id = t.man_id) AS [последний месяц плана]
FROM    #targets t
WHERE   NOT EXISTS (SELECT 1 FROM #to_delete d WHERE d.man_id = t.man_id)
ORDER BY t.man_fio;

/* ---------------------------------------------------------------------------
   5. Сверка «до»: список бонусов за контрольный период (июль 2026)
   --------------------------------------------------------------------------- */
IF OBJECT_ID('tempdb..#bonus_before') IS NOT NULL DROP TABLE #bonus_before;

SELECT man_id, man_fio, dep_name, money_itog
INTO   #bonus_before
FROM   dbo.fn_User_Bonus_by_Details_New('20260701', '20260731', 0, 0);

/* ---------------------------------------------------------------------------
   6. УДАЛЕНИЕ (в транзакции; по умолчанию откат)
   --------------------------------------------------------------------------- */
BEGIN TRANSACTION;

    INSERT dbo.userplanByMonth_deleted_backup
          (reason, userplan_id, user_id, userplan_value, userplan_month,
           userplan_quarter, userplan_year, user_plan_date, user_position,
           user_department, user_scheme, bet, probation, [percent], main_department)
    SELECT N'Уволен: план за месяц после последней зарплаты (Павлюкова/Чернова/Зайнутдинова/Воинова)',
           u.userplan_id, u.user_id, u.userplan_value, u.userplan_month,
           u.userplan_quarter, u.userplan_year, u.user_plan_date, u.user_position,
           u.user_department, u.user_scheme, u.bet, u.probation, u.[percent], u.main_department
    FROM   dbo.userplanByMonth u
    JOIN   #to_delete t ON t.userplan_id = u.userplan_id;

    PRINT '--- 6.1 Скопировано строк в бэкап: ---';
    PRINT @@ROWCOUNT;

    DELETE u
    FROM   dbo.userplanByMonth u
    JOIN   #to_delete t ON t.userplan_id = u.userplan_id;

    PRINT '--- 6.2 Удалено строк: ---';
    PRINT @@ROWCOUNT;

    PRINT '--- 6.3 Пропали из списка «Альта Бонус» за июль 2026 (ожидается Павлюкова, Чернова, Зайнутдинова; суммы 0) ---';
    SELECT b.man_fio, b.dep_name, b.money_itog
    FROM   #bonus_before b
    WHERE  NOT EXISTS (SELECT 1
                       FROM dbo.fn_User_Bonus_by_Details_New('20260701','20260731',0,0) a
                       WHERE a.man_id = b.man_id)
    ORDER BY b.man_fio;

    PRINT '--- 6.4 КОНТРОЛЬ: пропавшие с ненулевым бонусом (должно быть 0 строк) ---';
    SELECT b.man_fio, b.dep_name, b.money_itog
    FROM   #bonus_before b
    WHERE  b.money_itog <> 0
      AND  NOT EXISTS (SELECT 1
                       FROM dbo.fn_User_Bonus_by_Details_New('20260701','20260731',0,0) a
                       WHERE a.man_id = b.man_id);

/* Посмотреть отчёты выше → если всё верно, заменить ROLLBACK на COMMIT
   и выполнить скрипт повторно. */
ROLLBACK TRANSACTION;
-- COMMIT TRANSACTION;
GO

/* ============================================================================
   ОТКАТ уже закоммиченной чистки (если понадобится).
   userplan_id — IDENTITY, поэтому обязательно IDENTITY_INSERT:

   SET IDENTITY_INSERT dbo.userplanByMonth ON;

   INSERT dbo.userplanByMonth
         (userplan_id, user_id, userplan_value, userplan_month, userplan_quarter,
          userplan_year, user_plan_date, user_position, user_department,
          user_scheme, bet, probation, [percent], main_department)
   SELECT userplan_id, user_id, userplan_value, userplan_month, userplan_quarter,
          userplan_year, user_plan_date, user_position, user_department,
          user_scheme, bet, probation, [percent], main_department
   FROM   dbo.userplanByMonth_deleted_backup
   WHERE  deleted_at >= '<дата чистки в формате ГГГГММДД>';

   SET IDENTITY_INSERT dbo.userplanByMonth OFF;
   ============================================================================ */
