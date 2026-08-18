package com.travel.tour.mapper;

import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.viewmodel.DestinationVm;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface DestinationMapper {

    DestinationVm toVm(DestinationEntity entity);

    Set<DestinationVm> toVmSet(Set<DestinationEntity> entities);
}
