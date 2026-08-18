package com.travel.tour.mapper;

import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.viewmodel.TourScheduleVm;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T22:39:22+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TourScheduleMapperImpl implements TourScheduleMapper {

    @Override
    public TourScheduleVm toVm(TourScheduleEntity entity) {
        if ( entity == null ) {
            return null;
        }

        TourScheduleVm.TourScheduleVmBuilder tourScheduleVm = TourScheduleVm.builder();

        tourScheduleVm.arrivalTime( entity.getArrivalTime() );
        tourScheduleVm.availableSeats( entity.getAvailableSeats() );
        tourScheduleVm.departureTime( entity.getDepartureTime() );
        tourScheduleVm.id( entity.getId() );
        tourScheduleVm.priceAdult( entity.getPriceAdult() );
        tourScheduleVm.priceChild( entity.getPriceChild() );
        tourScheduleVm.totalSeats( entity.getTotalSeats() );

        return tourScheduleVm.build();
    }

    @Override
    public List<TourScheduleVm> toVmList(List<TourScheduleEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<TourScheduleVm> list = new ArrayList<TourScheduleVm>( entities.size() );
        for ( TourScheduleEntity tourScheduleEntity : entities ) {
            list.add( toVm( tourScheduleEntity ) );
        }

        return list;
    }

    @Override
    public TourScheduleEntity toEntity(CreateScheduleRequest request) {
        if ( request == null ) {
            return null;
        }

        TourScheduleEntity.TourScheduleEntityBuilder tourScheduleEntity = TourScheduleEntity.builder();

        tourScheduleEntity.availableSeats( request.totalSeats() );
        tourScheduleEntity.arrivalTime( request.arrivalTime() );
        tourScheduleEntity.departureTime( request.departureTime() );
        tourScheduleEntity.priceAdult( request.priceAdult() );
        tourScheduleEntity.priceChild( request.priceChild() );
        tourScheduleEntity.totalSeats( request.totalSeats() );

        return tourScheduleEntity.build();
    }
}
