-- ============================================================
-- ROLLBACK / БЭКАП fn_User_Bonus_Payment_NEW
-- Снято с боевой БД recruting 2026-06-29, ДО применения [FIX-3].
-- Чтобы откатить FIX-3 — выполнить этот скрипт целиком на recruting.
-- (CREATE заменён на ALTER, т.к. функция уже существует.)
-- ============================================================

ALTER FUNCTION [dbo].[fn_User_Bonus_Payment_NEW]
(@date1 date, @date2 date, @userId int, @department_id int)
RETURNS @t_res TABLE (
	man_id int,
	man_fio nvarchar(200) NULL,
	dep_name nvarchar(100),
	act_num nvarchar(200) NULL,
	act_id int,
	paid bit,
	date_act date,
	candidate nvarchar(200) NULL,
	project_id int,
	company_name varchar(200) NULL,
	bonus money,
	persent int,
	all_bonus money,
	date_for_pay date,
	payment_date date,
	payment_real_date date,
	emploedUser varchar(200),
	payment_buh_id uniqueidentifier
)
AS
BEGIN

DECLARE @date3 datetime;
	SET @date3 = (SELECT CASE WHEN datepart(dd, @date2) = 1 THEN @date2 ELSE
		DATEADD(m,1,DATEADD(mm, DATEDIFF(m,0,@date2),0)) END)

DECLARE @date1_m date = DATEADD(month, DATEDIFF(month, 0, @date1), 0)

	DECLARE @mydate date = (
		SELECT MIN(ab.date_act) FROM act_buh ab
		WHERE ab.id IN (
			SELECT pb.act_id FROM payment_buh pb
			WHERE pb.payment_date BETWEEN DATEADD(mm, -3, @date1_m) AND @date2
		)
	)

DECLARE @date4 date = (SELECT DATEADD(month, DATEDIFF(month, 0, @mydate), 0))

DECLARE @ya_date2          int       = DATEPART(yy, @date2)
DECLARE @mon_date2         int       = DATEPART(mm, @date2)
DECLARE @date2_hi          datetime2 = DATEADD(day, 1, CAST(@date2   AS datetime2))
DECLARE @t4_end            datetime  = DATEADD(day, 1, CAST(@date2   AS datetime))
DECLARE @loop_month_start  datetime
DECLARE @loop_month_end    datetime


DECLARE @tmp_money TABLE (
	user_id int, user_name varchar(255), company_name varchar(255), candidate varchar(255),
	summ_user money, money_tmp_total money,
	position_id int, dep_id int, schem int, bet float,
	mon int, ya int, act_id int, date_act datetime, user_percent float
)

DECLARE @tmp_money_consultant TABLE (
	user_id int, user_name varchar(255), company_name varchar(255), candidate varchar(255),
	summ_user money, money_tmp_total money,
	mon int, ya int, act_id int, date_act datetime, dep_id int, responsible_user_percent float
)

DECLARE @tmp_money_resecher TABLE (
	user_id int, user_name varchar(255), company_name varchar(255), candidate varchar(255),
	summ_user money, money_tmp_total money,
	mon int, ya int, act_id int, date_act datetime, dep_id int, resercher_percent float
)

DECLARE @tmp_money_extra_bonus TABLE (
	user_id int, user_name varchar(255), company_name varchar(255), candidate varchar(255),
	summ_user money, money_tmp_total money,
	mon int, ya int, act_id int, date_act datetime
)

INSERT @tmp_money_consultant
SELECT DISTINCT
	responsible_user_id, responsible_user_name, company_name, candidate, summ_responsible_user,
	SUM(CASE WHEN CHARINDEX('freelancer', responsible_user_name) = 0 THEN summ_responsible_user ELSE 0 END)
		OVER(PARTITION BY responsible_user_id, DATEPART(mm,date_act), DATEPART(yy,date_act) ORDER BY DATEPART(qq,date_act)) summ_total,
	DATEPART(mm,date_act) mon, DATEPART(yy,date_act) ya, act_id, date_act, depatment_id, percent_responsible_user_by_candidate_percent
