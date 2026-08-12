package com.travel.tour.service.impl;

import com.travel.tour.dto.CreateTourScheduleRequest;
import com.travel.tour.entity.Tour;
import com.travel.tour.entity.TourSchedule;
import com.travel.tour.exception.NotFoundException;
import com.travel.tour.exception.TourException;
import com.travel.tour.mapper.TourMapper;
import com.travel.tour.repository.TourRepository;
import com.travel.tour.repository.TourScheduleRepository;
import com.travel.tour.service.TourScheduleService;
import com.travel.tour.viewmodel.TourDetailVm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TourScheduleServiceImpl implements TourScheduleService {

    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourMapper tourMapper;

    public TourScheduleServiceImpl(TourRepository tourRepository, TourScheduleRepository tourScheduleRepository, TourMapper tourMapper) {
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourMapper = tourMapper;
    }

    @Override
    @Transactional
    public TourDetailVm.TourScheduleVm createSchedule(UUID tourId, CreateTourScheduleRequest request) {
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new TourException("Arrival time must be after departure time");
        }

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found: " + tourId));

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);
        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setPriceAdult(request.getPriceAdult());
        schedule.setPriceChild(request.getPriceChild());
        schedule.setTotalSeats(request.getTotalSeats());
        schedule.setAvailableSeats(request.getTotalSeats()); // Khởi tạo bằng totalSeats

        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);
        return tourMapper.toTourScheduleVm(savedSchedule);
    }
}
