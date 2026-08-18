package com.travel.booking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.client.TourServiceClient;
import com.travel.booking.client.dto.TourScheduleResponse;
import com.travel.booking.constant.BookingStatus;
import com.travel.booking.constant.PassengerType;
import com.travel.booking.dto.CancelBookingRequest;
import com.travel.booking.dto.CreateBookingRequest;
import com.travel.booking.dto.PassengerRequest;
import com.travel.booking.entity.Booking;
import com.travel.booking.entity.OutboxEventEntity;
import com.travel.booking.helper.BookingCodeGenerator;
import com.travel.booking.mapper.BookingMapper;
import com.travel.booking.repository.BookingRepository;
import com.travel.booking.repository.OutboxEventRepository;
import com.travel.booking.service.BookingSagaStateDomainService;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingVm;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private BookingCodeGenerator bookingCodeGenerator;

    @Mock
    private TourServiceClient tourServiceClient;

    @Mock
    private BookingSagaStateDomainService bookingSagaStateDomainService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID userId;
    private UUID scheduleId;
    private UUID bookingId;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        booking = Booking.builder()
                .id(bookingId)
                .bookingCode("BK20260819-A1B2")
                .userId(userId)
                .tourScheduleId(scheduleId)
                .totalAmount(new BigDecimal("8750000.00"))
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Tạo đơn đặt tour thành công và ghi Outbox Event nguyên tố")
    void createBooking_Success() throws Exception {
        // Arrange
        PassengerRequest adultPassenger = new PassengerRequest("Nguyễn Văn A", LocalDate.of(1990, 1, 1), PassengerType.ADULT, "0912345678");
        PassengerRequest childPassenger = new PassengerRequest("Nguyễn Văn B", LocalDate.of(2015, 5, 5), PassengerType.CHILD, null);
        CreateBookingRequest request = new CreateBookingRequest(scheduleId, List.of(adultPassenger, childPassenger));

        TourScheduleResponse scheduleResponse = new TourScheduleResponse(
                scheduleId,
                new BigDecimal("5000000.00"),
                new BigDecimal("3750000.00"),
                20
        );

        BookingVm bookingVm = new BookingVm(
                bookingId,
                "BK20260819-A1B2",
                userId,
                scheduleId,
                new BigDecimal("8750000.00"),
                BookingStatus.PENDING,
                OffsetDateTime.now()
        );

        when(tourServiceClient.getScheduleById(scheduleId)).thenReturn(scheduleResponse);
        when(bookingCodeGenerator.generateCode()).thenReturn("BK20260819-A1B2");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(outboxEventRepository.save(any(OutboxEventEntity.class))).thenReturn(new OutboxEventEntity());
        when(bookingMapper.toBookingVm(any(Booking.class))).thenReturn(bookingVm);

        // Act
        BookingVm result = bookingService.createBooking(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("BK20260819-A1B2", result.bookingCode());
        assertEquals(new BigDecimal("8750000.00"), result.totalAmount());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEventEntity.class));
    }

    @Test
    @DisplayName("Lấy chi tiết đơn hàng thành công khi người dùng là chủ sở hữu đơn hàng")
    void getBookingDetail_OwnerAccess_Success() {
        // Arrange
        BookingDetailVm detailVm = new BookingDetailVm(
                bookingId,
                "BK20260819-A1B2",
                userId,
                scheduleId,
                new BigDecimal("8750000.00"),
                BookingStatus.PENDING,
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(bookingRepository.findByIdWithPassengers(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDetailVm(booking)).thenReturn(detailVm);

        // Act
        BookingDetailVm result = bookingService.getBookingDetail(userId, bookingId);

        // Assert
        assertNotNull(result);
        assertEquals(bookingId, result.bookingId());
        assertEquals(userId, result.userId());
    }

    @Test
    @DisplayName("Ném ngoại lệ FORBIDDEN khi xem chi tiết đơn hàng không thuộc về mình")
    void getBookingDetail_NotOwner_ThrowsForbiddenException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(bookingRepository.findByIdWithPassengers(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                bookingService.getBookingDetail(otherUserId, bookingId)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("Hủy đơn hàng thành công và ghi Outbox event BookingCancelledEvent")
    void cancelBooking_OwnerAccess_Success() throws Exception {
        // Arrange
        CancelBookingRequest request = new CancelBookingRequest("Đổi lịch trình cá nhân");
        BookingVm cancelledVm = new BookingVm(
                bookingId,
                "BK20260819-A1B2",
                userId,
                scheduleId,
                new BigDecimal("8750000.00"),
                BookingStatus.CANCELLED,
                OffsetDateTime.now()
        );

        when(bookingRepository.findByIdWithPassengers(bookingId)).thenReturn(Optional.of(booking));
        doNothing().when(bookingSagaStateDomainService).validateAndTransitionToCancelled(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(outboxEventRepository.save(any(OutboxEventEntity.class))).thenReturn(new OutboxEventEntity());
        when(bookingMapper.toBookingVm(booking)).thenReturn(cancelledVm);

        // Act
        BookingVm result = bookingService.cancelBooking(userId, bookingId, request);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.status());
        verify(bookingSagaStateDomainService, times(1)).validateAndTransitionToCancelled(booking);
        verify(outboxEventRepository, times(1)).save(any(OutboxEventEntity.class));
    }
}
