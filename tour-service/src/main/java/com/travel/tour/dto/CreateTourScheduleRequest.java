package com.travel.tour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public class CreateTourScheduleRequest {
    @NotNull
    private Instant departureTime;

    @NotNull
    private Instant arrivalTime;

    @NotNull
    @Positive
    private BigDecimal priceAdult;

    @NotNull
    @Positive
    private BigDecimal priceChild;

    @NotNull
    @Min(1)
    private Integer totalSeats;

    // Getters and Setters
    public Instant getDepartureTime() { return departureTime; }
    public void setDepartureTime(Instant departureTime) { this.departureTime = departureTime; }
    public Instant getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(Instant arrivalTime) { this.arrivalTime = arrivalTime; }
    public BigDecimal getPriceAdult() { return priceAdult; }
    public void setPriceAdult(BigDecimal priceAdult) { this.priceAdult = priceAdult; }
    public BigDecimal getPriceChild() { return priceChild; }
    public void setPriceChild(BigDecimal priceChild) { this.priceChild = priceChild; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
}
