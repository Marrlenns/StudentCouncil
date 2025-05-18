package kg.alatoo.studentcouncil.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.stereotype.Service;


@Entity
@Getter
@Service
@Table(name = "offer_table")
public class Offer {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String description;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private String imageUrl;

//    @OneToMany
//    private List<User> likes;
//
//    @OneToMany
//    private List<User> dislikes;
//
//
//    @OneToMany
//    private List<Review> reviews;

}
