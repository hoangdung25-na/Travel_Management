package com.travel.tour.mapper;

import com.travel.tour.dto.TourImageRequest;
import com.travel.tour.entity.TourImageEntity;
import com.travel.tour.viewmodel.TourImageVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourImageMapper {

    TourImageVm toVm(TourImageEntity entity);

    List<TourImageVm> toVmList(List<TourImageEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TourImageEntity toEntity(TourImageRequest request);

    List<TourImageEntity> toEntityList(List<TourImageRequest> requests);
}
