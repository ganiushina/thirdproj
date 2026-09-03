CREATE FUNCTION [dbo].[fn_salary_for_all_user]
(
  @date1 DATETIME,
  @date2 datetime,
  @department_id int = 0
	
)
-- WITH ENCRYPTION, SCHEMABINDING, EXECUTE AS CALLER|SELF|OWNER|USER
RETURNS  @t TABLE (man_id int, man_fio VARCHAR(255), 
					bonusKPI money, bonus money, bonus_bdm_kpi money,  bonus_project_bdm money, manzp money, allsumm money , allsumm_without_coef money ,
					dep_name VARCHAR(255), dep_id int, pos_name VARCHAR(255), salary_month int, salary_quarter int, salary_month_str varchar(50), salary_year int)  

AS BEGIN

declare @mon1 int
    declare @mon2 int
    declare @ya1 int
    declare @ya2 int
    
    set @mon1 = (select DATEPART(mm,@date1))
    set @mon2 = (select DATEPART(mm,@date2))
    
    set @ya1 = (select DATEPART(yy,@ya1))
    set @ya2 = (select DATEPART(yy,@ya2))

    declare @detarmnet_tab table (dep_id int)

   if @department_id= 0 
	insert @detarmnet_tab
	select id from depatment_name()
else insert @detarmnet_tab values (@department_id)

declare @t1 TABLE (man_id int, man_fio VARCHAR(255),-- man_salary_all money, man_bonus money, 
                      bonus_bdm_kpi money, man_project_bonus money, manzp money, money_itog money, bonusKPI money, salary_month int, 
                      salary_year int, pos_name VARCHAR(255), dep_name VARCHAR(255), dep_id int, 
                      pos_id int, salary_quarter int, allsumm money, allsumm_without_coef money,
                      [percent] DECIMAL(5,2)) 
    
    declare @t2 TABLE (man_id int, --man_salary_all money, man_bonus money, 
	bonus_bdm_kpi money, man_project_bonus money,
                      manzp money, money_itog money, bonusKPI money,  salary_month int, salary_year int, 
                      pos_name VARCHAR(255), dep_name VARCHAR(255), dep_id int, pos_id int, 
                      salary_quarter int, money_by_candidate money, [percent] DECIMAL(5,2), is_freelancer int)  
 
;WITH FilteredUsers AS (
    SELECT DISTINCT 
        su.man_id,
        su.salary_month,
        su.salary_year,
        ubm.user_department,
        ubm.[percent],
        ubm.main_department,
        p.id AS pos_id,
        p.pos_name,
        d.dep_name,
        d.id AS dep_id,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_bonus_bdm_kpi, 0) ELSE 0 END AS man_bonus_bdm_kpi,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_salary_all, 0) ELSE 0 END AS man_salary_all,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_bonus, 0) ELSE 0 END AS man_bonus,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_bonus_bdm, 0) ELSE 0 END AS man_bonus_bdm,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_salary_ndfl, 0) ELSE 0 END AS man_salary_ndfl,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_salary_retention, 0) ELSE 0 END AS man_salary_retention,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.man_salary_single, 0) ELSE 0 END AS man_salary_single,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.salary_sick_days_pay, 0) ELSE 0 END AS salary_sick_days_pay,

        CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
             THEN ISNULL(su.salary_vacation_pay, 0) ELSE 0 END AS salary_vacation_pay,
		
		CASE WHEN ubm.user_department IN (SELECT id FROM depatment_name())
			THEN ISNULL(su.man_project_bonus, 0) ELSE 0 END AS man_project_bonus,

        su.salary_quarter
    FROM salary_by_user su
    JOIN userplanByMonth ubm
        ON ubm.user_id = su.man_id
       AND ubm.userplan_month = su.salary_month
       AND ubm.userplan_year  = su.salary_year
    LEFT JOIN depatment d ON d.id = ubm.user_department
    LEFT JOIN position  p ON p.id = ubm.user_position
    WHERE su.salary_month BETWEEN DATEPART(mm, @date1) AND DATEPART(mm, @date2)
      AND su.salary_year  = DATEPART(yy, @date1)
),
BonusData AS (
    SELECT
        b.man_id,
        b.mon AS salary_month,
		CASE WHEN b.mon BETWEEN 1 AND 3 THEN 1
				WHEN b.mon BETWEEN 4 AND 6 THEN 2
				WHEN b.mon BETWEEN 7 AND 9 THEN 3
				WHEN b.mon BETWEEN 10 AND 12 THEN 4 END salary_qrt,
        DATEPART(yy, @date1) AS salary_year,	
        b.dep_id,
        d.dep_name,
		b.pos_name,
		p.id pos_id,
        b.money_by_candidate,
        b.is_freelancer,
        SUM(b.money_by_candidate)
            OVER (PARTITION BY b.man_id, b.dep_id, b.mon, b.is_freelancer) AS money_itog
    FROM fn_User_Bonus_by_Details_New(@date1, @date2, '', '') b
    LEFT JOIN depatment d ON d.id = b.dep_id
	left join position p on p.pos_name = b.pos_name
),

