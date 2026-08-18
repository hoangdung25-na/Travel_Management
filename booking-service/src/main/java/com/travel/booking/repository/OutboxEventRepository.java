package com.travel.booking.repository;

import com.travel.booking.constant.OutboxStatus;
import com.travel.booking.entity.OutboxEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    // Lấy danh sách event có trạng thái chỉ định (PENDING) sắp xếp theo thời gian tạo để Outbox Publisher quét
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
