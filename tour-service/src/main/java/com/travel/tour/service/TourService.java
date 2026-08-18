package com.travel.tour.service;

import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.dto.TourSearchCriteria;
import com.travel.tour.dto.UpdateTourRequest;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourScheduleVm;
import com.travel.tour.viewmodel.TourVm;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TourService {

    TourDetailVm createTour(CreateTourRequest request);

    TourDetailVm getTourById(UUID id);

    TourDetailVm updateTour(UUID id, UpdateTourRequest request);

    TourScheduleVm addSchedule(UUID tourId, CreateScheduleRequest request);

    Page<TourVm> searchTours(TourSearchCriteria criteria);
}
