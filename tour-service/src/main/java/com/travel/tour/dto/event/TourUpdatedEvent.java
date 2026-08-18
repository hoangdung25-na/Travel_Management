package com.travel.tour.dto.event;

import com.travel.common.kafka.event.AbstractEventPayload;
import com.travel.tour.constant.TourStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourUpdatedEvent extends AbstractEventPayload {

    private UUID tourId;
    private String code;
    private String title;
    private String description;
    private BigDecimal minPrice;
    private Integer totalAvailableSeats;
    private TourStatus status;
}
