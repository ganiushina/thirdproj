package ru.alta.thirdproj.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.ActBuhInfo;
import ru.alta.thirdproj.entites.ActDistributionRow;
import ru.alta.thirdproj.services.ActDistributionService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/act-distribution")
public class ActDistributionController {

    private static final Logger log = LoggerFactory.getLogger(ActDistributionController.class);

    private final ActDistributionService service;

    @Autowired
    public ActDistributionController(ActDistributionService service) {
        this.service = service;
    }

    /**
     * Главная страница — список всех актов с признаком распределения.
     * GET /act-distribution
     */
    @GetMapping
    public String showPage(Model model) {
        model.addAttribute("acts", service.getAllActs());
        model.addAttribute("employees", service.getEmployees());
        return "act-distribution";
    }

    /**
     * Строки project_buh для конкретного акта (AJAX).
     * GET /act-distribution/{actId}/rows
     */
    @GetMapping("/{actId}/rows")
    @ResponseBody
    public List<ActDistributionRow> getRows(@PathVariable int actId) {
        return service.getRowsByActId(actId);
    }

    /**
     * Добавить строку распределения.
     * POST /act-distribution/{actId}/row
     * Body: JSON с полями ActDistributionRow + totalNoNds акта
     */
    @PostMapping("/{actId}/row")
    @ResponseBody
    public ResponseEntity<?> addRow(
            @PathVariable int actId,
            @RequestBody Map<String, Object> body) {
        try {
            ActDistributionRow row = buildRowFromBody(actId, body);
            double totalNoNds = getDouble(body, "totalNoNds");
            service.addRow(row, totalNoNds);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Ошибка добавления строки распределения actId={}", actId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Обновить строку распределения.
     * PUT /act-distribution/row/{id}
     */
    @PutMapping("/row/{id}")
    @ResponseBody
    public ResponseEntity<?> updateRow(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {
        try {
            // actId берём из существующих строк, передаём 0 — сервис не трогает act_id при UPDATE
            ActDistributionRow row = buildRowFromBody(0, body);
            row.setId(id);
            double totalNoNds = getDouble(body, "totalNoNds");
            service.updateRow(row, totalNoNds);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Ошибка обновления строки id={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Удалить строку распределения.
     * DELETE /act-distribution/row/{id}
     */
    @DeleteMapping("/row/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRow(@PathVariable int id) {
        try {
            service.deleteRow(id);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Ошибка удаления строки id={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Вспомогательные методы ──────────────────────────────────────────────

    private ActDistributionRow buildRowFromBody(int actId, Map<String, Object> body) {
        ActDistributionRow row = new ActDistributionRow();
        row.setActId(actId);
        row.setConsultantName(getString(body, "consultantName"));
        row.setConsultantId(getInteger(body, "consultantId"));
        row.setConsultantPercent(getDouble(body, "consultantPercent"));
        row.setResearcherName(getString(body, "researcherName"));
        row.setResearcherId(getInteger(body, "researcherId"));
        row.setResearcherPercent(getDouble(body, "researcherPercent"));
        row.setDepartment(getString(body, "department"));
        row.setDepartmentId(getInteger(body, "departmentId"));
        row.setDepartmentResearcher(getString(body, "departmentResearcher"));
        row.setDepartmentResearcherId(getInteger(body, "departmentResearcherId"));
        row.setTeamLeaderName(getString(body, "teamLeaderName"));
        row.setTeamLeaderId(getInteger(body, "teamLeaderId"));
        row.setCity(getString(body, "city"));
        row.setCityId(getInteger(body, "cityId"));
        return row;
    }

    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : v.toString().trim().isEmpty() ? null : v.toString().trim();
    }

    private Integer getInteger(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private double getDouble(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0.0;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
