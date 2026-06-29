package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.ActBuhInfo;
import ru.alta.thirdproj.entites.ActDistributionRow;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.repositories.ActDistributionRepository;

import java.util.List;

@Service
public class ActDistributionServiceImpl implements ActDistributionService {

    private final ActDistributionRepository repository;

    @Autowired
    public ActDistributionServiceImpl(ActDistributionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ActBuhInfo> getAllActs() {
        return repository.getAllActs();
    }

    @Override
    public ActBuhInfo getActById(int actId) {
        return repository.getActById(actId);
    }

    @Override
    public List<ActDistributionRow> getRowsByActId(int actId) {
        return repository.getRowsByActId(actId);
    }

    @Override
    public void addRow(ActDistributionRow row, double totalNoNds) {
        calculateSums(row, totalNoNds);
        repository.addRow(row);
    }

    @Override
    public void updateRow(ActDistributionRow row, double totalNoNds) {
        calculateSums(row, totalNoNds);
        repository.updateRow(row);
    }

    @Override
    public void deleteRow(int id) {
        repository.deleteRow(id);
    }

    @Override
    public List<Employer> getEmployees() {
        return repository.getEmployees();
    }

    /**
     * Рассчитывает суммы и нормированные проценты (доля от суммы акта).
     *
     * Логика:
     *   consultantSum  = totalNoNds * consultantPercent / 100
     *   researcherSum  = totalNoNds * researcherPercent / 100
     *   consultantPercentOfTotal  = consultantPercent / 100   (дробная форма)
     *   researcherPercentOfTotal  = researcherPercent / 100
     */
    private void calculateSums(ActDistributionRow row, double totalNoNds) {
        double consPercent  = row.getConsultantPercent()  != null ? row.getConsultantPercent()  : 0.0;
        double resPercent   = row.getResearcherPercent()  != null ? row.getResearcherPercent()  : 0.0;

        row.setConsultantSum(round2(totalNoNds * consPercent / 100.0));
        row.setResearcherSum(round2(totalNoNds * resPercent  / 100.0));

        row.setConsultantPercentOfTotal(round4(consPercent / 100.0));
        row.setResearcherPercentOfTotal(round4(resPercent  / 100.0));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
