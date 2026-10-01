package com.travel.tour.config;

import com.travel.tour.constant.TourStatus;
import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.entity.ItineraryEntity;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import com.travel.tour.repository.DestinationRepository;
import com.travel.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourDataInitializer implements CommandLineRunner {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;

    @Override
    public void run(String... args) {
        if (tourRepository.count() > 0) {
            log.info("Cơ sở dữ liệu Tour đã có {} bản ghi. Bỏ qua khởi tạo dữ liệu mẫu.", tourRepository.count());
            return;
        }

        log.info("Đang khởi tạo dữ liệu Tour mẫu cho hệ thống...");

        // 1. Tạo danh sách Điểm đến (Destinations)
        DestinationEntity dn = DestinationEntity.builder()
                .name("Đà Nẵng & Hội An")
                .city("Đà Nẵng")
                .country("Việt Nam")
                .build();

        DestinationEntity pq = DestinationEntity.builder()
                .name("Đảo Ngọc Phú Quốc")
                .city("Phú Quốc")
                .country("Việt Nam")
                .build();

        DestinationEntity hg = DestinationEntity.builder()
                .name("Hà Giang Loop")
                .city("Hà Giang")
                .country("Việt Nam")
                .build();

        DestinationEntity nt = DestinationEntity.builder()
                .name("Vịnh Biển Nha Trang")
                .city("Nha Trang")
                .country("Việt Nam")
                .build();

        DestinationEntity hl = DestinationEntity.builder()
                .name("Vịnh Hạ Long")
                .city("Quảng Ninh")
                .country("Việt Nam")
                .build();

        DestinationEntity sp = DestinationEntity.builder()
                .name("Sapa Mờ Sương")
                .city("Lào Cai")
                .country("Việt Nam")
                .build();

        destinationRepository.saveAll(List.of(dn, pq, hg, nt, hl, sp));

        // 2. Tạo Tour 1: Đà Nẵng 3N2Đ
        TourEntity tour1 = TourEntity.builder()
                .code("TOUR-DN-3N2D")
                .title("Tour Khám Phá Đà Nẵng - Ba Na Hills - Phố Cổ Hội An 3N2Đ")
                .description("Hành trình trải nghiệm tuyệt vời miền Trung: Chinh phục đỉnh Bà Nà Hills, check-in Cầu Vàng huyền thoại, ngắm hoàng hôn Phố cổ Hội An rực rỡ đèn lồng và thưởng thức ẩm thực đặc sản cao cấp.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(dn))
                .build();

        ItineraryEntity itin1_1 = ItineraryEntity.builder()
                .tour(tour1)
                .dayNumber(1)
                .title("Đón Sân Bay - Check-in Biển Mỹ Khê - Ngắm Cầu Rồng Phun Lửa")
                .content("Xe và HDV đón quý khách tại Sân bay Đà Nẵng. Nhận phòng khách sạn nghỉ ngơi, tự do tắm biển Mỹ Khê. Buổi tối thưởng thức đặc sản Bánh tráng thịt heo 2 đầu da, dạo phố sông Hàn.")
                .build();

        ItineraryEntity itin1_2 = ItineraryEntity.builder()
                .tour(tour1)
                .dayNumber(2)
                .title("KDL Bà Nà Hills - Cầu Vàng - Khám Phá Phố Cổ Hội An")
                .content("Khởi hành đi Bà Nà Hills, di chuyển tuyến cáp treo đạt 4 kỷ lục thế giới. Check-in Cầu Vàng, Làng Pháp, Fantasy Park. Chiều di chuyển tham quan Phố cổ Hội An, thả đèn hoa đăng sông Hoài.")
                .build();

        ItineraryEntity itin1_3 = ItineraryEntity.builder()
                .tour(tour1)
                .dayNumber(3)
                .title("Danh Thắng Ngũ Hành Sơn - Mua Sắm Chợ Hàn - Tiễn Đoàn")
                .content("Tham quan Ngũ Hành Sơn, Chùa Linh Ứng Bán Đảo Sơn Trà. Mua sắm đặc sản chả bò, hải sản khô tại Chợ Hàn. Xe tiễn quý khách ra sân bay Đà Nẵng.")
                .build();

        tour1.setItineraries(new ArrayList<>(List.of(itin1_1, itin1_2, itin1_3)));

        TourScheduleEntity sch1_1 = TourScheduleEntity.builder()
                .tour(tour1)
                .departureTime(OffsetDateTime.now().plusDays(10))
                .arrivalTime(OffsetDateTime.now().plusDays(13))
                .priceAdult(new BigDecimal("4500000"))
                .priceChild(new BigDecimal("3150000"))
                .totalSeats(25)
                .availableSeats(15)
                .build();

        TourScheduleEntity sch1_2 = TourScheduleEntity.builder()
                .tour(tour1)
                .departureTime(OffsetDateTime.now().plusDays(25))
                .arrivalTime(OffsetDateTime.now().plusDays(28))
                .priceAdult(new BigDecimal("4750000"))
                .priceChild(new BigDecimal("3320000"))
                .totalSeats(30)
                .availableSeats(20)
                .build();

        tour1.setSchedules(new ArrayList<>(List.of(sch1_1, sch1_2)));

        // 3. Tạo Tour 2: Phú Quốc 4N3Đ
        TourEntity tour2 = TourEntity.builder()
                .code("TOUR-PQ-4N3D")
                .title("Tour Thiên Đường Nắng Vàng Phú Quốc 4N3Đ - VinWonders & Safari")
                .description("Khám phá đảo ngọc Phú Quốc với tour trọn gói: Trải nghiệm cáp treo Hòn Thơm vượt biển dài nhất thế giới, lặn ngắm san hô 4 đảo, vui chơi không giới hạn VinWonders & Vinpearl Safari.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(pq))
                .build();

        ItineraryEntity itin2_1 = ItineraryEntity.builder()
                .tour(tour2)
                .dayNumber(1)
                .title("Đón Sân Bay Phú Quốc - Check-in Sunset Sanato - Dinh Cậu")
                .content("Nghỉ dưỡng tại resort 4 sao, ngắm hoàng hôn Sunset Sanato nổi tiếng.")
                .build();

        ItineraryEntity itin2_2 = ItineraryEntity.builder()
                .tour(tour2)
                .dayNumber(2)
                .title("Khu Vui Chơi Giải Trí VinWonders & Công Viên Bán Hoang Dã Safari")
                .content("Khám phá thế giới động vật bán hoang dã và thủy cung Thủy Cung Cung Điện Hải Vương.")
                .build();

        tour2.setItineraries(new ArrayList<>(List.of(itin2_1, itin2_2)));

        TourScheduleEntity sch2_1 = TourScheduleEntity.builder()
                .tour(tour2)
                .departureTime(OffsetDateTime.now().plusDays(15))
                .arrivalTime(OffsetDateTime.now().plusDays(19))
                .priceAdult(new BigDecimal("6200000"))
                .priceChild(new BigDecimal("4340000"))
                .totalSeats(20)
                .availableSeats(12)
                .build();

        tour2.setSchedules(new ArrayList<>(List.of(sch2_1)));

        // 4. Tạo Tour 3: Hà Giang 3N2Đ
        TourEntity tour3 = TourEntity.builder()
                .code("TOUR-HG-3N2D")
                .title("Tour Phượt Hà Giang Loop - Mã Pí Lèng - Chinh Phục Cột Cờ Lũng Cú")
                .description("Trải nghiệm cung đường đèo Mã Pí Lèng huyền thoại, đi thuyền trên dòng sông Nho Quế xanh ngọc bích, giao lưu văn hóa H'Mông tại Bản Lô Lô Chải.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(hg))
                .build();

        ItineraryEntity itin3_1 = ItineraryEntity.builder()
                .tour(tour3)
                .dayNumber(1)
                .title("Hà Nội - Thành Phố Hà Giang - Quản Bạ - Yên Minh")
                .content("Vượt dốc Bắc Sum, ngắm Cổng Trời Quản Bạ và Núi Đôi Cô Tiên.")
                .build();

        tour3.setItineraries(new ArrayList<>(List.of(itin3_1)));

        TourScheduleEntity sch3_1 = TourScheduleEntity.builder()
                .tour(tour3)
                .departureTime(OffsetDateTime.now().plusDays(8))
                .arrivalTime(OffsetDateTime.now().plusDays(11))
                .priceAdult(new BigDecimal("3850000"))
                .priceChild(new BigDecimal("2695000"))
                .totalSeats(16)
                .availableSeats(8)
                .build();

        tour3.setSchedules(new ArrayList<>(List.of(sch3_1)));

        // 5. Tạo Tour 4: Nha Trang 3N2Đ
        TourEntity tour4 = TourEntity.builder()
                .code("TOUR-NT-3N2D")
                .title("Tour Khám Phá Biển Đảo Nha Trang - VinWonders & Đảo Điệp Sơn")
                .description("Đến với thành phố biển Nha Trang sôi động, check-in con đường đi bộ giữa biển độc đáo tại đảo Điệp Sơn, vui chơi VinWonders và thư giãn tắm bùn khoáng nóng.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(nt))
                .build();

        TourScheduleEntity sch4_1 = TourScheduleEntity.builder()
                .tour(tour4)
                .departureTime(OffsetDateTime.now().plusDays(12))
                .arrivalTime(OffsetDateTime.now().plusDays(15))
                .priceAdult(new BigDecimal("4200000"))
                .priceChild(new BigDecimal("2940000"))
                .totalSeats(30)
                .availableSeats(18)
                .build();

        tour4.setSchedules(new ArrayList<>(List.of(sch4_1)));

        // 6. Tạo Tour 5: Hạ Long 2N1Đ
        TourEntity tour5 = TourEntity.builder()
                .code("TOUR-HL-2N1D")
                .title("Tour Du Thuyền 5 Sao Vịnh Hạ Long - Đảo Titop & Hang Sửng Sốt")
                .description("Nghỉ dưỡng đẳng cấp trên du thuyền 5 sao lướt sóng qua hàng nghìn hòn đảo đá vôi kỳ vĩ. Chèo thuyền Kayak, chèo SUP, tập Tai Chi buổi sáng và ăn tối buffet hải sản.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(hl))
                .build();

        TourScheduleEntity sch5_1 = TourScheduleEntity.builder()
                .tour(tour5)
                .departureTime(OffsetDateTime.now().plusDays(5))
                .arrivalTime(OffsetDateTime.now().plusDays(7))
                .priceAdult(new BigDecimal("3500000"))
                .priceChild(new BigDecimal("2450000"))
                .totalSeats(20)
                .availableSeats(10)
                .build();

        tour5.setSchedules(new ArrayList<>(List.of(sch5_1)));

        // 7. Tạo Tour 6: Sa Pa 3N2Đ
        TourEntity tour6 = TourEntity.builder()
                .code("TOUR-SP-3N2D")
                .title("Tour Sa Pa Săn Mây Fansipan - Bản Cát Cát - Moana Sapa")
                .description("Săn mây trên 'Nóc nhà Đông Dương' Fansipan ở độ cao 3.143m, dạo bước qua nấc thang bản Cát Cát của người H'Mông và check-in sống ảo tại Moana Sapa.")
                .status(TourStatus.PUBLISHED)
                .destinations(Set.of(sp))
                .build();

        TourScheduleEntity sch6_1 = TourScheduleEntity.builder()
                .tour(tour6)
                .departureTime(OffsetDateTime.now().plusDays(14))
                .arrivalTime(OffsetDateTime.now().plusDays(17))
                .priceAdult(new BigDecimal("3290000"))
                .priceChild(new BigDecimal("2300000"))
                .totalSeats(25)
                .availableSeats(15)
                .build();

        tour6.setSchedules(new ArrayList<>(List.of(sch6_1)));

        tourRepository.saveAll(List.of(tour1, tour2, tour3, tour4, tour5, tour6));
        log.info("Khởi tạo 6 Tour mẫu thành công vào cơ sở dữ liệu!");
    }
}
