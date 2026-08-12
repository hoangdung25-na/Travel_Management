package com.travel.tour.repository;

import com.travel.tour.entity.Tour;
import com.travel.tour.constant.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID> {
    
    boolean existsByCode(String code);

    // Tìm kiếm Tour đang PUBLISHED và có Schedule còn chỗ trống (availableSeats > 0)
    // Cần phải join với TourSchedule để check giá và seats
    @Query("SELECT DISTINCT t FROM Tour t " +
           "JOIN t.schedules s " +
           "WHERE t.status = :status " +
           "AND s.availableSeats > 0 " +
           "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minPrice IS NULL OR s.priceAdult >= :minPrice) " +
           "AND (:maxPrice IS NULL OR s.priceAdult <= :maxPrice)")
    Page<Tour> searchAvailableTours(
            @Param("status") TourStatus status,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
