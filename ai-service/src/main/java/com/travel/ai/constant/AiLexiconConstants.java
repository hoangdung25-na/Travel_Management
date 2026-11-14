package com.travel.ai.constant;

import java.util.Collections;
import java.util.Set;

/**
 * Lớp lưu trữ tập trung toàn bộ Từ điển & Từ khóa Ngữ nghĩa cho AI Service.
 * Loại bỏ hoàn toàn Magic Strings, tối ưu tra cứu O(1) và giữ cho Service code luôn sạch sẽ, dễ đọc.
 */
public final class AiLexiconConstants {

    private AiLexiconConstants() {
        // Private constructor ngăn việc khởi tạo instance
    }

    /**
     * Danh sách các câu/từ chào hỏi ban đầu phổ biến
     */
    public static final Set<String> GREETINGS = Collections.unmodifiableSet(Set.of(
            "chào", "hi", "hello", "xin chào", "chào bạn", "bạn là ai",
            "alo", "cho hỏi", "chào bot", "chao xìn", "hey", "halo", "chào em"
    ));

    /**
     * Danh sách các đại từ chỉ định thay thế trong hội thoại ngữ cảnh đa lượt (Multi-turn Context)
     */
    public static final Set<String> RELATIVE_PRONOUNS = Collections.unmodifiableSet(Set.of(
            "này", "đó", "đấy", "nó", "chuyến này", "chuyến đó", "ở đó", "ở đây",
            "giá này", "giá đó", "tour đó", "tour này", "chỗ này", "chỗ đó",
            "đoạn này", "tour 1", "tour 2", "chuyến 1", "chuyến 2", "cái này", "cái đó",
            "cái thứ nhất", "cái thứ hai", "tour đầu", "tour sau"
    ));

    /**
     * Từ điển các khái niệm, thực thể và dịch vụ liên quan đến Du lịch
     */
    public static final Set<String> TRAVEL_KEYWORDS = Collections.unmodifiableSet(Set.of(
            // Điểm đến & Loại hình
            "tour", "du lịch", "đi chơi", "nghỉ dưỡng", "chuyến", "vé", "đặt",
            "khách sạn", "homestay", "resort", "biển", "núi", "đảo", "vịnh", "thác", "rừng",
            // Phương tiện & Dịch vụ
            "bay", "máy bay", "tàu", "xe", "lịch trình", "ăn uống", "đặc sản", "ẩm thực",
            "ngân sách", "giá", "tiền", "check-in", "check in", "tham quan", "khám phá",
            "trải nghiệm", "hướng dẫn",
            // Thủ tục & Quy định
            "visa", "hộ chiếu", "cccd", "hủy", "hoàn", "cọc", "thanh toán",
            // Đối tượng khách hàng
            "trẻ em", "người lớn", "gia đình", "bạn bè", "người yêu", "cặp đôi",
            // Thời gian & Cảm xúc
            "cuối tuần", "mùa hè", "ngắm", "lung linh", "cảnh", "điểm đến", "chỗ nào", "ở đâu",
            "gợi ý", "tư vấn", "chọn"
    ));

    /**
     * Danh sách stop-words tiếng Việt phổ biến trong tìm kiếm du lịch
     */
    public static final Set<String> STOP_WORDS = Collections.unmodifiableSet(Set.of(
            "tour", "tours", "du", "lịch", "chuyến", "đi", "cho", "tôi", "ở", "tại", "có", "tìm",
            "muốn", "cần", "giá", "vé", "người", "ngày", "đêm", "n2đ", "n3đ", "n4đ", "n5đ",
            "trọn", "gói", "khám", "phá", "với", "và", "là", "gì", "nào", "sao", "được", "không",
            "ạ", "nhé", "nha", "xin", "chào", "bạn", "ơi", "khoảng", "tầm", "dưới", "trên"
    ));

    /**
     * Kiểm tra xem một chuỗi có đơn thuần là lời chào ban đầu hay không (khớp O(1))
     */
    public static boolean isGreeting(String text) {
        if (text == null) return false;
        return GREETINGS.contains(text.trim().toLowerCase());
    }

    /**
     * Kiểm tra xem câu hỏi có chứa các đại từ chỉ định thay thế liên kết ngữ cảnh không
     */
    public static boolean hasRelativePronoun(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String pronoun : RELATIVE_PRONOUNS) {
            if (lower.contains(pronoun)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem câu hỏi có chứa các khái niệm/từ khóa liên quan đến du lịch không
     */
    public static boolean isTravelRelated(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String keyword : TRAVEL_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem từ có phải là stop-word du lịch cần loại bỏ không
     */
    public static boolean isStopWord(String word) {
        if (word == null) return false;
        return STOP_WORDS.contains(word.trim().toLowerCase());
    }
}