KpiData AS (
    SELECT
        k.user_id AS man_id,
        k.mont AS salary_month,
        DATEPART(yy, k.date_kpi) AS salary_year,
        k.bonus
    FROM userBonusKPI(@date1, @date2) k
    WHERE DATEPART(yy, k.date_kpi) = DATEPART(yy, @date1)
),

MainData AS (
    SELECT
        ISNULL(fu.man_id, bd.man_id) AS man_id,

        ISNULL(fu.man_bonus_bdm_kpi, 0) AS man_bonus_bdm_kpi,
		ISNULL(fu.man_project_bonus, 0) AS man_project_bonus,		

        CASE
            WHEN fu.man_id IS NULL THEN 0
            ELSE
                CASE
                    WHEN fu.pos_id = 7 THEN
                        (fu.man_salary_all - fu.man_bonus - fu.man_bonus_bdm -
                         fu.man_salary_ndfl - fu.man_salary_retention - fu.man_project_bonus -
                         fu.man_salary_single * 0.87 - fu.man_bonus_bdm_kpi)
                         * ISNULL(fu.[percent], 100) / 100
                    ELSE
                        CASE
                            WHEN fu.main_department = 1 THEN
                                ((fu.man_salary_all - fu.man_bonus - fu.man_salary_ndfl -
                                  fu.man_salary_retention - fu.man_salary_single * 0.87 -
                                  fu.salary_sick_days_pay - fu.salary_vacation_pay)
                                  * ISNULL(fu.[percent], 100) / 100)
                                + fu.salary_sick_days_pay + fu.salary_vacation_pay
                            ELSE
                                (fu.man_salary_all - fu.man_bonus - fu.man_salary_ndfl -
                                 fu.man_salary_retention - fu.man_salary_single * 0.87 -
                                 fu.salary_sick_days_pay - fu.salary_vacation_pay)
                                 * ISNULL(fu.[percent], 100) / 100
                        END
                END
        END AS manzp,

        ISNULL(bd.money_itog, 0) AS money_itog,

        ISNULL(kd.bonus, 0) * ISNULL(fu.[percent], 100) / 100 AS bonusKPI,

        ISNULL(fu.salary_month, bd.salary_month) AS salary_month,
        ISNULL(fu.salary_year,  bd.salary_year)  AS salary_year,
		ISNULL(fu.pos_name,  bd.pos_name)  AS pos_name,
        ISNULL(fu.dep_name, bd.dep_name) AS dep_name,
        ISNULL(fu.dep_id,   bd.dep_id)   AS dep_id,
		ISNULL(fu.pos_id,  bd.pos_id)  AS pos_id,
		ISNULL(fu.salary_quarter,  bd.salary_qrt)  AS salary_quarter,
        ISNULL(bd.money_by_candidate, 0) AS money_by_candidate,
        ISNULL(fu.[percent], 100) AS [percent],
        ISNULL(bd.is_freelancer, 0) AS is_freelancer
    FROM FilteredUsers fu
    FULL OUTER JOIN BonusData bd
        ON bd.man_id       = fu.man_id
       AND bd.salary_month = fu.salary_month
       AND bd.salary_year  = fu.salary_year
       AND bd.dep_id       = fu.dep_id
    LEFT JOIN KpiData kd
        ON kd.man_id       = ISNULL(fu.man_id, bd.man_id)
       AND kd.salary_month = ISNULL(fu.salary_month, bd.salary_month)
       AND kd.salary_year  = ISNULL(fu.salary_year,  bd.salary_year)
)


    INSERT @t2
    SELECT * FROM MainData md


    INSERT @t1
    SELECT DISTINCT
        t.man_id, 
        m.man_fio, 
        t.bonus_bdm_kpi, 
		t.man_project_bonus,
        t.manzp, 
        t.money_itog, 
        t.bonusKPI, 
        t.salary_month, 
        t.salary_year, 
        t.pos_name, 
        t.dep_name, 
        t.dep_id, 
        t.pos_id, 
        t.salary_quarter,
        CASE 
            WHEN pos_id = 7 and t.man_id = 177874  THEN (ISNULL(manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonus_bdm_kpi, 0)) * 1.2 
			WHEN pos_id = 7 THEN (ISNULL(manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonus_bdm_kpi, 0)) * 1.4 			
            WHEN pos_id IN (26) THEN ISNULL(t.manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonusKPI, 0)    
            WHEN pos_id IN (23) THEN ISNULL(t.manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonusKPI, 0) 
            WHEN t.is_freelancer = 1 AND t.pos_id <> 23 THEN ISNULL(t.money_itog, 0)			
            ELSE (ISNULL(t.manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonusKPI, 0)) * 1.4 
        END allsumm,
        CASE 
            WHEN pos_id = 7 THEN (ISNULL(manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonus_bdm_kpi, 0)) 
            WHEN t.is_freelancer = 1 AND t.pos_id <> 23 THEN ISNULL(t.money_itog, 0)
            ELSE ISNULL(t.manzp, 0) + ISNULL(t.money_itog, 0) + ISNULL(t.bonusKPI, 0)                        
        END allsumm_without_coef,
        t.[percent]
    FROM @t2 t
    JOIN man m ON m.man_id = t.man_id

    INSERT @t
    SELECT DISTINCT 
        z.man_id, 
        z.man_fio,        
        SUM(ISNULL(z.bonusKPI, 0)) OVER (PARTITION BY z.man_id, z.salary_month, z.salary_year, z.dep_id) bonusKPI, 
        SUM(ISNULL(z.money_itog, 0)) OVER (PARTITION BY z.man_id, z.salary_month, z.salary_year, z.dep_id) bonus, 
        ISNULL(z.bonus_bdm_kpi, 0) bonus_bdm_kpi, 
		ISNULL(z.man_project_bonus, 0) man_project_bonus, 		 
        ISNULL(z.manzp, 0) manzp, 
        SUM(allsumm) OVER (PARTITION BY z.man_id, z.salary_month, z.salary_year, z.dep_id) allsumm, 
        SUM(allsumm_without_coef) OVER (PARTITION BY z.man_id, z.salary_month, z.salary_year, z.dep_id) allsumm_without_coef,	
        z.dep_name, 
        z.dep_id,
        z.pos_name, 
        z.salary_month, 
        z.salary_quarter,
        (SELECT [dbo].[fn_get_month_name_by_int] (z.salary_month)) salary_month_str,
        z.salary_year 
    FROM (
        SELECT 
            t1.man_id, 
            man_fio, 
            bonus_bdm_kpi, 
			man_project_bonus,
            manzp, 
            money_itog, 
            bonusKPI, 
            salary_month, 
            salary_year, 
            pos_name,
            dep_name, 
            dep_id, 
            pos_id, 
            salary_quarter, 
            allsumm, 
            allsumm_without_coef 
        FROM @t1 t1
    ) z    
    WHERE z.allsumm <> 0 AND z.dep_id IN (SELECT dep_id FROM @detarmnet_tab)
    ORDER BY z.salary_month


