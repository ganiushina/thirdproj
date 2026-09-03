-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE FUNCTION [dbo].[fn_salary_for_all_user_sverka]
(	
	 @date1 DATETIME,
     @date2 datetime 
)
RETURNS TABLE 
AS
RETURN 
(
		select distinct m.man_fio, cast(  isnull(su.man_salary_all, 0)  as money) man_salary_all, cast(su.man_bonus as money) man_bonus, 
		cast(su.man_bonus_bdm_kpi as money) man_bonus_bdm_kpi, cast(su.man_project_bonus  as money) man_project_bonus, cast(su.man_bonus_bdm as money) man_bonus_bdm, 
		cast(su.man_salary_ndfl as money) man_salary_ndfl, cast(su.man_salary_single as money) man_salary_single,
		cast(case when p.id = 7 then isnull(su.man_salary_all, 0) - isnull(su.man_bonus, 0) - isnull(su.man_bonus_bdm, 0)
			- isnull(su.man_salary_retention, 0) - isnull(su.man_project_bonus, 0) 
			- (isnull(su.man_salary_single, 0))*0.87 - isnull(su.man_bonus_bdm_kpi, 0)
			- isnull(su.salary_vacation_pay, 0) - isnull(su.salary_sick_days_pay, 0)
				else 
					isnull(su.man_salary_all, 0) - isnull(su.man_bonus, 0) - isnull(su.man_salary_retention, 0) -  (isnull(su.man_salary_single, 0))*0.87
					- isnull(su.salary_vacation_pay, 0) - isnull(su.salary_sick_days_pay, 0) - isnull(su.man_project_bonus, 0) end as money) manzp, 
			--cast(case when p.id = 7 then isnull(su.man_salary_all, 0) - isnull(su.man_bonus, 0) - isnull(su.man_bonus_bdm, 0) - isnull(man_salary_ndfl, 0)
			--- isnull(su.man_salary_retention, 0) - isnull(su.man_project_bonus, 0) 
			--- (isnull(su.man_salary_single, 0))*0.87 - isnull(su.man_bonus_bdm_kpi, 0)
			--	else 
			--		isnull(su.man_salary_all, 0) - isnull(su.man_bonus, 0) - isnull(man_salary_ndfl, 0) - isnull(su.man_salary_retention, 0) -  (isnull(su.man_salary_single, 0))*0.87 end as money) manzp, 		
		cast(isnull(su.salary_sick_days_pay, 0) as money) salary_sick_days_pay, cast(isnull(su.salary_vacation_pay, 0) as money) salary_vacation_pay,
	   (SELECT [dbo].[fn_get_month_name_by_int] (su.salary_month)) salary_month_str , su.salary_month,		
		su.salary_year, p.pos_name, --d.dep_name, 
		STUFF((
        SELECT DISTINCT ', ' + d2.dep_name
        FROM [salary_by_user] su2
        JOIN userplanByMonth ubm2 ON ubm2.user_id = su2.man_id 
            AND ubm2.userplan_month = su2.salary_month 
            AND ubm2.userplan_year = su2.salary_year
        JOIN depatment d2 ON d2.id = ubm2.user_department
        WHERE su2.man_id = su.man_id 
            AND su2.salary_month = su.salary_month 
            AND su2.salary_year = su.salary_year
        FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS dep_name,
		su.salary_quarter
		from  [salary_by_user] su
		join userplanByMonth ubm on ubm.user_id = su.man_id and ubm.userplan_month = su.salary_month and ubm.userplan_year = su.salary_year
		join depatment d on d.id =ubm.user_department
		join position p on p.id = ubm.user_position		
		join man m on m.man_id = su.man_id
		where su.salary_month BETWEEN DATEPART(mm, @date1) AND DATEPART(mm, @date2) 
							AND su.salary_year = DATEPART(yy, @date1)	
						  --where man_id = 9349454   
      --  order by m.man_fio, su.salary_month
)
