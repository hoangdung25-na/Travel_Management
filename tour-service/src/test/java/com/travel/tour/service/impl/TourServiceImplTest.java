package com.travel.tour.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.tour.constant.TourStatus;
import com.travel.tour.dto.CreateScheduleRequest;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.entity.OutboxEventEntity;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.mapper.TourMapper;
import com.travel.tour.mapper.TourScheduleMapper;
import com.travel.tour.repository.DestinationRepository;
import com.travel.tour.repository.OutboxEventRepository;
import com.travel.tour.repository.TourRepository;
import com.travel.tour.repository.TourScheduleRepository;
import com.travel.tour.viewmodel.TourDetailVm;
import com.travel.tour.viewmodel.TourScheduleVm;
import com.travel.tour.viewmodel.TourVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private TourMapper tourMapper;
    @Mock
    private TourScheduleMapper tourScheduleMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    private UUID tourId;
    private TourEntity tourEntity;
    private TourDetailVm tourDetailVm;
    private TourVm tourVm;

    @BeforeEach
    void setUp() {
        tourId = UUID.randomUUID();

        tourEntity = TourEntity.builder()
                .id(tourId)
                .code("TOUR-SAPA-01")
                .title("Tour Du Lịch Sapa")
                .description("Hành trình khám phá Tây Bắc")
                .status(TourStatus.DRAFT)
                .build();

        tourDetailVm = TourDetailVm.builder()
                .tourId(tourId)
                .code("TOUR-SAPA-01")
                .title("Tour Du Lịch Sapa")
                .status(TourStatus.DRAFT)
                .build();

        tourVm = TourVm.builder()
                .tourId(tourId)
                .code("TOUR-SAPA-01")
                .title("Tour Du Lịch Sapa")
                .build();
    }

    @Test
    @DisplayName("createTour - Tạo Tour thành công và tự động lưu bản tin Outbox Event")
    void createTour_Success() throws Exception {
        // Given
        CreateTourRequest request = CreateTourRequest.builder()
                .code("TOUR-SAPA-01")
                .title("Tour Du Lịch Sapa")
                .description("Hành trình khám phá Tây Bắc")
                .itineraries(Collections.emptyList())
                .destinationIds(Collections.emptySet())
                .build();

        when(tourRepository.existsByCode("TOUR-SAPA-01")).thenReturn(false);
        when(tourMapper.toEntity(request)).thenReturn(tourEntity);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(tourEntity);
        when(tourMapper.toTourVm(tourEntity)).thenReturn(tourVm);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(tourMapper.toTourDetailVm(tourEntity)).thenReturn(tourDetailVm);

        // When
        TourDetailVm result = tourService.createTour(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("TOUR-SAPA-01");
        verify(tourRepository, times(1)).existsByCode("TOUR-SAPA-01");
        verify(tourRepository, times(1)).save(any(TourEntity.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEventEntity.class));
    }

    @Test
    @DisplayName("createTour - Thất bại khi mã Tour bị trùng lặp (Quăng TOUR_CODE_EXISTS)")
    void createTour_DuplicateCode_ThrowsException() {
        // Given
        CreateTourRequest request = CreateTourRequest.builder()
                .code("TOUR-SAPA-01")
                .title("Tour Du Lịch Sapa")
                .build();

        when(tourRepository.existsByCode("TOUR-SAPA-01")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tourService.createTour(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Mã Tour đã tồn tại trong hệ thống")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOUR_CODE_EXISTS);

        verify(tourRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTourById - Trả về chi tiết Tour thành công khi ID hợp lệ")
    void getTourById_Success() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tourEntity));
        when(tourMapper.toTourDetailVm(tourEntity)).thenReturn(tourDetailVm);

        // When
        TourDetailVm result = tourService.getTourById(tourId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tourId()).isEqualTo(tourId);
        verify(tourRepository, times(1)).findById(tourId);
    }

    @Test
    @DisplayName("getTourById - Thất bại khi không tìm thấy Tour (Quăng TOUR_NOT_FOUND)")
    void getTourById_NotFound_ThrowsException() {
        // Given
        when(tourRepository.findById(tourId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tourService.getTourById(tourId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy Tour với ID")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOUR_NOT_FOUND);
    }

    @Test
    @DisplayName("addSchedule - Thêm lịch khởi hành cho Tour thành công")
    void addSchedule_Success() throws Exception {
        // Given
        CreateScheduleRequest scheduleReq = CreateScheduleRequest.builder()
                .departureTime(OffsetDateTime.now().plusDays(10))
                .arrivalTime(OffsetDateTime.now().plusDays(15))
                .priceAdult(BigDecimal.valueOf(5000000))
                .priceChild(BigDecimal.valueOf(2500000))
                .totalSeats(20)
                .build();

        TourScheduleEntity scheduleEntity = TourScheduleEntity.builder()
                .id(UUID.randomUUID())
                .tour(tourEntity)
                .totalSeats(20)
                .availableSeats(20)
                .build();

        TourScheduleVm scheduleVm = TourScheduleVm.builder()
                .id(scheduleEntity.getId())
                .totalSeats(20)
                .availableSeats(20)
                .build();

        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tourEntity));
        when(tourScheduleMapper.toEntity(scheduleReq)).thenReturn(scheduleEntity);
        when(tourScheduleRepository.save(any(TourScheduleEntity.class))).thenReturn(scheduleEntity);
        when(tourScheduleMapper.toVm(scheduleEntity)).thenReturn(scheduleVm);
        when(tourMapper.toTourVm(tourEntity)).thenReturn(tourVm);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        TourScheduleVm result = tourService.addSchedule(tourId, scheduleReq);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalSeats()).isEqualTo(20);
        verify(tourScheduleRepository, times(1)).save(any(TourScheduleEntity.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEventEntity.class));
    }
}
