package com.travel.tour.service.impl;

import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.tour.constant.TourStatus;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.repository.TourScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatAllocationDomainServiceImplTest {

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @InjectMocks
    private SeatAllocationDomainServiceImpl seatAllocationDomainService;

    private UUID scheduleId;
    private TourScheduleEntity scheduleEntity;
    private TourEntity tourEntity;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();

        tourEntity = TourEntity.builder()
                .id(UUID.randomUUID())
                .title("Tour Du Lịch Phú Quốc")
                .status(TourStatus.PUBLISHED)
                .build();

        scheduleEntity = TourScheduleEntity.builder()
                .id(scheduleId)
                .tour(tourEntity)
                .totalSeats(30)
                .availableSeats(20)
                .build();
    }

    @Test
    @DisplayName("reserveSeats - Giữ chỗ thành công khi Tour đang PUBLISHED và đủ số chỗ trống")
    void reserveSeats_Success() {
        // Given
        when(tourScheduleRepository.findByIdWithLock(scheduleId)).thenReturn(Optional.of(scheduleEntity));
        when(tourScheduleRepository.save(any(TourScheduleEntity.class))).thenReturn(scheduleEntity);

        // When
        boolean result = seatAllocationDomainService.reserveSeats(scheduleId, 5);

        // Then
        assertThat(result).isTrue();
        assertThat(scheduleEntity.getAvailableSeats()).isEqualTo(15);
        verify(tourScheduleRepository, times(1)).findByIdWithLock(scheduleId);
        verify(tourScheduleRepository, times(1)).save(scheduleEntity);
    }

    @Test
    @DisplayName("reserveSeats - Thất bại khi không tìm thấy Lịch trình (Quăng TOUR_SCHEDULE_NOT_FOUND)")
    void reserveSeats_ScheduleNotFound_ThrowsException() {
        // Given
        when(tourScheduleRepository.findByIdWithLock(scheduleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatAllocationDomainService.reserveSeats(scheduleId, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy lịch khởi hành")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOUR_SCHEDULE_NOT_FOUND);

        verify(tourScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserveSeats - Thất bại khi Tour ở trạng thái DRAFT (Quăng TOUR_NOT_PUBLISHED)")
    void reserveSeats_TourNotPublished_ThrowsException() {
        // Given
        tourEntity.setStatus(TourStatus.DRAFT);
        when(tourScheduleRepository.findByIdWithLock(scheduleId)).thenReturn(Optional.of(scheduleEntity));

        // When & Then
        assertThatThrownBy(() -> seatAllocationDomainService.reserveSeats(scheduleId, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hiện không mở bán")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOUR_NOT_PUBLISHED);

        verify(tourScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserveSeats - Thất bại khi không đủ số chỗ trống (Quăng TOUR_SEATS_INSUFFICIENT)")
    void reserveSeats_InsufficientSeats_ThrowsException() {
        // Given
        when(tourScheduleRepository.findByIdWithLock(scheduleId)).thenReturn(Optional.of(scheduleEntity));

        // When & Then (Available = 20, Request = 25)
        assertThatThrownBy(() -> seatAllocationDomainService.reserveSeats(scheduleId, 25))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không đủ chỗ trống")
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOUR_SEATS_INSUFFICIENT);

        verify(tourScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("releaseSeats - Hoàn chỗ thành công và không vượt quá tổng số chỗ (totalSeats)")
    void releaseSeats_Success() {
        // Given
        when(tourScheduleRepository.findByIdWithLock(scheduleId)).thenReturn(Optional.of(scheduleEntity));
        when(tourScheduleRepository.save(any(TourScheduleEntity.class))).thenReturn(scheduleEntity);

        // When (Available = 20, release = 5 -> new Available = 25)
        seatAllocationDomainService.releaseSeats(scheduleId, 5);

        // Then
        assertThat(scheduleEntity.getAvailableSeats()).isEqualTo(25);
        verify(tourScheduleRepository, times(1)).save(scheduleEntity);
    }
}
