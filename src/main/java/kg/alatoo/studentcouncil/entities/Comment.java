package kg.alatoo.studentcouncil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, nullable = false)
    private String text;

    private LocalDateTime createdAt;

    @ManyToOne
    private User author;

    @ManyToOne
    private Offer offer;
}
