package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.services.BonusSchemeService;

import javax.validation.Valid;

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
        model.addAttribute("gaps", bonusSchemeService.getGaps());
        model.addAttribute("positions", bonusSchemeService.getPositions());
        model.addAttribute("schemeLimits", bonusSchemeService.getSchemeLimits());
        model.addAttribute("bdmLimits", bonusSchemeService.getBdmLimits());
        model.addAttribute("schemeNames", bonusSchemeService.getSchemeNames());
        model.addAttribute("newScheme", new BonusSchemeEntry());
        return "bonus-schemes";
    }

    @PostMapping
    public String createScheme(@Valid @ModelAttribute("newScheme") BonusSchemeEntry entry,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("gaps", bonusSchemeService.getGaps());
            model.addAttribute("positions", bonusSchemeService.getPositions());
            model.addAttribute("schemeLimits", bonusSchemeService.getSchemeLimits());
            model.addAttribute("bdmLimits", bonusSchemeService.getBdmLimits());
            model.addAttribute("schemeNames", bonusSchemeService.getSchemeNames());
            return "bonus-schemes";
        }

        bonusSchemeService.addScheme(entry);
        return "redirect:/bonus-schemes";
    }
}
