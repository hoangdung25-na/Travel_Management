package com.travel.tour.mapper;

import com.travel.tour.dto.CreateItineraryRequest;
import com.travel.tour.entity.ItineraryEntity;
import com.travel.tour.viewmodel.ItineraryVm;
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
public class ItineraryMapperImpl implements ItineraryMapper {

    @Override
    public ItineraryVm toVm(ItineraryEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ItineraryVm.ItineraryVmBuilder itineraryVm = ItineraryVm.builder();

        itineraryVm.content( entity.getContent() );
        itineraryVm.dayNumber( entity.getDayNumber() );
        itineraryVm.id( entity.getId() );
        itineraryVm.title( entity.getTitle() );

        return itineraryVm.build();
    }

    @Override
    public List<ItineraryVm> toVmList(List<ItineraryEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ItineraryVm> list = new ArrayList<ItineraryVm>( entities.size() );
        for ( ItineraryEntity itineraryEntity : entities ) {
            list.add( toVm( itineraryEntity ) );
        }

        return list;
    }

    @Override
    public ItineraryEntity toEntity(CreateItineraryRequest request) {
        if ( request == null ) {
            return null;
        }

        ItineraryEntity.ItineraryEntityBuilder itineraryEntity = ItineraryEntity.builder();

        itineraryEntity.content( request.content() );
        itineraryEntity.dayNumber( request.dayNumber() );
        itineraryEntity.title( request.title() );

        return itineraryEntity.build();
    }

    @Override
    public List<ItineraryEntity> toEntityList(List<CreateItineraryRequest> requests) {
        if ( requests == null ) {
            return null;
        }

        List<ItineraryEntity> list = new ArrayList<ItineraryEntity>( requests.size() );
        for ( CreateItineraryRequest createItineraryRequest : requests ) {
            list.add( toEntity( createItineraryRequest ) );
        }

        return list;
    }
}
