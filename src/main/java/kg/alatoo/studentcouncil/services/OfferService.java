package kg.alatoo.studentcouncil.services;

import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface OfferService {
    Offer saveOffer(String title, String description, MultipartFile image, User user);

    Offer getOffer(Long id);

    List<Offer> getAllOffers();

    void deleteOffer(Long id);
}
