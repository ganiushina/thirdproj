package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.alta.thirdproj.entites.SchemeEntry;
import ru.alta.thirdproj.entites.SchemeRow;
import ru.alta.thirdproj.services.BonusSchemeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bonus-schemes")
public class BonusSchemeController {

    private final BonusSchemeService bonusSchemeService;

    @Autowired
    public BonusSchemeController(BonusSchemeService bonusSchemeService) {
        this.bonusSchemeService = bonusSchemeService;
    }

    @GetMapping
    public String showSchemePage(Model model) {

        Map<String, List<SchemeRow>> byScheme =
                bonusSchemeService.getSchemesGroupedByScheme();
        // Берём дату первой схемы (если есть)
        LocalDate dateScheme = null;
        if (!byScheme.isEmpty()) {
            var firstEntry = byScheme.values().iterator().next();
            if (!firstEntry.isEmpty()) {
                dateScheme = firstEntry.get(0).getDateScheme();
            }
        }
        model.addAttribute("dateScheme", dateScheme);
        model.addAttribute("byScheme", byScheme);
        model.addAttribute("gaps", bonusSchemeService.getGaps());
        model.addAttribute("positions", bonusSchemeService.getPositions());
        model.addAttribute("schemeNames", bonusSchemeService.getSchemeNames());
        model.addAttribute("schemeEntry", bonusSchemeService.createEmptyEntry());

        return "bonus-schemes";
    }

    @PostMapping
    public String addScheme(@ModelAttribute("schemeEntry") SchemeEntry entry) {
        bonusSchemeService.addScheme(entry);
        return "redirect:/bonus-schemes";
    }
}
