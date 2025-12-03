package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.alta.thirdproj.entites.BonusSchemeLimitView;
import ru.alta.thirdproj.services.BonusSchemeService;

import java.util.List;

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
        List<BonusSchemeLimitView> bonusSchemeLimitViewList = bonusSchemeService.getSchemeLimitDetails();
        model.addAttribute("bonusSchemeView", bonusSchemeLimitViewList);
        return "bonus-schemes";
    }
}
