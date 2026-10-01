package com.travel.ai.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Structured Output ViewModel trả về bài tư vấn tour du lịch hoàn chỉnh của AI RAG
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRecommendationVm {

    @JsonProperty("introduction")
    private String introduction;

    @JsonProperty("recommended_tours")
    private List<RecommendedTourVm> recommendedTours;

    @JsonProperty("detailed_itinerary")
    private List<DayItineraryVm> detailedItinerary;
}
