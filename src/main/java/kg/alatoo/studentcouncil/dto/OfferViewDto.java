package kg.alatoo.studentcouncil.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfferViewDto {
    private Long id;
    private String title;
    private String author;
    private String createdAt;
    private String imageUrl;
    private String description;
    private int commentCount;
    private int likes;
    private int dislikes;
}
