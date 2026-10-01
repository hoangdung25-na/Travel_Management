package com.travel.ai.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ViewModel đại diện cho thông tin một Tour được AI đề xuất
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedTourVm {

    @JsonProperty("tour_id")
    private UUID tourId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("booking_url")
    private String bookingUrl;

    @JsonProperty("reason")
    private String reason;
}
