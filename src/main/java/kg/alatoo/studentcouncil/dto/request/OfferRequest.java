package kg.alatoo.studentcouncil.dto.request;

import java.time.LocalDateTime;

public record OfferRequest(
        String title,
        String description,
        String imageUrl) {
}
