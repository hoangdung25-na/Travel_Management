package com.travel.tour.service;

import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface TourService {
    TourDetailVm createTour(CreateTourRequest request);
    TourDetailVm getTourById(UUID tourId);
    Page<TourVm> searchTours(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
