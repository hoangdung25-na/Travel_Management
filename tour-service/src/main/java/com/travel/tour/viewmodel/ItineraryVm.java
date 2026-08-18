package com.travel.tour.viewmodel;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ItineraryVm(
        UUID id,
        Integer dayNumber,
        String title,
        String content
) {
}
