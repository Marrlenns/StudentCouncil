package kg.alatoo.studentcouncil.services.impl;

import kg.alatoo.studentcouncil.entities.Comment;
import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.repositories.CommentRepository;
import kg.alatoo.studentcouncil.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public Comment addComment(String text, User user, Offer offer) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(user);
        comment.setOffer(offer);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByOffer(Offer offer) {
        return commentRepository.findByOfferOrderByCreatedAtDesc(offer);
    }
}
