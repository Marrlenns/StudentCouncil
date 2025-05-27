package kg.alatoo.studentcouncil.repositories;

import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByAuthorOrderByCreatedDesc(User author);
    List<Offer> findByAuthor(User author);
}
