package com.travel.tour.service.impl;

import com.travel.tour.constant.TourStatus;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.entity.Itinerary;
import com.travel.tour.entity.Tour;
import com.travel.tour.entity.TourSchedule;
import com.travel.tour.exception.NotFoundException;
import com.travel.tour.exception.TourException;
import com.travel.tour.mapper.TourMapper;
import com.travel.tour.repository.TourRepository;
import com.travel.tour.service.TourService;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    public TourServiceImpl(TourRepository tourRepository, TourMapper tourMapper) {
        this.tourRepository = tourRepository;
        this.tourMapper = tourMapper;
    }

    @Override
    @Transactional
    public TourDetailVm createTour(CreateTourRequest request) {
        if (tourRepository.existsByCode(request.getCode())) {
            throw new TourException("Tour code already exists: " + request.getCode());
        }

        Tour tour = tourMapper.toTour(request);
        tour.setStatus(TourStatus.DRAFT);
        
        List<Itinerary> itineraries = tourMapper.toItineraryList(request.getItineraries());
        if (itineraries != null) {
            for (Itinerary itinerary : itineraries) {
                itinerary.setTour(tour);
            }
            tour.getItineraries().addAll(itineraries);
        }

        Tour savedTour = tourRepository.save(tour);
        return tourMapper.toTourDetailVm(savedTour);
    }

    @Override
    @Transactional(readOnly = true)
    public TourDetailVm getTourById(UUID tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + tourId));
        return tourMapper.toTourDetailVm(tour);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourVm> searchTours(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Tour> tours = tourRepository.searchAvailableTours(TourStatus.PUBLISHED, keyword, minPrice, maxPrice, pageable);
        
        return tours.map(tour -> {
            TourVm vm = tourMapper.toTourVm(tour);
            // find min price and total available seats across schedules
            BigDecimal minAdultPrice = null;
            int totalAvailable = 0;
            
            for (TourSchedule schedule : tour.getSchedules()) {
                if (schedule.getAvailableSeats() > 0) {
                    if (minAdultPrice == null || schedule.getPriceAdult().compareTo(minAdultPrice) < 0) {
                        minAdultPrice = schedule.getPriceAdult();
                    }
                    totalAvailable += schedule.getAvailableSeats();
                }
            }
            vm.setMinPrice(minAdultPrice);
            vm.setAvailableSeats(totalAvailable);
            return vm;
        });
    }
}
