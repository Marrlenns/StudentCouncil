package kg.alatoo.studentcouncil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Service
@Table(name = "offer_table")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private String imageUrl;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;


    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int dislikes = 0;
//
//
//    @OneToMany
//    private List<Review> reviews;

}
