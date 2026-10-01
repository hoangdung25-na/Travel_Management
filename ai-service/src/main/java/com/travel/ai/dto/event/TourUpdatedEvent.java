package com.travel.ai.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.travel.ai.dto.ItineraryItemDto;
import com.travel.common.kafka.event.AbstractEventPayload;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Event nhận từ Kafka topic 'tour-events' khi tour-service tạo mới hoặc cập nhật Tour
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourUpdatedEvent extends AbstractEventPayload {

    private UUID tourId;
    private String code;
    private String title;
    private String description;
    private String destination;
    private BigDecimal minPrice;
    private Integer durationDays;
    private String suitableFor;
    private List<ItineraryItemDto> itineraries;
}
