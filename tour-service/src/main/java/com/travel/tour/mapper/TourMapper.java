package com.travel.tour.mapper;

import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.entity.Itinerary;
import com.travel.tour.entity.Tour;
import com.travel.tour.entity.TourSchedule;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TourMapper {

    @Mapping(source = "id", target = "tourId")
    TourVm toTourVm(Tour tour);

    @Mapping(source = "id", target = "tourId")
    TourDetailVm toTourDetailVm(Tour tour);

    TourDetailVm.ItineraryVm toItineraryVm(Itinerary itinerary);

    @Mapping(source = "id", target = "scheduleId")
    TourDetailVm.TourScheduleVm toTourScheduleVm(TourSchedule schedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    Tour toTour(CreateTourRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tour", ignore = true)
    Itinerary toItinerary(CreateTourRequest.ItineraryDto dto);
    
    List<Itinerary> toItineraryList(List<CreateTourRequest.ItineraryDto> dtos);
}
