/*
    FIX: месячная рассылка ДР («День рождения коллег в этом месяце») показывает
         «Команда\Должность: null, null».

    Причина: ветка @day = 100 берёт направление/должность из userplanByMonth
    строго за ТЕКУЩИЙ месяц (datepart(mm/yy, getdate())). Рассылка уходит по крону
    1-го числа в 6:00 (ScheduledConfiguration.executeTaskUserMonth), когда план
    на новый месяц ещё не заполнен -> LEFT JOIN не находит строку -> dep_name/pos_name = NULL.

    Решение: брать ПОСЛЕДНИЙ доступный план сотрудника (свежайший year+month, не позже
    текущего) через OUTER APPLY. На 1-е число подхватится прошлый месяц, а как только
    появится план текущего месяца — возьмётся он.

    Изменена ТОЛЬКО ветка @day = 100. Ветки @day <> 0 и @day = 0 не тронуты.
*/

ALTER FUNCTION [dbo].[GetUserBirDay] (@day int)
RETURNS @t TABLE (man_id int, man_fio varchar(255), bdate date, name_p_c varchar(255), stst varchar(255), login_user_id int, login_user_name varchar(255), man_email_man_email varchar(255),
					dep_name varchar(255), pos_name varchar(255), city varchar(255), chphoto int)
AS
begin
	--IF @day = 1 др сотрудника завтра, 100 - все сотрудники в этом месяце, 0 - клиенты

--	declare @date date = convert(date, '20250301')
	IF @day = 100
	INSERT @t
select s.man_id,
				s.man_fio,
				s.bdate,
				s.company_name,
				s.st,
				s.login_user_id, s.man_i
				,s.man_email_man_email
				, ISNULL(s.dep_name, ''), ISNULL(s.position_name, ''), s.man_adr_settlement, s.ch
from (
		select distinct m.man_id,
				m.man_f + ' ' + m.man_i man_fio,
				convert(date, m.man_b_date) as bdate, datepart(dd, m.man_b_date) bdateday,
				c.company_name,
				'Сотрудник' st,
				l1.login_user_id, m1.man_i
				,me1.man_email_man_email
				, dep_name, CASE when pos_name like 'R%' then 'Рекрутер'
										   WHEN pos_name like 'K%' THEN 'Консультант'
										   WHEN pos_name like 'Buh%' THEN 'Административный персонал'
										   WHEN pos_name like 'IT Support' THEN 'IT Support'
										   WHEN pos_name like 'BDM%' THEN 'BDM'
										   else pos_name
										  end position_name, m.man_adr_settlement
										   ,case when m.man_photo_id IS not NULL and m.man_photo_id <> -1 THEN 1 ELSE 0 END ch
										 -- ,mp.photo, datepart(mm, m.man_b_date) mon
										 from man m
join login l on l.login_user_id = m.man_id
left join dbo.man_email me on me.man_email_man_id = m.man_id
JOIN login l1 ON l.login_user_id <> l1.login_user_id AND l1.login_active =1
join dbo.man m1 on l1.login_user_id = m1.man_id
left join dbo.man_email me1 on me1.man_email_man_id = l1.login_user_id and me1.man_email_man_email like '%@altapersonnel.ru%'
left JOIN dbo.company c ON c.company_id = m.man_last_company_id
-- FIX: вместо жёсткого джойна по текущему месяцу — последний доступный план сотрудника
OUTER APPLY (
	SELECT TOP 1 ubm.user_position, ubm.user_department
	FROM [userplanByMonth] ubm
	WHERE ubm.user_id = l.login_user_id
	  AND (ubm.userplan_year * 100 + ubm.userplan_month)
	      <= datepart(yy, getdate()) * 100 + datepart(mm, getdate())
	ORDER BY ubm.userplan_year DESC, ubm.userplan_month DESC
) ubm
left JOIN dbo.position p ON p.id = ubm.user_position
LEFT JOIN depatment dep ON dep.id = ubm.user_department
where m.man_last_company_id =114915 and l.login_active=1
and datepart(mm, m.man_b_date) = datepart(mm, getdate()) --datepart(mm, @date)
and me1.man_email_man_email is not null
) s order by bdateday

	else
	IF @day <> 0
	INSERT @t
		select  distinct
				m.man_id,
				m.man_f + ' ' + m.man_i man_fio,
				convert(date, m.man_b_date) as bdate,
				isnull(c.company_name, '') company_name,
				'Сотрудник' st,
				l1.login_user_id, m1.man_i
				,isnull(me1.man_email_man_email, '') man_email_man_email
				, ISNULL(dep_name, ''), ISNULL(CASE when pos_name like 'R%' then 'Рекрутер'
										   WHEN pos_name like 'K%' THEN 'Консультант'
										   WHEN pos_name like 'Buh%' THEN 'Административный персонал'
										   WHEN pos_name like 'IT Support' THEN 'IT Support'
										   WHEN pos_name like 'BDM%' THEN 'BDM' else pos_name
										  end, '') position_name, k.name
										   ,case when m.man_photo_id IS not NULL and m.man_photo_id <> -1 THEN 1 ELSE 0 END ch
										 -- ,mp.photo
				from dbo.login l
				LEFT JOIN dbo.kladr k ON k.id = l.login_classifier_city_new
				join dbo.man m on l.login_user_id = m.man_id
				--left JOIN dbo.man_photo mp ON mp.id = m.man_photo_id
				CROSS JOIN(select convert(date, getdate()))d(today)
				left join dbo.man_email me on me.man_email_man_id = m.man_id
				JOIN login l1 ON l.login_user_id <> l1.login_user_id AND l1.login_active =1
				join dbo.man m1 on l1.login_user_id = m1.man_id
				left join dbo.man_email me1 on me1.man_email_man_id = l1.login_user_id and me1.man_email_man_email like '%@altapersonnel.ru%'
				left JOIN dbo.company c ON c.company_id = m.man_last_company_id
				-- FIX: последний доступный план сотрудника вместо жёсткого текущего месяца
				OUTER APPLY (
					SELECT TOP 1 ubm.user_position, ubm.user_department
					FROM [userplanByMonth] ubm
					WHERE ubm.user_id = l.login_user_id
					  AND (ubm.userplan_year * 100 + ubm.userplan_month)
					      <= datepart(yy, getdate()) * 100 + datepart(mm, getdate())
					ORDER BY ubm.userplan_year DESC, ubm.userplan_month DESC
				) ubm
				left JOIN dbo.position p ON p.id = ubm.user_position
				LEFT JOIN depatment dep ON dep.id = ubm.user_department
				WHERE convert(date,dateadd(year,year(d.today)-year(m.man_b_date),m.man_b_date)) = convert(date,dateadd(dd, @day, getdate())) and
				l.login_active = 1
				and me.man_email_man_email like '%@altapersonnel.ru%'
				and me1.man_email_man_email is not null
				and l1.login_user_id<> 101740

	else
	IF @day = 0
	--IF @day <> 0
	INSERT @t

