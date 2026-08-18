package com.travel.tour.repository;

import com.travel.tour.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, UUID>, JpaSpecificationExecutor<TourEntity> {

    Optional<TourEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT DISTINCT t FROM TourEntity t " +
           "LEFT JOIN FETCH t.itineraries " +
           "LEFT JOIN FETCH t.schedules " +
           "LEFT JOIN FETCH t.destinations " +
           "WHERE t.id = :id")
    Optional<TourEntity> findByIdWithDetails(@Param("id") UUID id);
}
