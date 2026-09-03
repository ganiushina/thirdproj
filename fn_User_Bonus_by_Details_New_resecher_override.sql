-- ============================================================================
--  ИСПРАВЛЕНИЕ fn_User_Bonus_by_Details_New
--  Учёт изменения/добавления РЕСЕЧЕРА по дате изменения (date_update)
-- ============================================================================
--
--  ПРОБЛЕМА:
--    Ветка ресечера читала данные ТОЛЬКО из project_buh напрямую и всегда
--    использовала ab.date_act. Поэтому смена/добавление ресечера через кнопку
--    «Изменить» (которая пишет в project_buh_failed_probation_period, PFPP,
--    с date_update = дата изменения) НЕ попадала в расчёт бонуса ресечера:
--    в выборке оставался старый ресечер, а дата изменения игнорировалась.
--    (Для консультанта override уже работал через v_project_buh_actual.)
--
--  РЕШЕНИЕ [RESECHER-OVERRIDE]:
--    Источник строк ресечера для @tmp_money_resecher собирается как UNION ALL,
--    по образцу консультантской ветки и fn_margin_bonus_by_quarter_fixed:
--      (A) Акты БЕЗ изменений  → ресечер из project_buh, дата = ab.date_act.
--      (B) Новый/изменённый ресечер из PFPP → дата = date_update (месяц
--          изменения → актуальная схема/лимиты → «актуальный бонус»).
--      (C) Сторно старого ресечера базы для изменённых актов → минус,
--          дата = date_update.
--    View v_project_buh_actual НЕ используется и НЕ меняется (он минусует
--    только summ_responsible_user, не summ_resecher, и задействован в
--    fn_marginality_by_month и fn_margin_bonus_by_quarter).
--
--  ВНИМАНИЕ (бизнес-семантика — согласовать при тестировании):
--    1. Как и у консультанта: после появления override по акту его
--       ИСХОДНОЕ (по date_act) начисление ресечера из функции пропадает —
--       коррекция целиком переносится в месяц изменения (предполагается,
--       что бонус за исходный месяц уже выплачен и заморожен).
--    2. Старый снятый ресечер получает ОТРИЦАТЕЛЬНую коррекцию в месяце
--       изменения. Если это нежелательно — убрать ветку (C).
--    3. Блок пересчёта внутри курсора `if (@mon <> DATEPART(mm,@date_act))`
--       (сценарий смены должности ресечера) по-прежнему читает project_buh
--       напрямую — это отдельная, не тронутая здесь логика.
--
--  ТОЛЬКО ветка @tmp_money_resecher изменена; остальное тело — без изменений.
-- ============================================================================

ALTER FUNCTION [dbo].[fn_User_Bonus_by_Details_New]
    (@date1 date,
     @date2 date,
     @userId int,
     @depatment_id int)
RETURNS @t_res TABLE (man_id int, man_fio nvarchar(200), pos_name nvarchar(100), dep_name varchar(255), is_freelancer int, dep_id int, money_itog money,
                      money_by_candidate money, persent float, summ_total money, summ_user money,
                      company_name nvarchar(200), candidate nvarchar(255),  mont nvarchar(100), mon int, ya int, act_id int)
