package com.travel.tour.mapper;

import com.travel.tour.constant.TourStatus;
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

@Mapper(
        componentModel = "spring",
        imports = {TourStatus.class},
        uses = {DestinationMapper.class, ItineraryMapper.class, TourScheduleMapper.class, TourImageMapper.class}
)
public interface TourMapper {

    @Mapping(target = "tourId", source = "id")
    @Mapping(target = "minPrice", source = "schedules", qualifiedByName = "calculateMinPrice")
    @Mapping(target = "availableSeats", source = "schedules", qualifiedByName = "calculateTotalAvailableSeats")
    @Mapping(target = "imageUrl", source = "tour", qualifiedByName = "resolveImageUrl")
    @Mapping(target = "destinationCity", source = "tour", qualifiedByName = "resolveDestinationCity")
    @Mapping(target = "duration", source = "duration")
    TourVm toTourVm(TourEntity tour);

    @Mapping(target = "tourId", source = "id")
    @Mapping(target = "imageUrl", source = "tour", qualifiedByName = "resolveImageUrl")
    @Mapping(target = "duration", source = "duration")
    TourDetailVm toTourDetailVm(TourEntity tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "status", source = "status", defaultExpression = "java(TourStatus.PUBLISHED)")
    TourEntity toEntity(CreateTourRequest request);

    @Named("resolveImageUrl")
    default String resolveImageUrl(TourEntity tour) {
        if (tour == null) {
            return null;
        }
        if (tour.getImageUrl() != null && !tour.getImageUrl().trim().isEmpty()) {
            return tour.getImageUrl();
        }
        if (tour.getImages() != null && !tour.getImages().isEmpty()) {
            // First check if an image is explicitly marked primary
            for (var img : tour.getImages()) {
                if (img != null && Boolean.TRUE.equals(img.getIsPrimary()) && img.getImageUrl() != null && !img.getImageUrl().trim().isEmpty()) {
                    return img.getImageUrl();
                }
            }
            // Otherwise use the first available image
            for (var img : tour.getImages()) {
                if (img != null && img.getImageUrl() != null && !img.getImageUrl().trim().isEmpty()) {
                    return img.getImageUrl();
                }
            }
        }
        if (tour.getDestinations() != null && !tour.getDestinations().isEmpty()) {
            for (var dest : tour.getDestinations()) {
                if (dest != null && dest.getImageUrl() != null && !dest.getImageUrl().trim().isEmpty()) {
                    return dest.getImageUrl();
                }
            }
        }
        return null;
    }

    @Named("resolveDestinationCity")
    default String resolveDestinationCity(TourEntity tour) {
        if (tour == null || tour.getDestinations() == null || tour.getDestinations().isEmpty()) {
            return "Việt Nam";
        }
        for (var dest : tour.getDestinations()) {
            if (dest != null && dest.getCity() != null && !dest.getCity().trim().isEmpty()) {
                return dest.getCity();
            }
        }
        return "Việt Nam";
    }

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


