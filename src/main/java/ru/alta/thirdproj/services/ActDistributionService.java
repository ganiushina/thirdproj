package ru.alta.thirdproj.services;

import ru.alta.thirdproj.entites.ActBuhInfo;
import ru.alta.thirdproj.entites.ActDistributionRow;
import ru.alta.thirdproj.entites.Employer;

import java.util.List;

public interface ActDistributionService {

    /** Все акты (с признаком — распределён или нет) */
    List<ActBuhInfo> getAllActs();

    /** Один акт по id */
    ActBuhInfo getActById(int actId);

    /** Строки project_buh для конкретного акта */
    List<ActDistributionRow> getRowsByActId(int actId);

    /**
     * Добавить строку распределения.
     * Перед сохранением автоматически рассчитывает суммы:
     *   consultantSum = totalNoNds * consultantPercent / 100
     *   researcherSum = totalNoNds * researcherPercent / 100
     */
    void addRow(ActDistributionRow row, double totalNoNds);

    /**
     * Обновить строку распределения.
     * Пересчитывает суммы по тем же правилам.
     */
    void updateRow(ActDistributionRow row, double totalNoNds);

    /** Удалить строку */
    void deleteRow(int id);

    /** Справочник сотрудников для выпадающих списков */
    List<Employer> getEmployees();
}
