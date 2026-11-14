package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.constant.ChunkType;
import com.travel.ai.entity.TourEmbeddingEntity;
import com.travel.ai.repository.TourEmbeddingRepository;
import com.travel.ai.service.CompanyPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyPolicyServiceImpl implements CompanyPolicyService {

    private final TourEmbeddingRepository tourEmbeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // UUID cố định đại diện cho Tri thức Doanh nghiệp
    private static final UUID POLICY_TOUR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    @Transactional
    public int initCompanyPolicies() {
        log.info("Bắt đầu nạp Tri thức Chính sách Doanh nghiệp vào Vector DB (pgvector)...");

        // 1. Xóa các chính sách cũ để chống trôi dữ liệu
        try {
            jdbcTemplate.update("DELETE FROM tour_embeddings WHERE chunk_type = 'POLICY'");
        } catch (Exception e) {
            log.warn("Cảnh báo khi xóa chính sách cũ: {}", e.getMessage());
        }

        List<Map<String, String>> policies = getDefinedPolicies();
        int count = 0;

        for (Map<String, String> policy : policies) {
            String title = policy.get("title");
            String content = policy.get("content");
            String category = policy.get("category");

            Map<String, Object> meta = new HashMap<>();
            meta.put("chunk_type", ChunkType.POLICY.name());
            meta.put("title", title);
            meta.put("category", category);

            String metaJson = "{}";
            try {
                metaJson = objectMapper.writeValueAsString(meta);
            } catch (Exception ignored) {}

            TourEmbeddingEntity entity = TourEmbeddingEntity.builder()
                    .tourId(POLICY_TOUR_ID)
                    .chunkType(ChunkType.POLICY)
                    .content(content)
                    .metadata(metaJson)
                    .build();

            TourEmbeddingEntity saved = tourEmbeddingRepository.save(entity);
            tourEmbeddingRepository.flush();

            // Tạo Vector Embedding
            try {
                float[] vector = embeddingModel.embed(content);
                if (vector != null) {
                    String vectorSql = "UPDATE tour_embeddings SET embedding = ?::vector WHERE id = ?";
                    jdbcTemplate.update(vectorSql, Arrays.toString(vector), saved.getId());
                }
            } catch (Exception ex) {
                log.warn("Không thể tạo Vector cho chính sách '{}': {}", title, ex.getMessage());
            }

            count++;
            log.info("Đã nạp chính sách vào Vector DB: '{}'", title);
        }

        log.info("HOÀN TẤT: Đã nạp thành công {} chính sách doanh nghiệp vào Vector DB!", count);
        return count;
    }

    private List<Map<String, String>> getDefinedPolicies() {
        List<Map<String, String>> list = new ArrayList<>();

        // 1. Quy định Hủy vé & Hoàn tiền
        list.add(Map.of(
                "title", "Quy định Hủy vé và Hoàn tiền của Travel Platform",
                "category", "CANCELLATION_REFUND",
                "content", """
                    Chính sách Hủy vé và Quy định Hoàn tiền của Travel Platform:
                    1. Mức phạt và Tỷ lệ hoàn tiền:
                       - Hủy trước ngày khởi hành từ 15 ngày trở lên: Hoàn lại 100% tiền tour (chỉ trừ phí giao dịch nếu có).
                       - Hủy từ 7 đến 14 ngày trước ngày đi: Phí phạt 30% giá trị tour, hoàn lại 70%.
                       - Hủy từ 3 đến 6 ngày trước ngày đi: Phí phạt 50% giá trị tour, hoàn lại 50%.
                       - Hủy trong vòng 48 giờ hoặc vào ngày khởi hành: Phí phạt 100%, không hoàn tiền.
                    2. Thời hạn nhận tiền hoàn:
                       - Tiền hoàn sẽ được chuyển tự động về tài khoản ngân hàng hoặc thẻ thanh toán ban đầu trong vòng 3 đến 5 ngày làm việc.
                    3. Quy trình thực hiện:
                       - Khách hàng đăng nhập vào website, truy cập mục 'Đơn hàng của tôi', chọn tour cần hủy và nhấn 'Yêu cầu hủy vé', hoặc liên hệ Tổng đài CSKH 1900 6868 để được xử lý ngay lập tức.
                    """
        ));

        // 2. Phương thức Thanh toán & Đặt cọc
        list.add(Map.of(
                "title", "Phương thức Thanh toán và Đặt cọc giữ chỗ",
                "category", "PAYMENT_DEPOSIT",
                "content", """
                    Chính sách Thanh toán, Đặt cọc và Giữ chỗ tại Travel Platform:
                    1. Tỷ lệ đặt cọc:
                       - Quý khách chỉ cần đặt cọc trước từ 30% đến 50% tổng giá trị tour để giữ chỗ chắc chắn.
                       - Số tiền còn lại quý khách thanh toán trước ngày khởi hành từ 5 đến 7 ngày.
                    2. Các cổng thanh toán hỗ trợ:
                       - Cổng VNPAY QR: Quét mã QR thanh toán nhanh chóng qua hơn 40 ứng dụng ngân hàng và ví điện tử.
                       - Chuyển khoản ngân hàng trực tiếp: Nhận số tài khoản ngân hàng và cú pháp chuyển khoản tự động ngay sau khi đặt tour.
                       - Thẻ Quốc tế Visa, Mastercard, JCB: Bảo mật tiêu chuẩn quốc tế 3D-Secure.
                       - Tiền mặt: Nộp trực tiếp tại các văn phòng giao dịch của Travel Platform tại Hà Nội và TP. Hồ Chí Minh.
                    """
        ));

        // 3. Thủ tục & Giấy tờ cần thiết
        list.add(Map.of(
                "title", "Thủ tục và Giấy tờ tùy thân khi đi du lịch",
                "category", "DOCUMENT_PROCEDURE",
                "content", """
                    Quy định về Thủ tục và Giấy tờ tùy thân khi tham gia tour du lịch:
                    1. Tour Nội địa Việt Nam:
                       - Người lớn (từ 14 tuổi trở lên): Bắt buộc mang bản gốc Căn cước công dân (CCCD) gắn chip hoặc Hộ chiếu còn hạn sử dụng để làm thủ tục bay, tàu hỏa và check-in khách sạn.
                       - Trẻ em dưới 14 tuổi: Mang bản sao trích lục hoặc bản gốc Giấy khai sinh hợp lệ.
                       - Giờ nhận phòng khách sạn (check-in) thường từ 14:00 và giờ trả phòng (check-out) trước 12:00 trưa.
                    2. Tour Quốc tế:
                       - Hộ chiếu (Passport) còn thời hạn tối thiểu 6 tháng tính từ ngày kết thúc chuyến đi.
                       - Visa: Các nước Đông Nam Á (Thái Lan, Singapore, Malaysia...) được miễn visa dưới 30 ngày. Với các tour Hàn Quốc, Nhật Bản, Châu Âu, công ty hỗ trợ thủ tục hồ sơ làm visa trọn gói.
                    """
        ));

        // 4. Hotline và Kênh Hỗ trợ Khách hàng 24/7
        list.add(Map.of(
                "title", "Thông tin Liên hệ, Tổng đài và Hotline CSKH 24/7",
                "category", "CONTACT_SUPPORT",
                "content", """
                    Kênh hỗ trợ và thông tin liên hệ Travel Platform 24/7:
                    - Tổng đài tư vấn và đặt tour miễn cước: 1900 6868.
                    - Hotline hỗ trợ khẩn cấp, giải quyết khiếu nại và hủy đổi vé: 0909 123 456.
                    - Email hỗ trợ khách hàng: support@travelplatform.vn.
                    - Địa chỉ trụ sở chính: Tòa nhà Travel Center, Quận 1, TP. Hồ Chí Minh và Chi nhánh Hoàn Kiếm, Hà Nội.
                    - Đội ngũ tư vấn viên luôn trực 24/7 trên Website và Hotline để hỗ trợ du khách mọi lúc, mọi nơi.
                    """
        ));

        // 5. Chính sách Trẻ em & Người cao tuổi
        list.add(Map.of(
                "title", "Chính sách Giá vé cho Trẻ em và Người cao tuổi",
                "category", "CHILDREN_ELDERLY",
                "content", """
                    Quy định giá tour cho Trẻ em và Người cao tuổi:
                    1. Trẻ em:
                       - Trẻ em dưới 2 tuổi: Miễn phí giá tour (chỉ tính phụ thu vé máy bay theo quy định hãng hàng không).
                       - Trẻ em từ 2 đến dưới 5 tuổi: Tính 50% giá tour người lớn (ngủ chung giường bố mẹ, có ghế ngồi riêng trên xe).
                       - Trẻ em từ 5 đến dưới 10 tuổi: Tính 75% giá tour người lớn (suất ăn riêng, ngủ chung phòng bố mẹ).
                       - Trẻ em từ 10 tuổi trở lên: Tính 100% như giá người lớn.
                    2. Người cao tuổi (từ 60 tuổi trở lên):
                       - Được ưu tiên sắp xếp hàng ghế phía trước trên xe ô tô du lịch để tránh say xe.
                       - Khách sạn và nhà hàng phục vụ thực đơn thanh đạm, dễ tiêu hóa theo yêu cầu.
                    """
        ));

        // 6. Dịch vụ Ăn uống và Khách ăn chay
        list.add(Map.of(
                "title", "Dịch vụ Ăn uống, Suất ăn Chay và Yêu cầu Dị ứng",
                "category", "MEAL_SERVICE",
                "content", """
                    Dịch vụ Ẩm thực và Yêu cầu suất ăn chay tại Travel Platform:
                    - Tất cả các bữa ăn trong chương trình tour được phục vụ tại các nhà hàng uy tín, đặc sản địa phương đảm bảo an toàn vệ sinh thực phẩm.
                    - Khách ăn chay, ăn kiêng hoặc có dị ứng thực phẩm: Travel Platform hỗ trợ sắp xếp suất ăn riêng chu đáo. Quý khách chỉ cần ghi chú vào ô 'Yêu cầu đặc biệt' khi đặt tour hoặc thông báo cho nhân viên tư vấn trước ngày khởi hành 48 giờ.
                    """
        ));

        return list;
    }
}
