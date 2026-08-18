package com.travel.tour.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.tour.constant.OutboxStatus;
import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.dto.TourSearchCriteria;
import com.travel.tour.dto.UpdateTourRequest;
import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.entity.OutboxEventEntity;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.mapper.TourMapper;
import com.travel.tour.mapper.TourScheduleMapper;
import com.travel.tour.repository.DestinationRepository;
import com.travel.tour.repository.OutboxEventRepository;
import com.travel.tour.repository.TourRepository;
import com.travel.tour.repository.TourScheduleRepository;
import com.travel.tour.repository.specification.TourSpecification;
import com.travel.tour.service.TourService;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourScheduleVm;
import com.travel.tour.viewmodel.TourVm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final DestinationRepository destinationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TourMapper tourMapper;
    private final TourScheduleMapper tourScheduleMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TourDetailVm createTour(CreateTourRequest request) {
        log.info("Tạo mới Tour với mã: {}", request.code());

        if (tourRepository.existsByCode(request.code())) {
            throw BusinessException.of(ErrorCode.TOUR_CODE_EXISTS, "Mã Tour đã tồn tại trong hệ thống: " + request.code());
        }

        TourEntity tour = tourMapper.toEntity(request);

        // Gắn quan hệ 2 chiều giữa Tour và Itineraries
        if (tour.getItineraries() != null) {
            tour.getItineraries().forEach(itinerary -> itinerary.setTour(tour));
        }

        // Đính kèm danh sách địa điểm nếu có
        if (request.destinationIds() != null && !request.destinationIds().isEmpty()) {
            List<DestinationEntity> destinations = destinationRepository.findByIdIn(request.destinationIds());
            tour.setDestinations(new HashSet<>(destinations));
        }

        TourEntity savedTour = tourRepository.save(tour);

        // Ghi bản tin Outbox Event để báo cho ai-service cập nhật Vector Embeddings
        saveOutboxEvent("TourUpdatedEvent", savedTour.getId().toString(), tourMapper.toTourVm(savedTour));

        log.info("Tạo Tour thành công với ID: {}", savedTour.getId());
        return tourMapper.toTourDetailVm(savedTour);
    }

    @Override
    @Transactional(readOnly = true)
    public TourDetailVm getTourById(UUID id) {
        log.info("Tra cứu chi tiết Tour ID: {}", id);
        TourEntity tour = tourRepository.findByIdWithDetails(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy Tour với ID: " + id));
        return tourMapper.toTourDetailVm(tour);
    }

    @Override
    @Transactional
    public TourDetailVm updateTour(UUID id, UpdateTourRequest request) {
        log.info("Cập nhật thông tin Tour ID: {}", id);
        TourEntity tour = tourRepository.findByIdWithDetails(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy Tour với ID: " + id));

        tour.setTitle(request.title());
        tour.setDescription(request.description());
        if (request.status() != null) {
            tour.setStatus(request.status());
        }

        if (request.destinationIds() != null) {
            List<DestinationEntity> destinations = destinationRepository.findByIdIn(request.destinationIds());
            tour.setDestinations(new HashSet<>(destinations));
        }

        TourEntity updatedTour = tourRepository.save(tour);
        saveOutboxEvent("TourUpdatedEvent", updatedTour.getId().toString(), tourMapper.toTourVm(updatedTour));

        return tourMapper.toTourDetailVm(updatedTour);
    }

    @Override
    @Transactional
    public TourScheduleVm addSchedule(UUID tourId, CreateScheduleRequest request) {
        log.info("Thêm lịch khởi hành cho Tour ID: {}", tourId);
        TourEntity tour = tourRepository.findById(tourId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy Tour với ID: " + tourId));

        TourScheduleEntity schedule = tourScheduleMapper.toEntity(request);
        schedule.setTour(tour);

        TourScheduleEntity savedSchedule = tourScheduleRepository.save(schedule);
        saveOutboxEvent("TourUpdatedEvent", tour.getId().toString(), tourMapper.toTourVm(tour));

        return tourScheduleMapper.toVm(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourVm> searchTours(TourSearchCriteria criteria) {
        log.info("Tìm kiếm Tour với từ khóa: '{}', trang: {}, kích thước: {}", criteria.keyword(), criteria.page(), criteria.size());

        Specification<TourEntity> spec = TourSpecification.filterByCriteria(criteria);
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), Sort.by("createdAt").descending());

        Page<TourEntity> tourPage = tourRepository.findAll(spec, pageable);
        return tourPage.map(tourMapper::toTourVm);
    }

    private void saveOutboxEvent(String eventType, String aggregateId, Object payloadObj) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadObj);
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .aggregateType("Tour")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Lỗi đóng gói JSON cho Outbox Event: {}", e.getMessage(), e);
        }
    }
}
