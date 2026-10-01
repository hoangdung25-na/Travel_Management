package com.travel.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO tìm kiếm ngữ nghĩa và tư vấn Tour AI
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiQueryRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @JsonAlias({"promptText", "prompt"})
    private String queryText;

    private BigDecimal maxBudget;
    private String destination;

    @Builder.Default
    private Integer topK = 5;
}
