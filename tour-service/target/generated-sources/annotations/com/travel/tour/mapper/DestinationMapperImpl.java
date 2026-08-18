package com.travel.tour.mapper;

import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.viewmodel.DestinationVm;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T22:39:22+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class DestinationMapperImpl implements DestinationMapper {

    @Override
    public DestinationVm toVm(DestinationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        DestinationVm.DestinationVmBuilder destinationVm = DestinationVm.builder();

        destinationVm.city( entity.getCity() );
        destinationVm.country( entity.getCountry() );
        destinationVm.id( entity.getId() );
        destinationVm.name( entity.getName() );

        return destinationVm.build();
    }

    @Override
    public Set<DestinationVm> toVmSet(Set<DestinationEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        Set<DestinationVm> set = new LinkedHashSet<DestinationVm>( Math.max( (int) ( entities.size() / .75f ) + 1, 16 ) );
        for ( DestinationEntity destinationEntity : entities ) {
            set.add( toVm( destinationEntity ) );
        }

        return set;
    }
}
