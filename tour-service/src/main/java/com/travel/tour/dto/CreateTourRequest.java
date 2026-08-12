package com.travel.tour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateTourRequest {
    @NotBlank
    @Pattern(regexp="^[A-Z0-9-]+$")
    private String code;

    @NotBlank
    @Size(min=10, max=250)
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<ItineraryDto> itineraries;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ItineraryDto> getItineraries() { return itineraries; }
    public void setItineraries(List<ItineraryDto> itineraries) { this.itineraries = itineraries; }

    public static class ItineraryDto {
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
}