FROM (
	SELECT
		pb.responsible_user_id, responsible_user_name, summ_responsible_user, ab.date_act, ab.company_name,
		ab.candidate, pb.act_id, pb.depatment_id, pb.percent_responsible_user_by_candidate_percent
	FROM dbo.act_buh ab
	JOIN project_buh pb ON pb.act_id = ab.id
	WHERE ab.date_act >= @date4 AND ab.date_act < @date2_hi
		AND pb.responsible_user_id IS NOT NULL
		AND pb.depatment_id IN (1,2,3,4,11,6,12,13,15)
) z1


INSERT @tmp_money_resecher
SELECT DISTINCT
	s4.resecher_id, resecher_name, company_name, candidate, summ_resecher, summ_total,
	mon, ya, act_id, date_act, depatment_id, percent_reseacher_by_candidate_percent
FROM (
	SELECT
		z1.resecher_id, resecher_name, company_name, candidate, summ_resecher,
		SUM(CASE WHEN CHARINDEX('freelancer', resecher_name) = 0 THEN summ_resecher ELSE 0 END)
			OVER(PARTITION BY resecher_id, mon, ya) summ_total,
		mon, ya, act_id, date_act, depatment_id, percent_reseacher_by_candidate_percent
	FROM (
		SELECT DISTINCT
			pb.resecher_id, resecher_name, summ_resecher, date_act,
			DATEPART(mm, ab.date_act) mon, DATEPART(yy,date_act) ya,
			ab.company_name, ab.candidate, pb.act_id,
			CASE WHEN ISNULL(pb.depatment_resecher_id, 0) = 0 THEN ubm.user_department
				ELSE pb.depatment_resecher_id END depatment_id,
			pb.percent_reseacher_by_candidate_percent
		FROM dbo.act_buh ab
		JOIN project_buh pb ON pb.act_id = ab.id
		LEFT JOIN dbo.userplanByMonth ubm
			ON ubm.user_id = pb.resecher_id
			AND ubm.userplan_month = DATEPART(mm, ab.date_act)
			AND ubm.userplan_year = DATEPART(yy, ab.date_act)
		WHERE ab.date_act >= @date4 AND ab.date_act < @date2_hi
			AND pb.resecher_id IS NOT NULL
			AND pb.depatment_id IN (1,2,3,4,11,6,12,13,15)
	) z1
) s4


INSERT @tmp_money_extra_bonus
SELECT
	s.employer_id, s.man_fio, s.company_name, s.candidate, s.extra_bonus,
	SUM(s.extra_bonus)
		OVER(PARTITION BY employer_id, DATEPART(mm,date_act), DATEPART(yy,date_act) ORDER BY DATEPART(qq,date_act)) summ_total,
	DATEPART(mm,date_act) mon, DATEPART(yy,date_act) ya, act_id, date_act
FROM (
	SELECT DISTINCT
		eb.employer_id, m.man_fio, ab.company_name,
		ab.candidate + ' ' + CAST(eb.bonus_percent AS varchar(200)) + '% доп' candidate,
		CAST(((CAST(eb.bonus_percent AS DEC(12,4))) * CAST(ab.total_no_nds AS DEC(12,4))) / (100.) AS DEC(12,4)) extra_bonus,
		ab.date_act, eb.act_id
	FROM extra_bonus eb
	JOIN dbo.act_buh ab ON ab.id = eb.act_id
	JOIN dbo.man m ON m.man_id = eb.employer_id
	WHERE ab.date_act >= @date1 AND ab.date_act < @date2_hi
) s


INSERT @tmp_money
SELECT DISTINCT
	tm.user_id, tm.user_name, company_name, candidate, tm.summ_user, tm.money_tmp_total,
	ubm.user_position, tm.dep_id, ubm.user_scheme, ubm.bet,
	tm.mon, tm.ya, tm.act_id, date_act, tm.responsible_user_percent
