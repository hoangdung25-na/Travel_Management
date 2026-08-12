package com.travel.tour.controller;

import com.travel.common.core.dto.ApiResponse;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.dto.CreateTourScheduleRequest;
import com.travel.tour.service.TourScheduleService;
import com.travel.tour.service.TourService;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourVm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tours")
public class TourController {

    private final TourService tourService;
    private final TourScheduleService tourScheduleService;

    public TourController(TourService tourService, TourScheduleService tourScheduleService) {
        this.tourService = tourService;
        this.tourScheduleService = tourScheduleService;
    }

    @PostMapping
    public ApiResponse<TourDetailVm> createTour(@Valid @RequestBody CreateTourRequest request) {
        TourDetailVm vm = tourService.createTour(request);
        return ApiResponse.ok(vm, "Khởi tạo Tour thành công");
    }

    @GetMapping
    public ApiResponse<Page<TourVm>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TourVm> result = tourService.searchTours(keyword, minPrice, maxPrice, pageable);
        return ApiResponse.ok(result, "Lấy danh sách Tour thành công");
    }

    @GetMapping("/{id}")
    public ApiResponse<TourDetailVm> getTourById(@PathVariable UUID id) {
        TourDetailVm vm = tourService.getTourById(id);
        return ApiResponse.ok(vm, "Lấy chi tiết Tour thành công");
    }

    @PostMapping("/{id}/schedules")
    public ApiResponse<TourDetailVm.TourScheduleVm> createSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTourScheduleRequest request) {
        TourDetailVm.TourScheduleVm vm = tourScheduleService.createSchedule(id, request);
        return ApiResponse.ok(vm, "Khởi tạo Lịch khởi hành thành công");
    }
}
