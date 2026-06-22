-- ============================================================================
--  Шаг 2a: fn_marginality_by_month — потребление реверсов из margin_bonus_correction
-- ----------------------------------------------------------------------------
--  Изменение ЧИСТО АДДИТИВНОЕ:
--    margin       = zarabotano - salary + ISNULL(reversal, 0)
--    vsego_margin = SUM(zarabotano) - SUM(salary) + SUM(reversal)
--  reversal — сумма bonus_amount по отделу/месяцу/году из margin_bonus_correction
--  (correction_month/_year). При ПУСТОЙ таблице reversal = 0 → вывод побитно
--  равен прежнему. Состав и порядок выходных колонок НЕ изменён (совместимость).
-- ============================================================================

ALTER FUNCTION [dbo].[fn_marginality_by_month]
(
    @date1 date,
    @date2 date
)
RETURNS TABLE
AS
RETURN
(
    select
        dep.dep_name,
        isnull(cast(z1.summ_zarabotano_depatment as money), 0) summ_zarabotano_depatment,
        isnull(cast(z1.sum_salary_depatment as money), 0) sum_salary_depatment,
        -- [CORR] + реверс месяца
        isnull(cast((z1.summ_zarabotano_depatment - z1.sum_salary_depatment + isnull(corr.reversal,0)) as money), 0) margin,
        (SELECT [dbo].[fn_get_month_name_by_int](z1.salary_month)) salary_month,
        z1.salary_year,
        isnull(cast(SUM(z1.summ_zarabotano_depatment) over (partition by z1.dep_id) as money), 0) summ_vsego_zarabotano,
        isnull(cast(SUM(z1.sum_salary_depatment) over (partition by z1.dep_id) as money), 0) summ_vsego_potracheno,
        -- [CORR] + суммарный реверс по отделу
        isnull(cast((SUM(z1.summ_zarabotano_depatment) over (partition by z1.dep_id)
                     - SUM(z1.sum_salary_depatment) over (partition by z1.dep_id)
                     + SUM(isnull(corr.reversal,0)) over (partition by z1.dep_id)) as money), 0) vsego_margin
    from (
        select
            dep_id,
            salary_month,
            salary_year,
            sum(sum_salary_depatment) as sum_salary_depatment,
            sum(summ_zarabotano_depatment) as summ_zarabotano_depatment
        from (
            select
                dep_id,
                salary_month,
                salary_year,
                sum(allsumm_with_coef) as sum_salary_depatment,
                isnull(max(tdepq.summ_total), 0) as summ_zarabotano_depatment
            from (
            -- исправления - убрала бонус бдм из маржи
                SELECT z.*,
                    ISNULL(z.allsumm, 0)
                    - ISNULL(z.bonus_bdm_kpi, 0)
                      * (ISNULL(z.allsumm, 0) / NULLIF(ISNULL(z.allsumm_without_coef, 0), 0))
                    AS allsumm_with_coef
                FROM [dbo].[fn_salary_for_all_user](@date1, @date2, DEFAULT) z
            ) sel
            left join (
                select
                    s.summ_total + isnull(sum_by_invoce, 0) as summ_total,
                    s.depatment_id,
                    s.mon,
                    s.ya
                from (
                    SELECT DISTINCT
                        sum(summ_responsible_user) over(PARTITION BY datepart(mm, date_act), datepart(yy,date_act), z1.depatment_id) as summ_total,
                        (isnull(sum_by_invoce, 0)) as sum_by_invoce,
                        datepart(yy,date_act) as ya,
                        z1.depatment_id,
                        datepart(mm, date_act) as mon
                    FROM ( -- добавила таблицу с измененными актами в связи с непрошедшими испытательный срок
                            SELECT
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.responsible_user_id   ELSE pb.responsible_user_id   END AS responsible_user_id,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.summ_responsible_user ELSE pb.summ_responsible_user END AS summ_responsible_user,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.date_update ELSE ab.date_act END AS date_act,
                                CASE WHEN pba.act_id IS NOT NULL THEN pba.depatment_id  ELSE pb.depatment_id END AS depatment_id
                            FROM dbo.act_buh ab
                            JOIN dbo.project_buh pb  ON pb.act_id = ab.id
                            LEFT JOIN dbo.v_project_buh_actual pba ON pba.act_id = pb.act_id
                            WHERE
                                  (
                                      CONVERT(date, ab.date_act) BETWEEN @date1 AND @date2
                                      OR CONVERT(date, pba.date_update) BETWEEN @date1 AND @date2
                                  )
                            AND pb.responsible_user_id IS NOT NULL
                            AND pb.depatment_id IN (1,2,3,4,11, 6, 12, 13, 15)
                    ) z1
                    left join (
                        select distinct
                             (sum(inv.summ*pb.percent_responsible_user_complicity/100) over(
                                PARTITION BY datepart(mm, inv.date_issue), datepart(yy,inv.date_issue), pb.depatment_id))/1.2 as sum_by_invoce,
                            pb.depatment_id,
                            datepart(mm, inv.date_issue) as mm,
                            datepart(yy, inv.date_issue) as ya
                        from invoice inv
                        join project_buh pb on pb.act_id = inv.act_id
                        where inv.date_issue between @date1 AND @date2
                    ) inv_by_dep on inv_by_dep.depatment_id = z1.depatment_id
                        and inv_by_dep.mm = datepart(mm, date_act)
                        and inv_by_dep.ya = datepart(yy,date_act)
                    WHERE convert(date, date_act) BETWEEN @date1 AND @date2
                        AND z1.depatment_id not in (7,9,10)
                ) s
            ) tdepq on tdepq.depatment_id = dep_id
                and tdepq.mon = salary_month
                and tdepq.ya = salary_year
            group by dep_id, salary_month, salary_year
        ) z
        group by dep_id, salary_month, salary_year
    ) z1
    join depatment dep on dep.id = z1.dep_id
    -- [CORR] агрегат реверсов по отделу/месяцу/году (пустая таблица → нет строк → reversal 0)
    left join (
        SELECT depatment_id, correction_month, correction_year, SUM(bonus_amount) AS reversal
        FROM dbo.margin_bonus_correction
        GROUP BY depatment_id, correction_month, correction_year
    ) corr
        on corr.depatment_id = z1.dep_id
       and corr.correction_month = z1.salary_month
       and corr.correction_year = z1.salary_year
)
