package com.travel.ai.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * ViewModel đại diện cho thông tin lịch trình từng ngày trong bản tư vấn của AI
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayItineraryVm {

    @JsonProperty("day")
    private Integer day;

    @JsonProperty("activity")
    private String activity;
}
