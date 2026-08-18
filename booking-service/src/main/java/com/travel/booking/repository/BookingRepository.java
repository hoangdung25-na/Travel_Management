package com.travel.booking.repository;

import com.travel.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Tìm danh sách booking của một User có phân trang, sắp xếp mới nhất lên đầu
    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Tìm đơn hàng theo mã công khai hiển thị (bookingCode)
    Optional<Booking> findByBookingCode(String bookingCode);

    // Kiểm tra xem mã bookingCode đã tồn tại chưa
    boolean existsByBookingCode(String bookingCode);

    // FETCH JOIN lấy Booking cùng danh sách Passengers trong 1 câu SELECT duy nhất (Giải quyết vấn đề N+1 Query)
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.passengers WHERE b.id = :id")
    Optional<Booking> findByIdWithPassengers(@Param("id") UUID id);
}
