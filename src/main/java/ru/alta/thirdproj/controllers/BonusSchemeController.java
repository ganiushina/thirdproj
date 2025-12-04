package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeRangeTable;
import ru.alta.thirdproj.services.BonusSchemeService;

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
        BonusSchemeRangeTable rangeTable = bonusSchemeService.getSchemeRangeTable();
        model.addAttribute("rangeColumns", rangeTable.getColumns());
        model.addAttribute("rangeRows", rangeTable.getRows());
        model.addAttribute("gaps", bonusSchemeService.getGaps());
        model.addAttribute("positions", bonusSchemeService.getPositions());
        model.addAttribute("schemeNames", bonusSchemeService.getSchemeNames());
        model.addAttribute("schemeEntry", new BonusSchemeEntry());
        return "bonus-schemes";
    }

    @PostMapping
    public String addScheme(@ModelAttribute("schemeEntry") BonusSchemeEntry entry) {
        bonusSchemeService.addScheme(entry);
        return "redirect:/bonus-schemes";
    }
}
