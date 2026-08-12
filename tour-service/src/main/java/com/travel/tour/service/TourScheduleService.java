package com.travel.tour.service;

import com.travel.tour.dto.CreateTourScheduleRequest;
import com.travel.tour.viewmodel.TourDetailVm;

import java.util.UUID;

public interface TourScheduleService {
    TourDetailVm.TourScheduleVm createSchedule(UUID tourId, CreateTourScheduleRequest request);
}
