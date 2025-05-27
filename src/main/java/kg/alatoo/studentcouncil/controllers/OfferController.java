package kg.alatoo.studentcouncil.controllers;

import kg.alatoo.studentcouncil.dto.CommentDto;
import kg.alatoo.studentcouncil.dto.OfferViewDto;
import kg.alatoo.studentcouncil.entities.Comment;
import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.entities.Vote;
import kg.alatoo.studentcouncil.repositories.OfferRepository;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import kg.alatoo.studentcouncil.services.CommentService;
import kg.alatoo.studentcouncil.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final OfferRepository offerRepository;
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

                        comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName(),
                        comment.getText(),
                        comment.getCreatedAt().format(formatter)
                ))
                .toList();

        String offerAuthor = offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName();
        String offerCreatedAt = offer.getCreated().format(formatter);

        model.addAttribute("offer", offer);
        model.addAttribute("comments", commentDtos);
        model.addAttribute("offerAuthor", offerAuthor);
        model.addAttribute("offerCreatedAt", offerCreatedAt);
        return "offer-view";
    }

    @GetMapping("/offers")
    public String listOffers(Model model, Authentication authentication,
                             @RequestParam(defaultValue = "new") String sort) {
        List<Offer> offers = offerService.getAllOffers();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        List<OfferViewDto> offerDtos = offers.stream()
                .map(offer -> new OfferViewDto(
                        offer.getId(),
                        offer.getTitle(),
                        offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName(),
                        offer.getCreated().format(formatter),
                        offer.getImageUrl(),
                        offer.getDescription(),
                        commentService.countCommentsByOffer(offer),
                        offer.getLikes(),
                        offer.getDislikes()
                ))
                .sorted((o1, o2) -> {
                    if ("popular".equals(sort)) {
                        return Integer.compare(o2.getLikes(), o1.getLikes());
                    } else {
                        return Long.compare(o2.getId(), o1.getId());
                    }
                })
                .toList();

        model.addAttribute("offers", offerDtos);
        model.addAttribute("currentSort", sort);

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "offer-list-admin";
        }

        return "offer-list-user";
    }

    @GetMapping("/offers/filter")
    @ResponseBody
    public List<OfferViewDto> filterOffers(@RequestParam(defaultValue = "new") String sort) {
        List<Offer> offers = offerService.getAllOffers();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return offers.stream()
                .map(offer -> new OfferViewDto(
                        offer.getId(),
                        offer.getTitle(),
                        offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName(),
                        offer.getCreated().format(formatter),
                        offer.getImageUrl(),
                        offer.getDescription(),
                        commentService.countCommentsByOffer(offer),
                        offer.getLikes(),
                        offer.getDislikes()
                ))
                .sorted((o1, o2) -> {
                    if ("popular".equals(sort)) {
                        return Integer.compare(o2.getLikes(), o1.getLikes());
                    } else {
                        return Long.compare(o2.getId(), o1.getId());
                    }
                })
                .toList();
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


    @GetMapping("/my-offers")
    @Transactional(readOnly = true)
    public String myOffers(Model model,
                           @RequestParam(defaultValue = "new") String sort) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        List<Offer> userOffers = offerService.getOffersByUser(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        List<OfferViewDto> offerDtos = userOffers.stream()
                .map(offer -> new OfferViewDto(
                        offer.getId(),
                        offer.getTitle(),
                        offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName(),
                        offer.getCreated().format(formatter),
                        offer.getImageUrl(),
                        offer.getDescription(),
                        commentService.countCommentsByOffer(offer),
                        offer.getLikes(),
                        offer.getDislikes()
                ))
                .sorted((o1, o2) -> {
                    if ("popular".equals(sort)) {
                        return Integer.compare(o2.getLikes(), o1.getLikes());
                    } else {
                        return Long.compare(o2.getId(), o1.getId());
                    }
                })
                .toList();

        model.addAttribute("offers", offerDtos);
        model.addAttribute("currentSort", sort);
        model.addAttribute("isMyOffers", true);
        model.addAttribute("currentUser", user.getFirstName() + " " + user.getLastName());

        return "my-offers";
    }

    @GetMapping("/my-offers/filter")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<OfferViewDto> filterMyOffers(@RequestParam(defaultValue = "new") String sort,
                                             Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Offer> userOffers = offerService.getOffersByUser(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return userOffers.stream()
                .map(offer -> new OfferViewDto(
                        offer.getId(),
                        offer.getTitle(),
                        offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName(),
                        offer.getCreated().format(formatter),
                        offer.getImageUrl(),
                        offer.getDescription(),
                        commentService.countCommentsByOffer(offer),
                        offer.getLikes(),
                        offer.getDislikes()
                ))
                .sorted((o1, o2) -> {
                    if ("popular".equals(sort)) {
                        return Integer.compare(o2.getLikes(), o1.getLikes());
                    } else {
                        return Long.compare(o2.getId(), o1.getId());
                    }
                })
                .toList();
    }


//    @GetMapping("/offer/edit/{id}")
//    public String editOfferForm(@PathVariable Long id, Model model, Authentication authentication) {
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        Offer offer = offerRepository.findById(id).get();
//        if (offer == null) {
//            return "redirect:/my-offers";
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName();
//        User user = userRepository.findByUsername(username).orElseThrow();
//
//        if (!offer.getAuthor().equals(user)) {
//            return "redirect:/my-offers"; // Или показать ошибку доступа
//        }
//
//        model.addAttribute("offer", offer);
//        model.addAttribute("isEdit", true);
//
//        return "offer-form"; // ваш шаблон для создания/редактирования оффера
//    }

//    @PostMapping("/offer/edit/{id}")
//    public String updateOffer(@PathVariable Long id,
//                              @ModelAttribute OfferDto offerDto,
//                              Authentication authentication) {
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
//
//        Offer offer = offerService.findById(id);
//        if (offer == null) {
//            return "redirect:/my-offers";
//        }
//
//        // Проверяем права доступа
//        String currentUsername = authentication.getName();
//        User currentUser = userService.findByUsername(currentUsername);
//
//        if (!offer.getAuthor().equals(currentUser)) {
//            return "redirect:/my-offers";
//        }
//
//        // Обновляем оффер
//        offerService.updateOffer(id, offerDto);
//
//        return "redirect:/my-offers";
//    }

    @DeleteMapping("/offers/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteOffer(@PathVariable Long id, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Offer offer = offerRepository.findById(id).get();
        if (offer == null) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем права доступа
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!offer.getAuthor().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            offerService.deleteOffer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
