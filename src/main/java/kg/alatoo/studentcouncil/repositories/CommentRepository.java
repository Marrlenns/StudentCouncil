package kg.alatoo.studentcouncil.repositories;

import kg.alatoo.studentcouncil.entities.Comment;
import kg.alatoo.studentcouncil.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByOfferOrderByCreatedAtDesc(Offer offer);
}
