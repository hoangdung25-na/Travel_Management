package com.travel.booking.repository;

import com.travel.booking.entity.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, UUID> {

    // Tìm danh sách hành khách theo bookingId
    List<BookingPassenger> findByBookingId(UUID bookingId);
}
