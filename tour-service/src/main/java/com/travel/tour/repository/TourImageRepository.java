package com.travel.tour.repository;

import com.travel.tour.entity.TourImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TourImageRepository extends JpaRepository<TourImageEntity, UUID> {
    List<TourImageEntity> findByTourIdOrderByDisplayOrderAsc(UUID tourId);
}
