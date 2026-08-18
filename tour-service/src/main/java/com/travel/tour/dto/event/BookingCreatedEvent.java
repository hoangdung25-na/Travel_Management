package com.travel.tour.dto.event;

import com.travel.common.kafka.event.AbstractEventPayload;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent extends AbstractEventPayload {

    private UUID bookingId;
    private UUID scheduleId;
    private Integer requestedSeats;
    private String customerEmail;
    private BigDecimal totalPrice;
}
