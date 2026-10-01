package com.travel.ai.service.impl;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;
import com.travel.ai.service.PromptEngineeringService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptEngineeringServiceImpl implements PromptEngineeringService {

    @Override
    public String buildPrompt(AiQueryRequest request, List<RetrievedChunkDto> chunks, String outputFormatInstructions) {
        StringBuilder contextBuilder = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunkDto chunk = chunks.get(i);
            contextBuilder.append("--- [Đoạn dữ liệu ").append(i + 1).append("] ---\n");
            contextBuilder.append("Tour ID: ").append(chunk.getTourId()).append("\n");
            contextBuilder.append("Nội dung: ").append(chunk.getContent()).append("\n");
            if (chunk.getMetadata() != null) {
                contextBuilder.append("Metadata: ").append(chunk.getMetadata()).append("\n");
            }
            contextBuilder.append("\n");
        }

        return String.format("""
            [HƯỚNG DẪN HỆ THỐNG]
            Bạn là Trợ lý AI Du lịch chuyên nghiệp của hệ thống Travel Platform.
            Nhiệm vụ của bạn là tư vấn lịch trình du lịch dựa CHỈ TRÊN DỮ LIỆU ĐƯỢC CUNG CẤP dưới đây.
            TUYỆT ĐỐI KHÔNG tự sáng tạo ra các Tour hoặc giá vé không có trong ngữ cảnh.
            Khi đưa ra tour gợi ý trong danh sách recommended_tours, hãy tạo link đặt tour chuẩn: '/tours/{tour_id}'.
            Nếu dữ liệu không đủ đáp ứng, hãy lịch sự thông báo cho khách hàng.

            [DỮ LIỆU NGỮ CẢNH TRÍCH XUẤT TỪ VECTOR DB]
            %s

            [YÊU CẦU CỦA KHÁCH HÀNG]
            %s

            [ĐỊNH DẠNG ĐẦU RA YÊU CẦU]
            %s
            """,
                contextBuilder.toString(),
                request.getQueryText(),
                outputFormatInstructions
        );
    }
}
