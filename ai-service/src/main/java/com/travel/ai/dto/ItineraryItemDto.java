package com.travel.ai.dto;

import lombok.*;

/**
 * DTO đại diện cho thông tin lịch trình một ngày của Tour
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItemDto {
    private Integer dayNumber;
    private String title;
    private String description;
    private String activity;
}