-- точно рабочий запрос. почему правильно работает тот, что выше - пока не понимаю
--declare @date1 datetime = convert(date, '20251001')
--declare @date2 datetime = convert(date, '20251031')

--   declare @detarmnet_tab table (dep_id int)
--  insert @detarmnet_tab
--        select id from depatment_name()


--    ;WITH MainData AS (
--        SELECT DISTINCT 
--            su.man_id,
--            su.salary_month,
--            su.salary_year,
--            su.salary_quarter,
            
--            -- Берем исходные данные без фильтра по отделам
--            ISNULL(su.man_bonus_bdm_kpi, 0) AS man_bonus_bdm_kpi,
--            ISNULL(su.man_salary_all, 0) AS man_salary_all,
--            ISNULL(su.man_bonus, 0) AS man_bonus,
--            ISNULL(su.man_bonus_bdm, 0) AS man_bonus_bdm,
--            ISNULL(su.man_salary_ndfl, 0) AS man_salary_ndfl,
--            ISNULL(su.man_salary_retention, 0) AS man_salary_retention,
--            ISNULL(su.man_salary_single, 0) AS man_salary_single,
--            ISNULL(su.salary_sick_days_pay, 0) AS salary_sick_days_pay,
--            ISNULL(su.salary_vacation_pay, 0) AS salary_vacation_pay,
            
--            -- Расчет manzp с учетом отделов
--            CASE 
--                WHEN ubm.user_department in (select id from depatment_name()) THEN
--                    CASE 
--                        WHEN p.id = 7 THEN 
--                            (ISNULL(su.man_salary_all, 0) - ISNULL(su.man_bonus, 0) - ISNULL(su.man_bonus_bdm, 0) - 
--                             ISNULL(su.man_salary_ndfl, 0) - ISNULL(su.man_salary_retention, 0) -
--                             (ISNULL(su.man_salary_single, 0)) * 0.87 - ISNULL(su.man_bonus_bdm_kpi, 0)) * 
--                             ISNULL(ubm.[percent], 100) / 100
--                        ELSE 
--                            CASE
--                                WHEN ubm.main_department = 1 THEN 
--                                    ((ISNULL(su.man_salary_all, 0) - ISNULL(su.man_bonus, 0) - ISNULL(su.man_salary_ndfl, 0) - 
--                                    ISNULL(su.man_salary_retention, 0) - (ISNULL(su.man_salary_single, 0)) * 0.87 - ISNULL(su.salary_sick_days_pay, 0) - ISNULL(su.salary_vacation_pay, 0)) * 
--                                    ISNULL(ubm.[percent], 100) / 100) + ISNULL(su.salary_sick_days_pay, 0) + ISNULL(su.salary_vacation_pay, 0)
--                                ELSE 
--                                    (ISNULL(su.man_salary_all, 0) - ISNULL(su.man_bonus, 0) - ISNULL(su.man_salary_ndfl, 0) - 
--                                    ISNULL(su.man_salary_retention, 0) - (ISNULL(su.man_salary_single, 0)) * 0.87 - ISNULL(su.salary_sick_days_pay, 0) - ISNULL(su.salary_vacation_pay, 0)) * 
--                                    ISNULL(ubm.[percent], 100) / 100 
--                            END
--                    END
--                ELSE 0  -- Если отдел не в списке - manzp = 0
--            END manzp,
            
--            -- Бонусы (всегда учитываем, независимо от отдела)
--            ISNULL(bonus.money_itog, 0) as money_itog,
--            ISNULL(bonus.money_by_candidate, 0) AS money_by_candidate,
            
--            -- KPI (всегда учитываем, независимо от отдела)
--            ISNULL(kpi.bonus, 0) * ISNULL(ubm.[percent], 100) / 100 AS bonusKPI,
            
--            -- Дополнительная информация
--            p.pos_name,
--            p.id as pos_id,
--            d.dep_name as main_dep_name,
--            d.id as main_dep_id,
--            bonus.dep_name as bonus_dep_name,
--            bonus.dep_id as bonus_dep_id,
--            ISNULL(ubm.[percent], 100) AS [percent],
--            ISNULL(bonus.is_freelancer, 0) as is_freelancer,
--            m.man_fio

--        FROM [salary_by_user] su
--        JOIN userplanByMonth ubm ON ubm.user_id = su.man_id 
--            AND ubm.userplan_month = su.salary_month 
--            AND ubm.userplan_year = su.salary_year
--        JOIN depatment d ON d.id = ubm.user_department
--        JOIN position p ON p.id = ubm.user_position
--        JOIN man m ON m.man_id = su.man_id
        
--        -- Бонусы (только ненулевые)
--        LEFT JOIN (
--            SELECT DISTINCT
--                bonus.man_id,
--                bonus.mon as salary_month,                
--                bonus.money_by_candidate as money_by_candidate,
--                bonus.is_freelancer as is_freelancer,
--                d.id as dep_id,
--                d.dep_name as dep_name,
--				bonus.ya,
--				SUM(bonus.money_by_candidate) OVER (PARTITION BY bonus.man_id, bonus.dep_id, bonus.mon, bonus.is_freelancer) as money_itog
--            FROM fn_User_Bonus_by_Details_New (@date1, @date2, '', '') bonus
--            LEFT JOIN depatment d ON d.id = bonus.dep_id
--            WHERE bonus.money_by_candidate != 0
--          --  GROUP BY bonus.man_id, bonus.mon
--        ) bonus ON bonus.man_id = su.man_id AND bonus.salary_month = su.salary_month and bonus.ya = su.salary_year
        
--        -- KPI (только ненулевые)
--        LEFT JOIN (
--            SELECT DISTINCT
--                user_id as man_id,
--                mont as salary_month,
--                bonus,
--				date_kpi
--            FROM userBonusKPI(@date1, @date2)
--            WHERE DATEPART(yy, date_kpi) = DATEPART(yy, @date1)
--            AND bonus != 0
--        ) kpi ON kpi.man_id = su.man_id AND kpi.salary_month = su.salary_month and datepart(yy,kpi.date_kpi) = su.salary_year
        
--        WHERE 
--            su.salary_month BETWEEN DATEPART(mm, @date1) AND DATEPART(mm, @date2) 
--            AND su.salary_year = DATEPART(yy, @date1)
--            -- Исключаем полностью нулевые записи (учитываем бонусы и KPI даже если manzp = 0)
--            AND (
--                su.man_salary_all != 0 OR su.man_bonus != 0 OR su.man_bonus_bdm != 0 
--                OR su.man_salary_ndfl != 0 OR su.man_salary_retention != 0 
--                OR su.man_salary_single != 0 OR su.salary_sick_days_pay != 0 
--                OR su.salary_vacation_pay != 0 OR su.man_bonus_bdm_kpi != 0
--                OR bonus.money_itog != 0 OR kpi.bonus != 0
--            )
--    ),
--    FinalData AS (
--        SELECT 
--            man_id,
--            man_fio,
--            bonusKPI,
--            money_itog as bonus,
--            man_bonus_bdm_kpi as bonus_bdm_kpi,
--            manzp,
--			money_itog,
--            CASE 
--                WHEN pos_id = 7 THEN (ISNULL(manzp, 0) + ISNULL(money_itog, 0) + ISNULL(man_bonus_bdm_kpi, 0)) * 1.4 
--                WHEN pos_id IN (26, 23) THEN ISNULL(manzp, 0) + ISNULL(money_itog, 0) + ISNULL(bonusKPI, 0)    
--                WHEN is_freelancer = 1 AND pos_id <> 23 THEN ISNULL(money_itog, 0)			
--                ELSE (ISNULL(manzp, 0) + ISNULL(money_itog, 0) + ISNULL(bonusKPI, 0)) * 1.4 
--            END allsumm,
--            CASE 
--                WHEN pos_id = 7 THEN (ISNULL(manzp, 0) + ISNULL(money_itog, 0) + ISNULL(man_bonus_bdm_kpi, 0)) 
--                WHEN is_freelancer = 1 AND pos_id <> 23 THEN ISNULL(money_itog, 0)
--                ELSE ISNULL(manzp, 0) + ISNULL(money_itog, 0) + ISNULL(bonusKPI, 0)                        
--            END allsumm_without_coef,
--            -- Выбор департамента: если manzp = 0, берем из бонусов, иначе из userplanByMonth
--            CASE WHEN manzp = 0 THEN bonus_dep_name ELSE main_dep_name END as dep_name,
--            CASE WHEN manzp = 0 THEN bonus_dep_id ELSE main_dep_id END as dep_id,
--            pos_name,
--            salary_month,
--            salary_quarter,
--            salary_year
--        FROM MainData
--    )

--    SELECT distinct
--        man_id,
--        man_fio,
--		ISNULL(bonusKPI, 0) bonusKPI, 
--        ISNULL(money_itog, 0) bonus, 
--        ISNULL(bonus_bdm_kpi, 0) bonus_bdm_kpi,  
--        ISNULL(manzp, 0) manzp, 
--        allsumm , 
--        allsumm_without_coef ,	        
--        dep_name,
--        dep_id,
--        pos_name,
--        salary_month,
--        salary_quarter,
--        (SELECT [dbo].[fn_get_month_name_by_int] (salary_month)) as salary_month_str,
--        salary_year
--    FROM FinalData
--    WHERE allsumm > 0 
--      AND dep_id IN (SELECT dep_id FROM @detarmnet_tab)
--    ORDER BY salary_month
 
 
 

	--------


	RETURN
END
