package com.travel.tour.mapper;

import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = { DestinationMapper.class, ItineraryMapper.class, TourScheduleMapper.class })
public interface TourMapper {

    @Mapping(target = "tourId", source = "id")
    @Mapping(target = "minPrice", source = "schedules", qualifiedByName = "calculateMinPrice")
    @Mapping(target = "availableSeats", source = "schedules", qualifiedByName = "calculateTotalAvailableSeats")
    TourVm toTourVm(TourEntity tour);

    @Mapping(target = "tourId", source = "id")
    TourDetailVm toTourDetailVm(TourEntity tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    TourEntity toEntity(CreateTourRequest request);

    @Named("calculateMinPrice")
    default BigDecimal calculateMinPrice(List<TourScheduleEntity> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return schedules.stream()
                .map(TourScheduleEntity::getPriceAdult)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Named("calculateTotalAvailableSeats")
    default Integer calculateTotalAvailableSeats(List<TourScheduleEntity> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }
        return schedules.stream()
                .mapToInt(s -> s.getAvailableSeats() != null ? s.getAvailableSeats() : 0)
                .sum();
    }
}
