package com.travel.tour.mapper;

import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.viewmodel.TourScheduleVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourScheduleMapper {

    TourScheduleVm toVm(TourScheduleEntity entity);

    List<TourScheduleVm> toVmList(List<TourScheduleEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "availableSeats", source = "totalSeats")
    @Mapping(target = "version", ignore = true)
    TourScheduleEntity toEntity(CreateScheduleRequest request);
}