AS
BEGIN

    DECLARE @qrt1 int = (SELECT datepart(qq, @date1))
    DECLARE @qrt2 int = (SELECT datepart(qq, @date2))

    DECLARE @ya1 int = (SELECT datepart(yy, @date1))
    DECLARE @ya2 int = (SELECT datepart(yy, @date2))

    DECLARE @mon1 varchar(20) = (SELECT datepart(mm,@date1))
    DECLARE @mon2 varchar(20) = (SELECT datepart(mm,@date2))

    DECLARE @halfYear1 int = CASE WHEN @mon1 BETWEEN 1 AND 6  THEN 1
                                  WHEN @mon1 BETWEEN 7 AND 12 THEN 2 END

    DECLARE @halfYear2 int = CASE WHEN @mon2 BETWEEN 1 AND 6  THEN 1
                                  WHEN @mon2 BETWEEN 7 AND 12 THEN 2 END


    DECLARE @t3 TABLE (id int)
    INSERT @t3
    SELECT id FROM dbo.position p
    WHERE p.id IN (3,4,5,6,7,13,1,2,14,15,16,19,20, 26, 27)

    DECLARE @tmp_money TABLE (user_id int, user_name varchar(255), company_name varchar(255), candidate varchar(255), summ_user money, money_tmp_total money,
                             position_id int, dep_id int, schem int, bet float, mon int, ya int, act_id int, date_act datetime)

    DECLARE @tmp_money_consultant TABLE (user_id int, user_name varchar(255),  company_name varchar(255), candidate varchar(255), summ_user money, money_tmp_total money,
                             mon int, ya int, act_id int, date_act datetime, dep_id int)
    DECLARE @tmp_money_resecher TABLE (user_id int, user_name varchar(255),  company_name varchar(255), candidate varchar(255), summ_user money, money_tmp_total money,
                             mon int, ya int, act_id int, date_act datetime, dep_id int)
    DECLARE @tmp_money_extra_bonus TABLE (user_id int, user_name varchar(255),  company_name varchar(255), candidate varchar(255), summ_user money, money_tmp_total money,
                             mon int, ya int, act_id int, date_act datetime)


    -- ==== КОНСУЛЬТАНТ (responsible_user) — без изменений, override через v_project_buh_actual ====
    INSERT @tmp_money_consultant
    SELECT DISTINCT responsible_user_id, responsible_user_name, company_name, candidate, summ_responsible_user,
        SUM(CASE WHEN CHARINDEX('freelancer', responsible_user_name) = 0 THEN summ_responsible_user ELSE 0 END)
            OVER(PARTITION BY responsible_user_id, DATEPART(mm,date_act), DATEPART(yy,date_act) ORDER BY DATEPART(qq,date_act)) summ_total,
        DATEPART(mm,date_act) mon, DATEPART(yy,date_act) ya, act_id, date_act, depatment_id
    FROM (
        SELECT DISTINCT
            CASE WHEN pba.act_id IS NOT NULL THEN pba.responsible_user_id   ELSE pb.responsible_user_id   END AS responsible_user_id,
            CASE WHEN pba.act_id IS NOT NULL THEN pba.responsible_user_name ELSE pb.responsible_user_name END AS responsible_user_name,
            ab.company_name,
            ab.candidate,
            CASE WHEN pba.act_id IS NOT NULL THEN pba.summ_responsible_user ELSE pb.summ_responsible_user END AS summ_responsible_user,
            CASE WHEN pba.act_id IS NOT NULL THEN pba.date_update ELSE ab.date_act END AS date_act,
            pb.act_id,
            CASE WHEN pba.act_id IS NOT NULL THEN pba.depatment_id  ELSE pb.depatment_id END AS depatment_id
        FROM dbo.act_buh ab
        JOIN dbo.project_buh pb
          ON pb.act_id = ab.id
        LEFT JOIN dbo.v_project_buh_actual pba
          ON pba.act_id = pb.act_id
        WHERE
              (
                  CONVERT(date, ab.date_act) BETWEEN CONVERT(date, @date1) AND CONVERT(date,  @date2)
                  OR CONVERT(date, pba.date_update) BETWEEN CONVERT(date, @date1) AND CONVERT(date, @date2)
              )
        AND pb.responsible_user_id IS NOT NULL
        AND pb.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
    ) z1


    -- ==== РЕСЕЧЕР (resecher) — [RESECHER-OVERRIDE] учёт изменения по date_update ====
    INSERT @tmp_money_resecher
    SELECT DISTINCT s4.resecher_id, s4.resecher_name, s4.company_name, s4.candidate, s4.summ_resecher, s4.summ_total, s4.mon, s4.ya, s4.act_id, s4.date_act, s4.depatment_id
    FROM (
        SELECT z1.resecher_id, z1.resecher_name, z1.company_name, z1.candidate, z1.summ_resecher,
               SUM(CASE WHEN CHARINDEX('freelancer', z1.resecher_name) = 0 THEN z1.summ_resecher ELSE 0 END)
                   OVER (PARTITION BY z1.resecher_id, z1.mon, z1.ya) AS summ_total,
               z1.mon, z1.ya, z1.act_id, z1.date_act, z1.depatment_id
        FROM (
            SELECT DISTINCT
                src.resecher_id, src.resecher_name, src.summ_resecher, src.date_act,
                DATEPART(mm, src.date_act) AS mon, DATEPART(yy, src.date_act) AS ya,
                src.company_name, src.candidate, src.act_id,
                CASE WHEN ISNULL(src.depatment_resecher_id, 0) = 0
                     THEN ubm.user_department
                     ELSE src.depatment_resecher_id END AS depatment_id
            FROM (
                -- (A) Акты БЕЗ изменений: ресечер из project_buh, дата = дата акта
                SELECT pb.resecher_id, pb.resecher_name, pb.summ_resecher, ab.date_act,
                       ab.company_name, ab.candidate, pb.act_id, pb.depatment_resecher_id
                FROM dbo.act_buh ab
                JOIN dbo.project_buh pb ON pb.act_id = ab.id
                WHERE CONVERT(date, ab.date_act) BETWEEN CONVERT(date, @date1) AND CONVERT(date, @date2)
                  AND pb.resecher_id IS NOT NULL
                  AND pb.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                  AND NOT EXISTS (SELECT 1 FROM dbo.project_buh_failed_probation_period f
                                  WHERE f.act_id = pb.act_id)

                UNION ALL

                -- (B) Новый/изменённый ресечер из override (PFPP): дата = date_update (месяц изменения)
                SELECT f.resecher_id, f.resecher_name, f.summ_resecher, f.date_update AS date_act,
                       ab.company_name, ab.candidate, f.act_id, f.depatment_resecher_id
                FROM dbo.project_buh_failed_probation_period f
                JOIN dbo.act_buh ab ON ab.id = f.act_id
                WHERE f.resecher_id IS NOT NULL
                  AND CONVERT(date, f.date_update) BETWEEN CONVERT(date, @date1) AND CONVERT(date, @date2)
                  AND f.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                  AND NOT EXISTS (
                        SELECT 1 FROM dbo.project_buh b
                        WHERE b.act_id = f.act_id
                          AND b.resecher_id = f.resecher_id
                          AND CAST(ISNULL(b.summ_resecher,0) AS DECIMAL(18,2)) = CAST(ISNULL(f.summ_resecher,0) AS DECIMAL(18,2)))

                UNION ALL

                -- (C) Сторно старого ресечера базы для изменённых актов: минус, дата = date_update
                SELECT b.resecher_id, b.resecher_name, -b.summ_resecher AS summ_resecher, du.max_date_update AS date_act,
                       ab.company_name, ab.candidate, b.act_id, b.depatment_resecher_id
                FROM dbo.project_buh b
                JOIN dbo.act_buh ab ON ab.id = b.act_id
                JOIN (SELECT act_id, MAX(date_update) AS max_date_update
                      FROM dbo.project_buh_failed_probation_period
                      GROUP BY act_id) du ON du.act_id = b.act_id
                WHERE b.resecher_id IS NOT NULL
                  AND CONVERT(date, du.max_date_update) BETWEEN CONVERT(date, @date1) AND CONVERT(date, @date2)
                  AND b.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                  AND NOT EXISTS (
                        SELECT 1 FROM dbo.project_buh_failed_probation_period f
                        WHERE f.act_id = b.act_id
                          AND f.resecher_id = b.resecher_id
                          AND CAST(ISNULL(f.summ_resecher,0) AS DECIMAL(18,2)) = CAST(ISNULL(b.summ_resecher,0) AS DECIMAL(18,2)))
            ) src
            LEFT JOIN dbo.userplanByMonth ubm
                ON ubm.user_id = src.resecher_id
               AND ubm.userplan_month = DATEPART(mm, @date2)
               AND ubm.userplan_year  = DATEPART(yy, @date2)
        ) z1
    ) s4


    -- ==== ДОП. БОНУСЫ (extra_bonus) — без изменений ====
    INSERT @tmp_money_extra_bonus
    SELECT s.employer_id, s.man_fio, s.company_name, s.candidate, s.extra_bonus,
        sum(s.extra_bonus)
            over(PARTITION BY employer_id, DATEPART(mm,date_act), DATEPART(yy,date_act) ORDER BY DATEPART(qq,date_act)) summ_total,
        DATEPART(mm,date_act) mon, DATEPART(yy,date_act) ya, act_id, date_act
    FROM (
        SELECT DISTINCT eb.employer_id, m.man_fio, ab.company_name, ab.candidate + ' ' + cast(eb.bonus_percent AS varchar(200)) + '% доп' candidate,
            cast(((cast(eb.bonus_percent AS DEC(12,4)) ) * cast(ab.total_no_nds AS DEC(12,4))) / (100.) AS DEC(12,4)) extra_bonus,
            ab.date_act, eb.act_id
        FROM extra_bonus eb
        JOIN dbo.act_buh ab ON ab.id = eb.act_id
        JOIN dbo.man m ON m.man_id = eb.employer_id
        left JOIN dbo.userplanByMonth ubm ON ubm.user_id = eb.employer_id
                AND ubm.userplan_month = datepart(mm, @date2) AND ubm.userplan_year = datepart(yy, @date2)
        left JOIN dbo.position p ON p.id = ubm.user_position
        left JOIN dbo.depatment d ON d.id = ubm.user_department
        WHERE ab.date_act BETWEEN CONVERT(date, @date1) AND CONVERT(date, @date2)
    ) s


    -- ==== Свод консультантов в @tmp_money — без изменений ====
    INSERT @tmp_money
    SELECT tm.user_id, tm.user_name, company_name, candidate, tm.summ_user, tm.money_tmp_total, ubm.user_position, tm.dep_id, ubm.user_scheme, ubm.bet,
        tm.mon, tm.ya, tm.act_id, date_act
    FROM @tmp_money_consultant tm
    JOIN man m ON m.man_id = tm.user_id
    JOIN dbo.userplanByMonth ubm ON ubm.user_id = tm.user_id AND ubm.userplan_month = tm.mon AND ubm.userplan_year = tm.ya


    -- ==== Свод ресечеров в @tmp_money (с логикой смены должности ubmres) — без изменений ====
    INSERT @tmp_money
    SELECT DISTINCT tm.user_id, tm.user_name, company_name, candidate, tm.summ_user, tm.money_tmp_total,
    CASE WHEN ubm.user_position NOT IN (1,2,14,17,18, 22, 25, 24) and DATEPART(mm, date_act) <> ubmres.userplan_month  THEN ubmres.user_position
    else ubm.user_position end pos_id,
    CASE WHEN ubm.user_position NOT IN (1,2,14,17,18, 22, 25, 24) and DATEPART(mm, date_act) <> ubmres.userplan_month THEN ubmres.user_department
    else ubm.user_department end dep_id,
    CASE WHEN ubm.user_position NOT IN (1,2,14,17,18, 22, 25, 24) and DATEPART(mm, date_act) <> ubmres.userplan_month THEN ubmres.user_scheme
    else ubm.user_scheme END scheme,
    ubm.bet,
    CASE WHEN ubm.user_position NOT IN (1,2,14,17,18, 22, 25, 24) and DATEPART(mm, date_act) <> ubmres.userplan_month THEN ubmres.userplan_month
    else tm.mon END mon,
    tm.ya, tm.act_id, date_act
    FROM @tmp_money_resecher tm
    JOIN man m ON m.man_id = tm.user_id
    left JOIN dbo.userplanByMonth ubm ON ubm.user_id = tm.user_id AND ubm.userplan_month = tm.mon AND ubm.userplan_year = tm.ya and ubm.user_department = tm.dep_id
    LEFT JOIN (select ubm.user_id, ubm.user_department, ubm.user_position, ubm.user_scheme, ubm.userplan_month from userplanByMonth ubm
                join (
                SELECT distinct s.user_id, max(ubml.userplan_month) over (partition by s.user_id order by ubml.userplan_month desc ) mon , ya,
                ubml.user_department, ubml.user_scheme FROM userplanByMonth ubml
                                JOIN (SELECT DISTINCT ubmmax.user_id, max(ubmmax.userplan_year) ya, max(ubmmax.user_scheme) uscheme
                                            FROM userplanByMonth ubmmax
                                            WHERE ubmmax.user_position IN (1,2,14,17,18, 22, 25, 24)
                                            GROUP BY  ubmmax.user_id
                                            ) s ON s.user_id = ubml.user_id AND s.ya = ubml.userplan_year and ubml.user_scheme = s.uscheme
                                WHERE ubml.user_position IN (1,2,14,17,18, 22, 25, 24) and ubml.user_id in (select tr.user_id from @tmp_money_resecher tr)
        ) s2 on s2.user_id = ubm.user_id and s2.user_department = ubm.user_department and ubm.userplan_month = s2.mon and ubm.userplan_year = s2.ya) ubmres ON ubmres.user_id = tm.user_id


    -- ==== Свод доп. бонусов в @tmp_money — без изменений ====
    INSERT @tmp_money
    SELECT DISTINCT tm.user_id, tm.user_name, company_name, candidate, tm.summ_user, tm.money_tmp_total, ubm.user_position, ubm.user_department, ubm.user_scheme, ubm.bet,
        tm.mon, tm.ya, tm.act_id, tm.date_act
    FROM @tmp_money_extra_bonus tm
    JOIN man m ON m.man_id = tm.user_id
    JOIN dbo.userplanByMonth ubm ON ubm.user_id = tm.user_id AND ubm.userplan_month = tm.mon AND ubm.userplan_year = tm.ya


    declare @tabl_itog TABLE (user_id int, user_name varchar(255), money_itog money, money_by_candidate money, persent float, company_name varchar(255), candidate varchar(255),
                              summ_user money, summ_total money, mon int, ya int, position_id int, dep_id int, act_id int
                              , is_freelancer int
                              )

    DECLARE @user_id int
    DECLARE @money_itog1 money
    DECLARE @money_total money
    DECLARE @pos_id int, @dep_id int, @act_id int
    DECLARE @scheme_id int
    DECLARE @percent float
    DECLARE @mon int, @ya int,
    @company_name varchar(255), @candidate varchar(255), @user_name varchar(255), @summ_user money
    ,@date_act datetime
    declare @is_freelancer int = 0;

    declare cur1 cursor for (SELECT distinct tm.user_id, tm.user_name,
                                                CASE
                                                    WHEN convert(date, tm.date_act) < convert(date, '20251001') THEN
                                                        CASE
                                                            WHEN ubm.probation = 0 THEN tm.money_tmp_total/tm.bet
                                                            ELSE (tm.money_tmp_total/tm.bet) * 2
                                                        END
                                                    ELSE
                                                        CASE
                                                            WHEN ubm.probation = 0 THEN tm.money_tmp_total
                                                            ELSE tm.money_tmp_total * 2
                                                        END
                                                END, -- учитываем испытательный срок и дату акта. 01.10.2025 поменялась схема, ставку не учитываем
                            tm.money_tmp_total, tm.company_name, tm.candidate, tm.summ_user,
                            tm.position_id, tm.dep_id, tm.schem, tm.mon, tm.ya, tm.act_id, tm.date_act FROM @tmp_money tm
                            JOIN dbo.userplanByMonth ubm ON ubm.user_id = tm.user_id AND ubm.userplan_month = tm.mon AND ubm.userplan_year = tm.ya
                            )
    open cur1
    fetch next from cur1 into  @user_id, @user_name, @money_itog1, @money_total, @company_name, @candidate, @summ_user,  @pos_id, @dep_id,
                            @scheme_id,@mon , @ya, @act_id, @date_act
    while @@fetch_status = 0
    begin
            declare @formula1 dec(12,4)
            declare @formula2 dec(12,4)
            set @is_freelancer = 0

            if (@mon <> DATEPART(mm, @date_act))
            begin
                declare @monye_itog_last_month money

                set @monye_itog_last_month =
                    (SELECT distinct summ_total FROM
                        (
                        SELECT z1.resecher_id, summ_resecher,
                        sum(summ_resecher) over(PARTITION BY resecher_id, mon, ya) summ_total, mon, ya, act_id, date_act FROM (
                            SELECT DISTINCT
                                pb.resecher_id, summ_resecher, date_act,
                                DATEPART(mm, ab.date_act) mon , DATEPART(yy,date_act) ya, ab.company_name, ab.candidate, pb.act_id
                            FROM dbo.act_buh ab
                                JOIN project_buh pb ON pb.act_id = ab.id
                            WHERE
                                DATEPART(mm, ab.date_act) = @mon  and DATEPART(yy,date_act) = @ya  AND pb.resecher_id = @user_id
                                AND pb.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                            ) z1
                        ) s4
                    )

                    if @monye_itog_last_month >0
                    begin

                        SET @percent = (
                                        SELECT TOP 1 gap_percent FROM scheme_limits sl
                                    JOIN gap g ON g.gap_id =  sl.gap_id
                                    WHERE limits <= @monye_itog_last_month AND position_id = @pos_id AND sl.scheme_id = @scheme_id
                                    AND sl.date_scheme = (SELECT max(date_scheme) FROM (
                                                                    SELECT sl.date_scheme FROM scheme_limits sl
                                                                    GROUP BY sl.date_scheme
                                                                    ) s
                                                                    WHERE @date_act >= s.date_scheme)
                                    ORDER BY sl.limits DESC)
                    end
                    else
                    begin
                        SET @percent = (
                                        SELECT TOP 1 gap_percent FROM scheme_limits sl
                                    JOIN gap g ON g.gap_id =  sl.gap_id
                                    WHERE limits = 0.00 AND position_id = @pos_id AND sl.scheme_id = @scheme_id
                                    AND sl.date_scheme = (SELECT max(date_scheme) FROM (
                                                                    SELECT sl.date_scheme FROM scheme_limits sl
                                                                    GROUP BY sl.date_scheme
                                                                    ) s
                                                                    WHERE @date_act >= s.date_scheme)
                                    ORDER BY sl.limits DESC)

                    end

                set @formula1 = (cast(@money_itog1 as dec(12,4)) * @percent)  / 100

                IF @formula1 IS NOT null
                set @formula2  = (cast(@summ_user as dec(12,4)) * @percent)  / 100

            end
            else

            IF (CHARINDEX ('%', @candidate)>0)
            begin
                SET @percent = (SELECT eb.bonus_percent FROM dbo.extra_bonus eb
                    WHERE eb.employer_id = @user_id AND eb.act_id = @act_id)
                set @formula1 = @money_itog1
                set @formula2 = @summ_user
                set @money_itog1 =0
            END

            else
            if (@pos_id <> 26) and ((select CHARINDEX('freelancer', @user_name)) > 0)
            BEGIN
                SET @percent = 40
                set @formula1 = (cast(@money_itog1 as dec(12,4)) * @percent)  / 100
                set @formula2  = (cast(@summ_user as dec(12,4)) * @percent)  / 100
                set @is_freelancer = 1
            END
            else

            if (@pos_id = 26) or ((select CHARINDEX('sales', @user_name)) > 0)
            BEGIN
                set @formula1 = @summ_user
                set @formula2 = @summ_user
                if (@pos_id = 26)
                    SET @percent = (select pb.percent_responsible_user_complicity from project_buh pb where pb.act_id = @act_id and pb.responsible_user_id = @user_id
                                    )
                if ((select CHARINDEX('sales', @user_name)) > 0)
                    begin
                    SET @percent = (select pb.percent_responsible_user_complicity from project_buh pb where pb.act_id = @act_id and pb.responsible_user_id = @user_id
                                    and pb.responsible_user_name = @user_name)

                   end
            END

            ELSE

            BEGIN

                SET @percent = (
                                SELECT TOP 1 gap_percent FROM scheme_limits sl
                            JOIN gap g ON g.gap_id =  sl.gap_id
                            WHERE limits <= @money_itog1 AND position_id = @pos_id AND sl.scheme_id = @scheme_id
                            AND sl.date_scheme = (SELECT max(date_scheme) FROM (
                                                        SELECT sl.date_scheme FROM scheme_limits sl
                                                        GROUP BY sl.date_scheme
                                                        ) s
                                                        WHERE @date_act >= s.date_scheme)
                            ORDER BY sl.limits DESC)

                set @formula1 = (cast(@money_itog1 as dec(12,4)) * @percent)  / 100

                IF @formula1 IS NOT null
                set @formula2  = (cast(@summ_user as dec(12,4)) * @percent)  / 100
            end

            IF @formula1 IS NOT null
            insert @tabl_itog values (@user_id, @user_name, @formula1, @formula2, @percent, @company_name, @candidate, @summ_user,
                    @money_total, DATEPART(mm, @date_act)  , @ya, @pos_id, @dep_id, @act_id, @is_freelancer)


        fetch next from cur1 into @user_id, @user_name, @money_itog1, @money_total, @company_name, @candidate, @summ_user,
                                    @pos_id, @dep_id, @scheme_id,@mon , @ya, @act_id, @date_act
    end;

    close cur1
    deallocate cur1


    ;WITH cte AS (
        SELECT s.man_id, s.man_fio, s.pos_name, s.dep_id,
        STUFF((
            SELECT DISTINCT ', ' + d2.dep_name
            FROM dbo.userplanByMonth ubm2
            JOIN dbo.depatment d2 ON d2.id = ubm2.user_department
            WHERE ubm2.user_id = s.man_id
                AND ubm2.userplan_month = s.mon
                AND ubm2.userplan_year = s.ya
            FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS dep_names,
        s.money_by_candidate,
        sum(s.money_by_candidate) OVER (PARTITION BY s.man_id) money_itog,
        s.persent, s.summ_user, sum(s.summ_user) OVER (PARTITION BY man_id, s.dep_id) summ_total,
        s.company_name, s.candidate, s.mont, s.mon, s.ya, s.act_id, s.is_freelancer
        FROM (
            SELECT DISTINCT m.man_id, m.man_fio, p.pos_name, ti.money_by_candidate, ti.persent,
            ti.summ_total, ti.dep_id, ti.is_freelancer,
            case when CHARINDEX('sales', ti.user_name) > 0  then 0 else ti.summ_user end summ_user,
            ti.company_name, ti.candidate,
            CASE WHEN ti.mon =1 THEN 'Январь'
                 WHEN ti.mon =2 THEN 'Февраль'
                 WHEN ti.mon =3 THEN 'Март'
                 WHEN ti.mon =4 THEN 'Апрель'
                 WHEN ti.mon =5 THEN 'Май'
                 WHEN ti.mon =6 THEN 'Июнь'
                 WHEN ti.mon =7 THEN 'Июль'
                 WHEN ti.mon =8 THEN 'Август'
                 WHEN ti.mon =9 THEN 'Сентябрь'
                 WHEN ti.mon =10 THEN 'Октябрь'
                 WHEN ti.mon =11 THEN 'Ноябрь'
                 WHEN ti.mon =12 THEN 'Декабрь' END  mont,
            ti.mon,
            ti.ya, ti.act_id
            FROM @tabl_itog ti
            JOIN man m ON m.man_id = ti.user_id
            JOIN dbo.userplanByMonth ubm ON ubm.user_id = ti.user_id
                AND ubm.userplan_month = ti.mon
                AND ubm.userplan_year = ti.ya
            JOIN dbo.position p ON p.id = ubm.user_position
        ) s
    )

    INSERT @t_res
    SELECT s.man_id, s.man_fio, s.pos_name,
        s.dep_names,
        s.is_freelancer,
        s.dep_id,
        isnull(s.money_itog, '') money_itog,
        isnull(s.money_by_candidate, '') money_by_candidate,
        isnull(s.persent, '') persent,
        isnull(sum(s.summ_user) over(PARTITION BY s.man_id), '') summ_total,
        isnull(s.summ_user, '') summ_user,
        isnull(s.company_name, '') company_name,
        isnull(s.candidate, '') candidate,
        isnull(s.mont, '') mont,
        isnull(s.mon, '') mon,
        isnull(s.ya, '') ya,
        isnull(s.act_id, '') act_id
    FROM (
        SELECT * from cte
        UNION all
        SELECT DISTINCT
            m.man_id,
            m.man_fio,
            p.pos_name,
            ubm.user_department AS dep_id,
            STUFF((
                SELECT DISTINCT ', ' + d2.dep_name
                FROM dbo.userplanByMonth ubm2
                JOIN dbo.depatment d2 ON d2.id = ubm2.user_department
                WHERE ubm2.user_id = m.man_id
                  AND ubm2.userplan_month = ubm.userplan_month
                  AND ubm2.userplan_year = ubm.userplan_year
                FOR XML PATH(''), TYPE
            ).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS dep_names,
            null, null, null, null, null, null, null,
            (SELECT [dbo].[fn_get_month_name_by_int](ubm.userplan_month)),
            ubm.userplan_month,
            ubm.userplan_year,
            null,
            0
            FROM man m
            JOIN login l
                ON l.login_user_id = m.man_id
            OUTER APPLY (
                SELECT TOP 1
                    ubm1.user_department,
                    ubm1.userplan_month,
                    ubm1.userplan_year,
                    ubm1.user_position
                FROM dbo.userplanByMonth ubm1
                WHERE ubm1.user_id = l.login_user_id
                  AND ubm1.userplan_month BETWEEN DATEPART(mm, @date1) AND DATEPART(mm, @date2)
                  AND ubm1.userplan_year = DATEPART(yy, @date1)
                  AND ubm1.main_department >= 1
                ORDER BY ubm1.user_department
            ) ubm
            LEFT JOIN cte c
                ON c.man_id = m.man_id
               AND c.mon = ubm.userplan_month
               AND c.ya = ubm.userplan_year
            JOIN dbo.position p
                ON p.id = ubm.user_position
            WHERE c.man_id IS NULL
              AND ubm.user_department IS NOT NULL
    ) s
    GROUP BY s.man_id, s.man_fio, s.pos_name, s.dep_names, s.dep_id,
        s.money_itog, s.money_by_candidate, s.persent, s.summ_total, s.summ_user,
        s.company_name, s.candidate, s.mont, s.mon, s.ya, s.act_id, s.is_freelancer
    ORDER BY s.man_fio, s.money_itog DESC, mon

    return
end
GO

-- ============================================================================
--  ПРОВЕРКА (выполнять на ТЕСТОВОЙ среде; (1)-(2) меняют данные)
-- ============================================================================
--
--  Сценарий: акт за прошлый месяц, ресечеру меняют на нового «сегодня».
--  Ожидание: новый ресечер появляется в выборке за месяц ИЗМЕНЕНИЯ
--            (с актуальным бонусом по схеме этого месяца), старый — сторнируется.
--
--  -- 0) Выбрать тестовый акт с ресечером (без записей в PFPP):
--  SELECT TOP 5 pb.act_id, pb.resecher_id, pb.resecher_name, pb.summ_resecher, ab.date_act
--  FROM project_buh pb JOIN act_buh ab ON ab.id = pb.act_id
--  WHERE pb.resecher_id IS NOT NULL
--    AND NOT EXISTS (SELECT 1 FROM project_buh_failed_probation_period f WHERE f.act_id = pb.act_id);
--
--  -- 1) Сымитировать изменение ресечера (date_update = текущая дата):
--  --    Скопировать ВСЕ строки акта в PFPP, у нужной строки заменить resecher_id/name.
--  --    (на проде это делает кнопка «Изменить» → POST /userAct/update)
--
--  -- 2) Прогнать функцию за месяц ИЗМЕНЕНИЯ и проверить нового/старого ресечера:
--  --  SELECT man_fio, dep_name, money_by_candidate, persent, summ_user, mont, act_id
--  --  FROM dbo.fn_User_Bonus_by_Details_New('20260601','20260630', 0, 0)
--  --  WHERE act_id = <act_id> ORDER BY man_fio;
--
--  -- 3) Откатить тестовые данные:
--  --  DELETE FROM project_buh_failed_probation_period WHERE act_id = <act_id>;
-- ============================================================================
