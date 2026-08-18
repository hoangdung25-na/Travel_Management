package com.travel.tour.controller;

import com.travel.common.core.dto.ApiResponse;
import com.travel.tour.dto.CreateDestinationRequest;
import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.mapper.DestinationMapper;
import com.travel.tour.repository.DestinationRepository;
import com.travel.tour.viewmodel.DestinationVm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
@Slf4j
public class DestinationController {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;

    @GetMapping
    public ApiResponse<List<DestinationVm>> getAllDestinations() {
        log.info("Lấy danh sách điểm đến du lịch");
        List<DestinationEntity> destinations = destinationRepository.findAll();
        List<DestinationVm> destinationVms = destinations.stream()
                .map(destinationMapper::toVm)
                .toList();
        return ApiResponse.ok(destinationVms, "Lấy danh sách điểm đến thành công");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DestinationVm> createDestination(@Valid @RequestBody CreateDestinationRequest request) {
        log.info("Tạo mới điểm đến: {}, {}", request.name(), request.city());
        DestinationEntity destination = DestinationEntity.builder()
                .name(request.name())
                .city(request.city())
                .country(request.country())
                .build();
        DestinationEntity saved = destinationRepository.save(destination);
        return ApiResponse.ok(destinationMapper.toVm(saved), "Tạo mới điểm đến thành công");
    }
}
