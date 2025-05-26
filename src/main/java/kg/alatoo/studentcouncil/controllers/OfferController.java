package kg.alatoo.studentcouncil.controllers;

import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.entities.Vote;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import kg.alatoo.studentcouncil.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final UserRepository userRepository;

    @GetMapping("/offer/new")
    public String showForm() {
        return "offer-form";
    }

    @PostMapping("/offers")
    public String createOffer(@RequestParam String title,
                                @RequestParam String description,
                                @RequestParam MultipartFile image,
                                Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            Offer offer = offerService.saveOffer(title, description, image, user);
            return "redirect:/offers/" + offer.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при сохранении продукта");
            return "offer-form";
        }
    }

    @GetMapping("/offers/{id}")
    public String viewOffer(@PathVariable Long id, Model model) {
        Offer offer = offerService.getOffer(id);
        model.addAttribute("offer", offer);
        return "offer-view";
    }

    @GetMapping("/offers")
    public String listOffers(Model model, Authentication authentication) {
        List<Offer> offers = offerService.getAllOffers();
        model.addAttribute("offers", offers);

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "offer-list-admin";
        }

        return "offer-list-user";
    }

    @PostMapping("/offers/{id}/delete")
    public String deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return "redirect:/offers";
    }

    @PostMapping("/offers/{id}/like")
    public String like(@PathVariable Long id, Authentication authentication) {
        offerService.vote(id, authentication.getName(), Vote.VoteType.LIKE);
        return "redirect:/offers";
    }

    @PostMapping("/offers/{id}/dislike")
    public String dislike(@PathVariable Long id, Authentication authentication) {
        offerService.vote(id, authentication.getName(), Vote.VoteType.DISLIKE);
        return "redirect:/offers";
    }


}
