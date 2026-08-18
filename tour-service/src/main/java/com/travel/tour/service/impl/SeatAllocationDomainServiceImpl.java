package com.travel.tour.service.impl;

import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.tour.constant.TourStatus;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.repository.TourScheduleRepository;
import com.travel.tour.service.SeatAllocationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatAllocationDomainServiceImpl implements SeatAllocationDomainService {

    private final TourScheduleRepository tourScheduleRepository;

    @Override
    @Transactional
    public boolean reserveSeats(UUID scheduleId, int requestedSeats) {
        log.info("Thực hiện giữ chỗ cho Lịch trình ID: {}, số chỗ yêu cầu: {}", scheduleId, requestedSeats);

        // 1. Khóa bi quan (Pessimistic Lock) chống tranh chấp chỗ đồng thời
        TourScheduleEntity schedule = tourScheduleRepository.findByIdWithLock(scheduleId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_SCHEDULE_NOT_FOUND, "Không tìm thấy lịch khởi hành với ID: " + scheduleId));

        // 2. Quy tắc DDD Invariant: Tour chứa lịch khởi hành phải đang mở bán (PUBLISHED)
        if (schedule.getTour() == null || schedule.getTour().getStatus() != TourStatus.PUBLISHED) {
            throw BusinessException.of(ErrorCode.TOUR_NOT_PUBLISHED, "Chuyến đi này hiện không mở bán.");
        }

        // 3. Quy tắc DDD Invariant: Số chỗ khả dụng phải >= số chỗ yêu cầu
        if (schedule.getAvailableSeats() < requestedSeats) {
            throw BusinessException.of(ErrorCode.TOUR_SEATS_INSUFFICIENT, 
                    String.format("Không đủ chỗ trống. Số chỗ còn lại: %d, Số chỗ yêu cầu: %d", schedule.getAvailableSeats(), requestedSeats));
        }

        // 4. Trừ số chỗ khả dụng và cập nhật lại
        schedule.setAvailableSeats(schedule.getAvailableSeats() - requestedSeats);
        tourScheduleRepository.save(schedule);
        log.info("Giữ chỗ thành công. Số chỗ còn lại của Lịch trình ID {}: {}", scheduleId, schedule.getAvailableSeats());
        return true;
    }

    @Override
    @Transactional
    public void releaseSeats(UUID scheduleId, int releasedSeats) {
        log.info("Thực hiện hoàn chỗ cho Lịch trình ID: {}, số lượng hoàn: {}", scheduleId, releasedSeats);

        TourScheduleEntity schedule = tourScheduleRepository.findByIdWithLock(scheduleId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_SCHEDULE_NOT_FOUND, "Không tìm thấy lịch khởi hành với ID: " + scheduleId));

        int newAvailableSeats = Math.min(schedule.getTotalSeats(), schedule.getAvailableSeats() + releasedSeats);
        schedule.setAvailableSeats(newAvailableSeats);
        tourScheduleRepository.save(schedule);
        log.info("Hoàn chỗ thành công. Số chỗ còn lại của Lịch trình ID {}: {}", scheduleId, schedule.getAvailableSeats());
    }
}
