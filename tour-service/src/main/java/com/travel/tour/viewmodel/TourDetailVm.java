package com.travel.tour.viewmodel;

import com.travel.tour.constant.TourStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TourDetailVm {
    private UUID tourId;
    private String code;
    private String title;
    private String description;
    private TourStatus status;
    private Instant createdAt;
    private List<ItineraryVm> itineraries;
    private List<TourScheduleVm> schedules;

    // Getters and Setters
    public UUID getTourId() { return tourId; }
    public void setTourId(UUID tourId) { this.tourId = tourId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<ItineraryVm> getItineraries() { return itineraries; }
    public void setItineraries(List<ItineraryVm> itineraries) { this.itineraries = itineraries; }
    public List<TourScheduleVm> getSchedules() { return schedules; }
    public void setSchedules(List<TourScheduleVm> schedules) { this.schedules = schedules; }

    public static class ItineraryVm {
        private Integer dayNumber;
        private String title;
        private String content;

        public Integer getDayNumber() { return dayNumber; }
        public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class TourScheduleVm {
        private UUID scheduleId;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer availableSeats;
        
        public UUID getScheduleId() { return scheduleId; }
        public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }
        public Instant getDepartureTime() { return departureTime; }
        public void setDepartureTime(Instant departureTime) { this.departureTime = departureTime; }
        public Instant getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(Instant arrivalTime) { this.arrivalTime = arrivalTime; }
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    }
}