FROM @tmp_money_consultant tm
JOIN man m ON m.man_id = tm.user_id
JOIN dbo.userplanByMonth ubm
	ON ubm.user_id = tm.user_id
	AND ubm.userplan_month = tm.mon
	AND ubm.userplan_year = tm.ya


INSERT @tmp_money
SELECT DISTINCT
	tm.user_id, tm.user_name, company_name, candidate, tm.summ_user, tm.money_tmp_total,
	CASE WHEN ubm.user_position NOT IN (1,2,14,17,18,22,25,24) AND DATEPART(mm, date_act) <> ubmres.userplan_month
		THEN ubmres.user_position ELSE ubm.user_position END pos_id,
	CASE WHEN ubm.user_position NOT IN (1,2,14,17,18,22,25,24) AND DATEPART(mm, date_act) <> ubmres.userplan_month
		THEN ubmres.user_department ELSE ubm.user_department END dep_id,
	CASE WHEN ubm.user_position NOT IN (1,2,14,17,18,22,25,24) AND DATEPART(mm, date_act) <> ubmres.userplan_month
		THEN ubmres.user_scheme ELSE ubm.user_scheme END scheme,
	ubm.bet,
	CASE WHEN ubm.user_position NOT IN (1,2,14,17,18,22,25,24) AND DATEPART(mm, date_act) <> ubmres.userplan_month
		THEN ubmres.userplan_month ELSE tm.mon END mon,
	tm.ya, tm.act_id, date_act, tm.resercher_percent
FROM @tmp_money_resecher tm
JOIN man m ON m.man_id = tm.user_id
LEFT JOIN dbo.userplanByMonth ubm
	ON ubm.user_id = tm.user_id
	AND ubm.userplan_month = tm.mon
	AND ubm.userplan_year = tm.ya
	AND ubm.user_department = tm.dep_id
LEFT JOIN (
	SELECT ubm.user_id, ubm.user_department, ubm.user_position, ubm.user_scheme, ubm.userplan_month
	FROM userplanByMonth ubm
	JOIN (
		SELECT DISTINCT s.user_id,
			MAX(ubml.userplan_month) OVER (PARTITION BY s.user_id ORDER BY ubml.userplan_month DESC) mon,
			ya, ubml.user_department, ubml.user_scheme
		FROM userplanByMonth ubml
		JOIN (
			SELECT DISTINCT ubmmax.user_id, MAX(ubmmax.userplan_year) ya, MAX(ubmmax.user_scheme) uscheme
			FROM userplanByMonth ubmmax
			WHERE ubmmax.user_position IN (1,2,14,17,18,22,25,24)
			GROUP BY ubmmax.user_id
		) s ON s.user_id = ubml.user_id AND s.ya = ubml.userplan_year AND ubml.user_scheme = s.uscheme
		WHERE ubml.user_position IN (1,2,14,17,18,22,25,24)
			AND ubml.user_id IN (SELECT tr.user_id FROM @tmp_money_resecher tr)
	) s2 ON s2.user_id = ubm.user_id
		AND s2.user_department = ubm.user_department
		AND ubm.userplan_month = s2.mon
		AND ubm.userplan_year = s2.ya
) ubmres ON ubmres.user_id = tm.user_id


INSERT @tmp_money
SELECT DISTINCT
	tm.user_id, tm.user_name, tm.company_name, tm.candidate, tm.summ_user, tm.money_tmp_total,
	ISNULL(ubm.user_position, ubm_last.user_position),
	ISNULL(ubm.user_department, ubm_last.user_department),
	ISNULL(ubm.user_scheme, ubm_last.user_scheme),
	ISNULL(ubm.bet, ubm_last.bet),
	tm.mon, tm.ya, tm.act_id, tm.date_act, NULL
FROM @tmp_money_extra_bonus tm
JOIN man m ON m.man_id = tm.user_id
LEFT JOIN dbo.userplanByMonth ubm
	ON ubm.user_id = tm.user_id
	AND ubm.userplan_month = tm.mon
	AND ubm.userplan_year = tm.ya
