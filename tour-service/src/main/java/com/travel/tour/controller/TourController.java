package com.travel.tour.controller;

import com.travel.common.core.dto.ApiResponse;
import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.dto.TourSearchCriteria;
import com.travel.tour.dto.UpdateTourRequest;
import com.travel.tour.service.TourService;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourScheduleVm;
import com.travel.tour.viewmodel.TourVm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
@Slf4j
public class TourController {

    private final TourService tourService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TourDetailVm> createTour(@Valid @RequestBody CreateTourRequest request) {
        log.info("REST request tạo mới Tour mã: {}", request.code());
        TourDetailVm tourDetailVm = tourService.createTour(request);
        return ApiResponse.ok(tourDetailVm, "Tạo mới Tour thành công");
    }

    @GetMapping
    public ApiResponse<Page<TourVm>> searchTours(TourSearchCriteria criteria) {
        log.info("REST request tìm kiếm Tour từ khóa: '{}'", criteria.keyword());
        Page<TourVm> page = tourService.searchTours(criteria);
        return ApiResponse.ok(page, "Lấy danh sách Tour thành công");
    }

    @GetMapping("/{id}")
    public ApiResponse<TourDetailVm> getTourById(@PathVariable UUID id) {
        log.info("REST request lấy chi tiết Tour ID: {}", id);
        TourDetailVm tourDetailVm = tourService.getTourById(id);
        return ApiResponse.ok(tourDetailVm, "Lấy chi tiết Tour thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<TourDetailVm> updateTour(@PathVariable UUID id, @Valid @RequestBody UpdateTourRequest request) {
        log.info("REST request cập nhật Tour ID: {}", id);
        TourDetailVm tourDetailVm = tourService.updateTour(id, request);
        return ApiResponse.ok(tourDetailVm, "Cập nhật Tour thành công");
    }

    @PostMapping("/{id}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TourScheduleVm> addSchedule(@PathVariable UUID id, @Valid @RequestBody CreateScheduleRequest request) {
        log.info("REST request thêm lịch khởi hành cho Tour ID: {}", id);
        TourScheduleVm scheduleVm = tourService.addSchedule(id, request);
        return ApiResponse.ok(scheduleVm, "Thêm lịch khởi hành thành công");
    }
}
