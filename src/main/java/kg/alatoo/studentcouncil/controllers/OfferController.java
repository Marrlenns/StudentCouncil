package kg.alatoo.studentcouncil.controllers;

import kg.alatoo.studentcouncil.dto.CommentDto;
import kg.alatoo.studentcouncil.entities.Comment;
import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.entities.Vote;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import kg.alatoo.studentcouncil.repositories.VoteRepository;
import kg.alatoo.studentcouncil.services.CommentService;
import kg.alatoo.studentcouncil.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final UserRepository userRepository;
    private final CommentService commentService;

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
        List<Comment> comments = commentService.getCommentsByOffer(offer);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> new CommentDto(
                        comment.getAuthor().getUsername(),
                        comment.getText(),
                        comment.getCreatedAt().format(formatter)
                ))
                .toList();

        String offerAuthor = offer.getAuthor().getUsername();
        String offerCreatedAt = offer.getCreated().format(formatter);

        model.addAttribute("offer", offer);
        model.addAttribute("comments", commentDtos);
        model.addAttribute("offerAuthor", offerAuthor);
        model.addAttribute("offerCreatedAt", offerCreatedAt);
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

    @PostMapping("/offers/{id}/{type}")
    @ResponseBody
    public Map<String, Integer> vote(@PathVariable Long id,
                                     @PathVariable String type,
                                     Authentication authentication) {
        Vote.VoteType voteType;
        if (type.equalsIgnoreCase("like")) {
            voteType = Vote.VoteType.LIKE;
        } else if (type.equalsIgnoreCase("dislike")) {
            voteType = Vote.VoteType.DISLIKE;
        } else {
            throw new IllegalArgumentException("Invalid vote type");
        }

        offerService.vote(id, authentication.getName(), voteType);
        Offer offer = offerService.getOffer(id);

        return Map.of(
                "likes", offer.getLikes(),
                "dislikes", offer.getDislikes()
        );
    }

    @PostMapping("/offers/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String commentText,
                             Authentication authentication) {
        Offer offer = offerService.getOffer(id);
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        commentService.addComment(commentText, user, offer);
        return "redirect:/offers/" + id;
    }

}
