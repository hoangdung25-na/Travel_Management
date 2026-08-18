package com.travel.tour.mapper;

import com.travel.tour.dto.CreateItineraryRequest;
import com.travel.tour.entity.ItineraryEntity;
import com.travel.tour.viewmodel.ItineraryVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItineraryMapper {

    ItineraryVm toVm(ItineraryEntity entity);

    List<ItineraryVm> toVmList(List<ItineraryEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tour", ignore = true)
    ItineraryEntity toEntity(CreateItineraryRequest request);

    List<ItineraryEntity> toEntityList(List<CreateItineraryRequest> requests);
}
