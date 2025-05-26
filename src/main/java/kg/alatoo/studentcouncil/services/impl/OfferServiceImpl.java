package kg.alatoo.studentcouncil.services.impl;

import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.repositories.OfferRepository;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import kg.alatoo.studentcouncil.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

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
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
    }

    @Override
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }


    @Override
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

}
