package kg.alatoo.studentcouncil.services;

import kg.alatoo.studentcouncil.entities.Comment;
import kg.alatoo.studentcouncil.entities.Offer;
import kg.alatoo.studentcouncil.entities.User;

import java.util.List;

public interface CommentService {
    Comment addComment(String text, User user, Offer offer);
    List<Comment> getCommentsByOffer(Offer offer);

    int countCommentsByOffer(Offer offer);
}
