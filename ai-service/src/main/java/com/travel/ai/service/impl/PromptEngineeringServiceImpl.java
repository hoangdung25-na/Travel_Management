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
            Nhiệm vụ của bạn là tư vấn tour và lịch trình du lịch dựa trên DỮ LIỆU ĐƯỢC CUNG CẤP dưới đây.

            QUY TẮC TƯ VẤN THÔNG MINH:
            1. Khách hàng có thể chỉ gõ tên điểm đến, danh lam thắng cảnh hoặc tỉnh thành (Ví dụ: 'Nghệ An', 'Cửa Lò', 'Đà Nẵng', 'Bà Nà', 'Sa Pa', 'Phú Quốc', 'Hà Giang'...).
               Hãy hiểu nhu cầu và chủ động gợi ý ngay các Tour có điểm đến, khu vực hoặc lộ trình đi qua địa danh/tỉnh thành đó trong dữ liệu ngữ cảnh. KHÔNG bắt buộc câu hỏi phải khớp chính xác với tiêu đề tour.
            2. TUYỆT ĐỐI KHÔNG tự sáng tạo ra các Tour hoặc giá vé không có trong dữ liệu ngữ cảnh.
            3. Khi đưa ra tour gợi ý trong danh sách recommended_tours, hãy tạo link đặt tour chuẩn: '/tours/{tour_id}'.
            4. Trình bày thân thiện, nhiệt tình, có cấu trúc rõ ràng với các điểm nhấn nổi bật của chuyến đi.

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

    @Override
    public String buildChatPrompt(String userQuery, List<RetrievedChunkDto> chunks) {
        return buildChatPrompt(userQuery, chunks, java.util.Collections.emptyList());
    }

    @Override
    public String buildChatPrompt(String userQuery, List<RetrievedChunkDto> chunks, List<com.travel.ai.dto.ChatMessageDto> history) {
        StringBuilder contextBuilder = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunkDto chunk = chunks.get(i);
            contextBuilder.append("--- [DỮ LIỆU CSDL ").append(i + 1).append(" (Loại: ").append(chunk.getChunkType()).append(")] ---\n");
            if (chunk.getTourId() != null) {
                contextBuilder.append("Tour ID: ").append(chunk.getTourId()).append("\n");
                contextBuilder.append("Link tour: /tours/").append(chunk.getTourId()).append("\n");
            }
            contextBuilder.append(chunk.getContent()).append("\n\n");
        }

        StringBuilder historyBuilder = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            historyBuilder.append("[LỊCH SỬ TRAO ĐỔI GẦN ĐÂY TRONG HỘI THOẠI]:\n");
            for (com.travel.ai.dto.ChatMessageDto msg : history) {
                String sender = "user".equalsIgnoreCase(msg.getRole()) ? "Khách hàng" : "Trợ lý AI";
                historyBuilder.append("• ").append(sender).append(": ").append(msg.getContent()).append("\n");
            }
            historyBuilder.append("\nLƯU Ý NGỮ CẢNH HỘI THOẠI (MULTI-TURN CONTEXT):\n");
            historyBuilder.append("- Hãy tham chiếu vào các lượt trao đổi trước đó để hiểu đúng đại từ chỉ định thay thế ('tour này', 'ở đó', 'chuyến đi đó', 'giá này', 'ngày khởi hành'...) mà khách hàng đang hỏi tiếp.\n\n");
        }

        return String.format("""
            [HƯỚNG DẪN HỆ THỐNG - CHUYÊN VIÊN CSKH & TƯ VẤN DU LỊCH 24/7]
            Bạn là Trợ lý AI Du lịch & Chăm sóc khách hàng độc quyền của Travel Platform.
            Nhiệm vụ: Giải đáp câu hỏi, tư vấn tour du lịch hoặc hỗ trợ chính sách cho khách hàng dựa vào [DỮ LIỆU THỰC TẾ TỪ CƠ SỞ DỮ LIỆU] bên dưới.

            QUY TẮC BẮT BUỘC:
            1. ĐÍNH KÈM ĐƯỜNG LINK TOUR: Khi gợi ý hoặc nhắc tới bất kỳ tour du lịch nào có trong dữ liệu, BẮT BUỘC chèn đường link dạng Markdown: [Tên Tour](/tours/{tourId}) để khách hàng có thể bấm vào xem chi tiết và đặt chỗ ngay.
            2. CHÍNH SÁCH DOANH NGHIỆP:
               - Với các câu hỏi về hoàn hủy vé, thời hạn nhận tiền hoàn, mức phạt cọc, phương thức thanh toán, giấy tờ tùy thân (CCCD/Passport/Visa), trẻ em, suất ăn chay: Trích dẫn chính xác theo các điều khoản trong dữ liệu CSDL được cung cấp.
               - Cung cấp số Hotline CSKH 24/7: 1900 6868 nếu khách cần hỗ trợ trực tiếp.
            3. KIỂM SOÁT PHẠM VI NGHIÊM NGẶT (STRICT DOMAIN BOUNDARY - CHỈ DU LỊCH & TOUR):
               - Bạn là Trợ lý AI Du lịch & Chăm sóc khách hàng độc quyền của Travel Platform. Bạn CHỈ ĐƯỢC PHÉP trả lời và tư vấn các chủ đề thuộc lĩnh vực:
                 + Tour du lịch, gợi ý điểm đến, lịch trình, danh lam thắng cảnh, thời tiết điểm đến, ẩm thực, văn hóa và kinh nghiệm du lịch.
                 + Chính sách, quy định đặt tour, hoàn hủy vé, thanh toán, giấy tờ tùy thân của Travel Platform.
               - NẾU KHÁCH HÀNG HỎI CÁC CHỦ ĐỀ NGOÀI PHẠM VI TRÊN (Ví dụ: lập trình/viết code, giải toán, bài tập học tập, y tế chữa bệnh, chính trị, triết học, tài chính không liên quan du lịch, chuyện phiếm ngoài luồng...):
                 + BẠN BẮT BUỘC PHẢI LỊCH SỰ TỪ CHỐI NGAY.
                 + Mẫu phản hồi chuẩn: Lịch sự thông báo bạn là Trợ lý AI Du lịch chuyên trách của Travel Platform nên chỉ có thể hỗ trợ các thông tin về tour và dịch vụ lữ hành; sau đó chủ động gợi ý một số điểm đến hoặc tour nổi bật của nền tảng để khách tham khảo.
                 + TUYỆT ĐỐI KHÔNG giải bài tập/toán, KHÔNG viết code phần mềm, KHÔNG trả lời lan man các kiến thức ngoài ngành du lịch.
            4. VĂN PHONG & TRÌNH BÀY:
               - Tiếng Việt tự nhiên, nhiệt tình, lịch sự, xưng hô 'Em' hoặc 'Travel Platform' và 'Quý khách' hoặc 'Bạn'.
               - Sử dụng Markdown rõ ràng: gạch đầu dòng, in đậm các mốc quan trọng, kèm emoji sinh động.

            %s
            [DỮ LIỆU THỰC TẾ TỪ CƠ SỞ DỮ LIỆU]:
            %s

            [CÂU HỎI HIỆN TẠI CỦA KHÁCH HÀNG]:
            %s
            """,
                historyBuilder.toString(),
                contextBuilder.toString(),
                userQuery
        );
    }
}
