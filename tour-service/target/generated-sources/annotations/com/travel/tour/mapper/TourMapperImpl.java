package com.travel.tour.mapper;

import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T22:39:21+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TourMapperImpl implements TourMapper {

    @Autowired
    private DestinationMapper destinationMapper;
    @Autowired
    private ItineraryMapper itineraryMapper;
    @Autowired
    private TourScheduleMapper tourScheduleMapper;

    @Override
    public TourVm toTourVm(TourEntity tour) {
        if ( tour == null ) {
            return null;
        }

        TourVm.TourVmBuilder tourVm = TourVm.builder();

        tourVm.tourId( tour.getId() );
        tourVm.minPrice( calculateMinPrice( tour.getSchedules() ) );
        tourVm.availableSeats( calculateTotalAvailableSeats( tour.getSchedules() ) );
        tourVm.code( tour.getCode() );
        tourVm.createdAt( tour.getCreatedAt() );
        tourVm.status( tour.getStatus() );
        tourVm.title( tour.getTitle() );

        return tourVm.build();
    }

    @Override
    public TourDetailVm toTourDetailVm(TourEntity tour) {
        if ( tour == null ) {
            return null;
        }

        TourDetailVm.TourDetailVmBuilder tourDetailVm = TourDetailVm.builder();

        tourDetailVm.tourId( tour.getId() );
        tourDetailVm.code( tour.getCode() );
        tourDetailVm.createdAt( tour.getCreatedAt() );
        tourDetailVm.description( tour.getDescription() );
        tourDetailVm.destinations( destinationMapper.toVmSet( tour.getDestinations() ) );
        tourDetailVm.itineraries( itineraryMapper.toVmList( tour.getItineraries() ) );
        tourDetailVm.schedules( tourScheduleMapper.toVmList( tour.getSchedules() ) );
        tourDetailVm.status( tour.getStatus() );
        tourDetailVm.title( tour.getTitle() );
        tourDetailVm.updatedAt( tour.getUpdatedAt() );

        return tourDetailVm.build();
    }

    @Override
    public TourEntity toEntity(CreateTourRequest request) {
        if ( request == null ) {
            return null;
        }

        TourEntity.TourEntityBuilder tourEntity = TourEntity.builder();

        tourEntity.code( request.code() );
        tourEntity.description( request.description() );
        tourEntity.itineraries( itineraryMapper.toEntityList( request.itineraries() ) );
        tourEntity.title( request.title() );

        return tourEntity.build();
    }
}
