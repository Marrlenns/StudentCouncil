package kg.alatoo.studentcouncil.repositories;

import kg.alatoo.studentcouncil.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
