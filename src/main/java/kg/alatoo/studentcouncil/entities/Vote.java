package kg.alatoo.studentcouncil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "offer_id"}))
@Getter
@Setter
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Offer offer;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    public enum VoteType {
        LIKE,
        DISLIKE
    }
}

