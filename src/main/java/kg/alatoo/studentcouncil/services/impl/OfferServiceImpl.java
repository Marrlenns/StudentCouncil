package kg.alatoo.studentcouncil.services.impl;

import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.entities.Vote;
import kg.alatoo.studentcouncil.repositories.OfferRepository;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import kg.alatoo.studentcouncil.repositories.VoteRepository;
import kg.alatoo.studentcouncil.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Override
    public Offer saveOffer(String title, String description, MultipartFile image, User user) {
        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setCreated(LocalDateTime.now());

        offer.setAuthor(user);

        try {
            if (image != null && !image.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
                offer.setImageUrl(base64Image);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при обработке изображения", e);
        }

        return offerRepository.save(offer);
    }

    @Override
    public Offer getOffer(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found with id: " + id));
    }

    @Override
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }


    @Override
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    public void vote(Long offerId, String username, Vote.VoteType voteType) {
        Offer offer = offerRepository.findById(offerId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        Optional<Vote> existingVote = voteRepository.findByUserAndOffer(user, offer);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                voteRepository.delete(vote);
                if (voteType == Vote.VoteType.LIKE) offer.setLikes(offer.getLikes() - 1);
                else offer.setDislikes(offer.getDislikes() - 1);
            } else {
                if (voteType == Vote.VoteType.LIKE) {
                    offer.setLikes(offer.getLikes() + 1);
                    offer.setDislikes(offer.getDislikes() - 1);
                } else {
                    offer.setDislikes(offer.getDislikes() + 1);
                    offer.setLikes(offer.getLikes() - 1);
                }
                vote.setVoteType(voteType);
                voteRepository.save(vote);
            }
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setOffer(offer);
            vote.setVoteType(voteType);
            voteRepository.save(vote);

            if (voteType == Vote.VoteType.LIKE) offer.setLikes(offer.getLikes() + 1);
            else offer.setDislikes(offer.getDislikes() + 1);
        }

        offerRepository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> getOffersByUser(User user) {
        return offerRepository.findByAuthorOrderByCreatedDesc(user);
    }

}
