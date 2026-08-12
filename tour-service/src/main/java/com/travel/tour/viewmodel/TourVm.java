package com.travel.tour.viewmodel;

import com.travel.tour.constant.TourStatus;
import java.math.BigDecimal;
import java.util.UUID;

public class TourVm {
    private UUID tourId;
    private String code;
    private String title;
    private BigDecimal minPrice;
    private Integer availableSeats;
    private TourStatus status;

    // Getters and Setters
    public UUID getTourId() { return tourId; }
    public void setTourId(UUID tourId) { this.tourId = tourId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
}