OUTER APPLY (
	SELECT TOP 1 user_position, user_department, user_scheme, bet
	FROM dbo.userplanByMonth
	WHERE user_id = tm.user_id
		AND (userplan_year < tm.ya OR (userplan_year = tm.ya AND userplan_month <= tm.mon))
	ORDER BY userplan_year DESC, userplan_month DESC
) ubm_last


DECLARE @tabl_itog TABLE (
	user_id int, user_name varchar(255), money_itog money, money_by_candidate money,
	persent float, company_name varchar(255), candidate varchar(255),
	summ_user money, summ_total money, mon int, ya int,
	position_id int, dep_id int, act_id int, user_percent float
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
	,@user_percent float

DECLARE cur1 CURSOR FOR (
	SELECT
		tm.user_id, tm.user_name,
		CASE
			WHEN CONVERT(date, tm.date_act) < CONVERT(date, '20251001') THEN
				CASE
					WHEN ISNULL(ubm.probation, ISNULL(ubm_last.probation, 0)) = 0 THEN tm.money_tmp_total / tm.bet
					ELSE (tm.money_tmp_total / tm.bet) * 2
				END
			ELSE
				CASE
					WHEN ISNULL(ubm.probation, ISNULL(ubm_last.probation, 0)) = 0 THEN tm.money_tmp_total
					ELSE tm.money_tmp_total * 2
				END
		END,
		tm.money_tmp_total, tm.company_name, tm.candidate, tm.summ_user,
		tm.position_id, tm.dep_id, tm.schem, tm.mon, tm.ya, tm.act_id, tm.date_act, tm.user_percent
	FROM @tmp_money tm
	LEFT JOIN dbo.userplanByMonth ubm
		ON ubm.user_id = tm.user_id
		AND ubm.userplan_month = tm.mon
		AND ubm.userplan_year = tm.ya
		AND ubm.user_department = tm.dep_id
	OUTER APPLY (
		SELECT TOP 1 probation
		FROM dbo.userplanByMonth
		WHERE user_id = tm.user_id
			AND (userplan_year < tm.ya OR (userplan_year = tm.ya AND userplan_month <= tm.mon))
		ORDER BY userplan_year DESC, userplan_month DESC
	) ubm_last
)

OPEN cur1
FETCH NEXT FROM cur1 INTO @user_id, @user_name, @money_itog1, @money_total, @company_name, @candidate, @summ_user,
	@pos_id, @dep_id, @scheme_id, @mon, @ya, @act_id, @date_act, @user_percent

WHILE @@fetch_status = 0
BEGIN
	DECLARE @formula1 dec(12,4)
	DECLARE @formula2 dec(12,4)

	IF (@mon <> DATEPART(mm, @date_act))
	BEGIN
		DECLARE @monye_itog_last_month money

		SET @loop_month_start = CAST(DATEFROMPARTS(@ya, @mon, 1) AS datetime)
		SET @loop_month_end   = DATEADD(month, 1, @loop_month_start)

		SET @monye_itog_last_month = (
			SELECT DISTINCT summ_total FROM (
				SELECT z1.resecher_id, summ_resecher,
					SUM(summ_resecher) OVER(PARTITION BY resecher_id, mon, ya) summ_total,
					mon, ya, act_id, date_act
				FROM (
					SELECT DISTINCT pb.resecher_id, summ_resecher, date_act,
						DATEPART(mm, ab.date_act) mon, DATEPART(yy,date_act) ya,
						ab.company_name, ab.candidate, pb.act_id
					FROM dbo.act_buh ab
					JOIN project_buh pb ON pb.act_id = ab.id
					WHERE ab.date_act >= @loop_month_start AND ab.date_act < @loop_month_end
						AND pb.resecher_id = @user_id
						AND pb.depatment_id IN (1,2,3,4,11,6,12,13,15)
				) z1
			) s4
		)

		IF @monye_itog_last_month > 0
		BEGIN
			SET @percent = (
				SELECT TOP 1 gap_percent FROM scheme_limits sl
				JOIN gap g ON g.gap_id = sl.gap_id
				WHERE limits <= @monye_itog_last_month
					AND position_id = @pos_id
					AND sl.scheme_id = @scheme_id
					AND sl.date_scheme = (
						SELECT MAX(date_scheme) FROM (
							SELECT sl.date_scheme FROM scheme_limits sl GROUP BY sl.date_scheme
						) s
						WHERE @date_act >= s.date_scheme
					)
				ORDER BY sl.limits DESC
			)
		END
		ELSE
		BEGIN
			SET @percent = (
				SELECT TOP 1 gap_percent FROM scheme_limits sl
				JOIN gap g ON g.gap_id = sl.gap_id
				WHERE limits = 0.00
					AND position_id = @pos_id
					AND sl.scheme_id = @scheme_id
					AND sl.date_scheme = (
						SELECT MAX(date_scheme) FROM (
							SELECT sl.date_scheme FROM scheme_limits sl GROUP BY sl.date_scheme
						) s
						WHERE @date_act >= s.date_scheme
					)
				ORDER BY sl.limits DESC
			)
		END

		SET @formula1 = (CAST(@money_itog1 AS dec(12,4)) * @percent) / 100
		IF @formula1 IS NOT NULL
			SET @formula2 = (CAST(@summ_user AS dec(12,4)) * @percent) / 100
	END

	ELSE IF (CHARINDEX('%', @candidate) > 0)
	BEGIN
		SET @percent = (
			SELECT eb.bonus_percent FROM dbo.extra_bonus eb
			WHERE eb.employer_id = @user_id AND eb.act_id = @act_id
		)
		SET @formula1 = @money_itog1
		SET @formula2 = @summ_user
		SET @money_itog1 = 0
	END

	ELSE IF (@pos_id <> 26) AND ((SELECT CHARINDEX('freelancer', @user_name)) > 0)
	BEGIN
		SET @percent = 40
		SET @formula1 = (CAST(@money_itog1 AS dec(12,4)) * @percent) / 100
		SET @formula2 = (CAST(@summ_user AS dec(12,4)) * @percent) / 100
	END

	ELSE IF (@pos_id = 26) OR ((SELECT CHARINDEX('sales', @user_name)) > 0)
	BEGIN
		SET @formula1 = @summ_user
		SET @formula2 = @summ_user
		IF (@pos_id = 26)
			SET @percent = (
				SELECT pb.percent_responsible_user_complicity FROM project_buh pb
				WHERE pb.act_id = @act_id AND pb.responsible_user_id = @user_id
			)
		IF ((SELECT CHARINDEX('sales', @user_name)) > 0)
			SET @percent = (
				SELECT pb.percent_responsible_user_complicity FROM project_buh pb
				WHERE pb.act_id = @act_id
					AND pb.responsible_user_id = @user_id
					AND pb.responsible_user_name = @user_name
			)
	END

	ELSE
	BEGIN
		SET @percent = (
			SELECT TOP 1 gap_percent FROM scheme_limits sl
			JOIN gap g ON g.gap_id = sl.gap_id
			WHERE limits <= @money_itog1
				AND position_id = @pos_id
				AND sl.scheme_id = @scheme_id
				AND sl.date_scheme = (
					SELECT MAX(date_scheme) FROM (
						SELECT sl.date_scheme FROM scheme_limits sl GROUP BY sl.date_scheme
					) s
					WHERE @date_act >= s.date_scheme
				)
			ORDER BY sl.limits DESC
		)

		SET @formula1 = (CAST(@money_itog1 AS dec(12,4)) * @percent) / 100
		IF @formula1 IS NOT NULL
			SET @formula2 = (CAST(@summ_user AS dec(12,4)) * @percent) / 100
	END

	IF @formula1 IS NOT NULL
		INSERT @tabl_itog VALUES (
			@user_id, @user_name, @formula1, @formula2, @percent,
			@company_name, @candidate, @summ_user, @money_total,
			DATEPART(mm, @date_act), @ya, @pos_id, @dep_id, @act_id, @user_percent
		)

	FETCH NEXT FROM cur1 INTO @user_id, @user_name, @money_itog1, @money_total, @company_name, @candidate, @summ_user,
		@pos_id, @dep_id, @scheme_id, @mon, @ya, @act_id, @date_act, @user_percent
END

CLOSE cur1
DEALLOCATE cur1


DECLARE @t4 TABLE (
	act_id int, pay_buh_id uniqueidentifier, pya_sum money,
	summ money, payment_date datetime, act_date datetime
)

INSERT @t4
SELECT DISTINCT
	pb.act_id, pb.pay_guid, pb.sum pya_sum,
	CASE WHEN ab.total = pb.sum THEN ab.total_no_nds ELSE pb.sum / 1.2 END summ,
	pb.payment_date, ab.date_act
FROM payment_buh pb
JOIN dbo.act_buh ab ON ab.id = pb.act_id
WHERE pb.payment_date >= DATEADD(mm, -2, CAST(@date1_m AS datetime)) AND pb.payment_date < @t4_end
	AND act_id <> 446


DECLARE @TempResults TABLE (
	user_id INT, act_num VARCHAR(255), act_id INT, date_act DATETIME,
	candidate VARCHAR(255), project_id INT, company_name VARCHAR(255),
	money_by_candidate MONEY, persent FLOAT, money_itog MONEY,
	paied INT, payment_summ MONEY, payment_date DATETIME,
	mon INT, ya INT, date_for_pay DATETIME,
	real_date DATETIME, real_mon INT, real_ya INT,
	empPaid VARCHAR(500), payment_buh_id uniqueidentifier
)

;WITH PaymentData AS (
	SELECT
		ps.employer_id, ps.act_id, ps.id AS payment_id,
		ps.payment_date, ps.payment_real_summ, ps.payment_summ,
		m.man_fio_short,
		m.man_fio_short + ' ' + CONVERT(varchar(20), ps.payment_real_summ) AS empPaid,
		ROW_NUMBER() OVER (PARTITION BY ps.employer_id, ps.act_id ORDER BY ps.payment_date DESC) AS payment_rn,
		ps.pay_guid
	FROM paymentSuccess ps
	JOIN man m ON m.man_id = ps.user_id
	WHERE ps.payment_real_summ IS NOT NULL
),
ItogData AS (
	SELECT ti.*,
		ROW_NUMBER() OVER (PARTITION BY ti.user_id, ti.act_id ORDER BY ti.money_by_candidate) AS itog_rn
	FROM @tabl_itog ti
)
INSERT INTO @TempResults
SELECT DISTINCT
	ti.user_id, ab.act_num, ab.id AS act_id, ab.date_act,
	ab.candidate, ab.project_id, ab.company_name,
	CASE
		WHEN ab.total = t4.pya_sum OR ti.user_percent IS NULL THEN ti.money_by_candidate
		ELSE t4.summ * ti.user_percent * ti.persent / 100
	END AS money_by_candidate,
	ti.persent, ti.money_itog,
	CASE WHEN pd.act_id > 0 AND t4.pay_buh_id = pd.pay_guid THEN 1 ELSE 0 END AS paied,
	pd.payment_summ, t4.payment_date,
	DATEPART(mm, t4.payment_date) AS mon,
	DATEPART(yy, t4.payment_date) AS ya,
	CASE
		WHEN p.id IN (1,2,18,17,21,22,14,24,25)
		THEN DATEADD(DAY, 1,  EOMONTH(t4.payment_date, 1))
		ELSE DATEADD(DAY, 15, EOMONTH(t4.payment_date, 1))
	END AS date_for_pay,
	pd.payment_date AS real_date,
	DATEPART(mm, pd.payment_date) AS real_mon,
	DATEPART(yy, pd.payment_date) AS real_ya,
	pd.empPaid, t4.pay_buh_id
FROM ItogData ti
JOIN @t4 t4 ON ti.act_id = t4.act_id
JOIN dbo.project_buh pb ON t4.act_id = pb.act_id
JOIN dbo.act_buh ab ON ab.id = pb.act_id
LEFT JOIN PaymentData pd
	ON pd.act_id = ti.act_id
	AND pd.employer_id = ti.user_id
	AND pd.payment_rn = ti.itog_rn
JOIN dbo.position p ON p.id = ti.position_id
JOIN dbo.depatment d ON d.id = ti.dep_id


;WITH FilteredData AS (
	SELECT DISTINCT
		s.user_id,
		ISNULL(m.man_fio, '') + ' ' + ISNULL(p.pos_name, '') man_fio,
		STUFF((
			SELECT DISTINCT ', ' + d2.dep_name
			FROM (
				SELECT TOP 1 ubm3.userplan_year AS max_yr, ubm3.userplan_month AS max_mon
				FROM dbo.userplanByMonth ubm3
				WHERE ubm3.user_id = s.user_id
					AND (ubm3.userplan_year < @ya_date2
						OR (ubm3.userplan_year = @ya_date2 AND ubm3.userplan_month <= @mon_date2))
				ORDER BY ubm3.userplan_year DESC, ubm3.userplan_month DESC
			) mx
			JOIN dbo.userplanByMonth ubm2
				ON ubm2.user_id = s.user_id
				AND ubm2.userplan_year  = mx.max_yr
				AND ubm2.userplan_month = mx.max_mon
			JOIN dbo.depatment d2 ON d2.id = ubm2.user_department
			FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS dep_names,
		ISNULL(s.act_num, 0) act_num,
		ISNULL(s.act_id, 0) act_id,
		s.paied, s.date_act,
		ISNULL(s.candidate, '') candidate,
		ISNULL(s.project_id, 0) project_id,
		ISNULL(s.company_name, '') company_name,
		ISNULL(ROUND(CAST(s.money_by_candidate AS FLOAT), 2, 0), 0) money_by_candidate,
		ISNULL(s.persent, 0) persent,
		s.date_for_pay, s.payment_date, s.real_date, s.empPaid, s.payment_buh_id
	FROM (
		SELECT DISTINCT
			z.user_id, LEFT(z.act_num, 11) act_num, z.act_id,
			CASE WHEN paied > 0 THEN 1 ELSE paied END paied,
			z.date_act, z.candidate, z.project_id, z.company_name,
			ROUND(CAST(z.money_by_candidate AS FLOAT), 2, 0) money_by_candidate,
			z.persent, z.date_for_pay, z.payment_date, z.mon, ya,
			z.real_date, z.empPaid, z.payment_buh_id
		FROM (
			SELECT
				user_id, act_num, act_id, date_act, candidate, project_id, company_name,
				money_by_candidate, persent, money_itog, paied, payment_summ,
				payment_date, mon, ya, date_for_pay, real_date, real_mon, real_ya,
				empPaid, payment_buh_id
			FROM @TempResults tr
		) z

		UNION

		SELECT DISTINCT
			eb.employer_id,
			LEFT(ab.act_num, 11) act_num,
			ab.id,
			ISNULL(ps.act_id, 0) paied,
			ab.date_act,
			'Доп бонус: ' + ab.candidate,
			ab.project_id,
			ab.company_name,
			CAST(((CAST(eb.bonus_percent AS DEC(12,4))) * CAST(ab.total_no_nds AS DEC(12,4))) / (100.) AS DEC(12,4)) extra_bonus,
			eb.bonus_percent,
			CASE
				WHEN ISNULL(ubm_eb.user_position, ubm_eb_last.user_position) IN (1,2,18,17,21,22,14,24,25)
				THEN DATEADD(DAY, 1,  EOMONTH(t4.payment_date, 1))
				ELSE DATEADD(DAY, 15, EOMONTH(t4.payment_date, 1))
			END AS date_for_pay,
			t4.payment_date,
			DATEPART(mm, t4.payment_date) mon,
			DATEPART(yy, t4.payment_date) ya,
			ps.payment_date real_date,
			ISNULL(m1.man_fio_short, '') + ' ' + CONVERT(VARCHAR(20), ps.payment_summ) empPaid,
			t4.pay_buh_id
		FROM extra_bonus eb
		JOIN dbo.act_buh ab ON ab.id = eb.act_id
		JOIN @t4 t4 ON t4.act_id = eb.act_id
		LEFT JOIN paymentSuccess ps ON ps.act_id = eb.act_id AND ps.employer_id = eb.employer_id
		LEFT JOIN man m1 ON m1.man_id = ps.user_id
		LEFT JOIN dbo.userplanByMonth ubm_eb
			ON ubm_eb.user_id = eb.employer_id
			AND ubm_eb.userplan_month = DATEPART(mm, ab.date_act)
			AND ubm_eb.userplan_year = DATEPART(yy, ab.date_act)
		OUTER APPLY (
			SELECT TOP 1 user_position
			FROM dbo.userplanByMonth
			WHERE user_id = eb.employer_id
				AND (userplan_year < DATEPART(yy, ab.date_act)
					OR (userplan_year = DATEPART(yy, ab.date_act)
						AND userplan_month <= DATEPART(mm, ab.date_act)))
			ORDER BY userplan_year DESC, userplan_month DESC
		) ubm_eb_last

		UNION

		SELECT DISTINCT
			ubk.user_id,
			NULL yy, NULL xx,
			CASE WHEN ps.id > 0 THEN 1 ELSE 0 END paied,
			NULL vv,
			'KPI - ' + ubk.mon candidate,
			NULL qq, 'KPI' ee,
			ubk.all_bonus, NULL tt,
			CASE
				WHEN p.id IN (1,2,18,17,21,22,14,24,25)
				THEN DATEADD(DAY, 1,  EOMONTH(ubk.date_kpi, 1))
				ELSE DATEADD(DAY, 15, EOMONTH(ubk.date_kpi, 1))
			END date_for_pay,
			ubk.date_kpi,
			ubk.mont,
			DATEPART(yy, @date1) ya,
			ps.payment_date real_date,
			ISNULL(m1.man_fio_short, '') + ' ' + CONVERT(VARCHAR(20), ps.payment_summ) empPaid,
			NULL payment_buh_id
		FROM userBonusKPI(DATEADD(mm, -2, @date1_m), DATEADD(mm, -1, @date3)) ubk
		LEFT JOIN dbo.position p ON p.pos_name = ubk.position
		LEFT JOIN paymentSuccess ps
			ON ps.employer_id = ubk.user_id
			AND ps.month_kpi = ubk.mont
			AND ps.act_id = 0
			AND ps.payment_date BETWEEN @date1 AND @date2
		LEFT JOIN man m1 ON m1.man_id = ps.user_id
	) s
	OUTER APPLY (
		SELECT TOP 1 user_position, user_department, user_scheme
		FROM dbo.userplanByMonth
		WHERE user_id = s.user_id
			AND (userplan_year < @ya_date2
				OR (userplan_year = @ya_date2 AND userplan_month <= @mon_date2))
		ORDER BY userplan_year DESC, userplan_month DESC
	) ubm
	LEFT JOIN man m ON m.man_id = s.user_id
	LEFT JOIN dbo.position p ON p.id = ubm.user_position
	WHERE s.date_for_pay BETWEEN @date1 AND @date2
)
INSERT @t_res
SELECT
	user_id, man_fio, dep_names, act_num, act_id, paied, date_act,
	candidate, project_id, company_name, money_by_candidate, persent,
	ISNULL((SUM(money_by_candidate) OVER (PARTITION BY user_id)), 0) all_bonus,
	date_for_pay, payment_date, real_date, empPaid, payment_buh_id
FROM FilteredData
ORDER BY persent DESC

RETURN
END
