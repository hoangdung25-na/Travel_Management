package com.travel.tour.viewmodel;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DestinationVm(
        UUID id,
        String name,
        String city,
        String country
) {
}
