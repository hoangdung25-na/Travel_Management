package com.travel.booking.client.impl;

import com.travel.booking.client.TourServiceClient;
import com.travel.booking.client.dto.TourScheduleResponse;
import com.travel.common.core.dto.ApiResponse;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class TourServiceClientImpl implements TourServiceClient {

    private final RestClient restClient;

    public TourServiceClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${app.services.tour-service.url:http://localhost:8082}") String tourServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(tourServiceUrl)
                .build();
    }

    @Override
    public TourScheduleResponse getScheduleById(UUID scheduleId) {
        log.info("Gọi HTTP RestClient sang tour-service lấy giá chuyến đi ID: {}", scheduleId);
        try {
            // Thực hiện cuộc gọi HTTP GET thực tế sang tour-service
            ApiResponse<TourScheduleResponse> response = restClient.get()
                    .uri("/api/v1/tours/schedules/{id}", scheduleId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<TourScheduleResponse>>() {
                    });

            if (response != null && response.data() != null) {
                return response.data();
            }

            throw BusinessException.of(ErrorCode.TOUR_SCHEDULE_NOT_FOUND,
                    "Không tìm thấy thông tin chuyến đi ID: " + scheduleId);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            log.warn(
                    "Không kết nối được tới tour-service HTTP server. Sử dụng giá fallback mặc định cho scheduleId: {}",
                    scheduleId);
            // Fallback an toàn khi tour-service offline (chế độ phát triển local)
            return new TourScheduleResponse(
                    scheduleId,
                    new BigDecimal("5000000.00"),
                    new BigDecimal("3750000.00"),
                    20);
        }
    }
}
