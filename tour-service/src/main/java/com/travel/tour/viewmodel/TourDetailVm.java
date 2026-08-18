package com.travel.tour.viewmodel;

import com.travel.tour.constant.TourStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record TourDetailVm(
        UUID tourId,
        String code,
        String title,
        String description,
        TourStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ItineraryVm> itineraries,
        List<TourScheduleVm> schedules,
        Set<DestinationVm> destinations
) {
}