SELECT distinct m.man_id, m.man_fio,
		convert(date, m.man_b_date) as bdate,
		isnull(CASE WHEN p.project_id IS NULL THEN c.company_name ELSE p.project_name END, '') nm,
		isnull(CASE WHEN p.project_id IS NOT NULL THEN rt.recruiting_type_name WHEN c.company_id IS NOT NULL THEN 'Клиент'  WHEN b.recruiting_type_id = -3 THEN 'Важный человек' END, '') kl_name  ,
		b.user_id, m1.man_i,
		me.man_email_man_email, isnull((case when (m.man_last_company_id is null)
		then m.man_last_company_name else com.company_name end), '') man_last_company_name,
		isnull(m.man_last_company_position, '') man_last_company_position, k.name--, mp.photo
		, 0
		from dbo.ManBirthday b
		JOIN login l ON b.user_id = l.login_user_id
		JOIN dbo.man m ON b.man_id = m.man_id
		JOIN dbo.man m1 ON l.login_user_id = m1.man_id
		left JOIN dbo.man_photo mp ON mp.id = m.man_photo_id
		LEFT JOIN dbo.kladr k ON k.id = m.man_town
		left JOIN dbo.company com ON com.company_id = m.man_last_company_id
		CROSS JOIN(select convert(date, getdate()))d(today)
		left JOIN dbo.project p ON p.project_id = b.project_id
		LEFT JOIN dbo.recruiting_type rt ON rt.recruiting_type_id = b.recruiting_type_id
		left join dbo.man_email me on me.man_email_man_id = b.user_id
		left JOIN dbo.company c ON c.company_id = b.project_id
		WHERE
		convert(date,dateadd(year,year(d.today)-year(m.man_b_date),m.man_b_date)) = convert(date, getdate()) and
		me.man_email_man_email like '%@altapersonnel.ru%'
		AND b.checked = 1 AND l.login_active = 1
RETURN
end
