package kg.alatoo.studentcouncil.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
    private String author;
    private String text;
    private String formattedDate;

    public CommentDto(String author, String text, String formattedDate) {
        this.author = author;
        this.text = text;
        this.formattedDate = formattedDate;
    }

    // геттеры и сеттеры
}
