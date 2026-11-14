# 🗺️ LỘ TRÌNH PHÁT TRIỂN & HỌC TẬP DEEP-DIVE: `BOOKING-SERVICE`

Lộ trình này được chuẩn hóa theo đúng khuôn mẫu và quy ước cấu trúc dự án (`tour-service`), kết hợp toàn bộ tài liệu đặc tả hệ thống (`architecture.md`, `system_analysis_spec.md`, `ddd_domain_model_spec.md`, `database_erd_spec.md`, `rest_api_design_spec.md`).

---

## 📋 Tổng Quan Về `booking-service`
* **Nhiệm vụ chính:** Quản lý vòng đời Booking (`PENDING` $\rightarrow$ `PAYMENT_PENDING` / `SEATS_RESERVED` $\rightarrow$ `CONFIRMED` hoặc `CANCELLED`), đóng vai trò **Saga Initiator** (khởi xướng luồng Saga đặt tour) và **Saga State Manager** (quản lý chuyển đổi trạng thái), đồng thời áp dụng **Transactional Outbox Pattern** để phát sự kiện an toàn sang Kafka.
* **Database:** PostgreSQL `db_travel_booking` (Port `5434`).
* **Cấu trúc Package:** Tổ chức theo chuẩn Package-by-Layer tại `BookingServiceApplication.java` (`config`, `constant`, `controller`, `dto`, `viewmodel`, `entity`, `exception`, `helper`, `mapper`, `repository`, `service`, `listener`, `publisher`).

---

## 🚀 CÁC BƯỚC BẮT ĐẦU PHÁT TRIỂN `BOOKING-SERVICE`

### Bước 1: Cấu hình Hạ tầng & Database (`application.yml`)
1. Cấu hình kết nối Database `db_travel_booking` (Port `5434`) và Kafka trong `booking-service/src/main/resources/application.yml`.
2. Cấu hình Hibernate/JPA Schema (`hibernate.ddl-auto: update` hoặc Flyway SQL script `db/migration/`).
3. Cấu hình tích hợp `common-logging` (MDC Logging) và `common-security` (`UserHeaderFilter`).

---

### Bước 2: Định nghĩa JPA Entities & Enums (`entity/`, `constant/`)
Tạo các JPA Entities thuộc Aggregate Booking trong package `com.travel.booking.entity` & `constant`:
* `BookingStatus` (Enum): `PENDING`, `PAYMENT_PENDING`, `CONFIRMED`, `CANCELLED`.
* `PassengerType` (Enum): `ADULT`, `CHILD`, `INFANT`.
* `Booking` (Aggregate Root): `id` (UUID), `bookingCode` (Unique String), `userId` (UUID), `tourScheduleId` (UUID), `status` (Enum), `totalAmount` (BigDecimal), `contactName`, `contactEmail`, `contactPhone`, `createdAt`, `updatedAt`, `@Version Integer version` (Optimistic Locking chống xung đột ghi đồng thời).
* `BookingPassenger`: `id` (UUID), `bookingId` (FK), `fullName`, `dob`, `passengerType`, `idCardNumber`, `price`.
* `OutboxEventEntity`: Bảng `outbox_events` (`id`, `aggregateType`, `aggregateId`, `eventType`, `payload`, `status`, `createdAt`, `processedAt`) áp dụng Transactional Outbox Pattern.

---

### Bước 3: Định nghĩa DTO Request & Response ViewModel (`dto/`, `viewmodel/`)
Tuân thủ quy ước đặt tên chuẩn trong `architecture.md`:
* **Request DTOs (`dto/`):**
  * `CreateBookingRequest` (`tourScheduleId`, `contactName`, `contactEmail`, `contactPhone`, list `PassengerRequest` kèm `@NotBlank`, `@Email`, `@Valid`).
  * `PassengerRequest` (`fullName`, `dob`, `passengerType`, `idCardNumber`).
  * `CancelBookingRequest` (`reason`).
* **Response ViewModels (`viewmodel/`):**
  * `BookingVm`, `BookingDetailVm`, `BookingPassengerVm`.
* **Response Format:** Tất cả Controller trả về `ApiResponse<T>` từ `common-core`.

---

### Bước 4: Xây dựng Repositories & Domain Services (`repository/`, `service/`)
1. **Spring Data JPA Repositories (`repository/`):**
   * `BookingRepository`, `BookingPassengerRepository`, `OutboxEventRepository`.
2. **Domain Service State Machine (`BookingSagaStateDomainService`):**
   * Kiểm soát ma trận chuyển đổi trạng thái đơn hàng (Invariants: Chỉ chuyển từ `PENDING` $\rightarrow$ `PAYMENT_PENDING`, `PAYMENT_PENDING` $\rightarrow$ `CONFIRMED`, hoặc hủy $\rightarrow$ `CANCELLED`).
3. **Application Service (`BookingServiceImpl`):**
   * Logic tạo đơn: Sinh `bookingCode`, tính tổng tiền, mở `@Transactional` lưu nguyên tố `Booking` + `BookingPassenger` + `OutboxEventEntity` (`BookingCreatedEvent`).
   * Logic nghiệp vụ: Lấy danh sách booking của tôi (`findByUserId`), xem chi tiết booking, xử lý yêu cầu hủy.

---

### Bước 5: Xây dựng REST Controllers & API Endpoints (`controller/`)
Triển khai REST API theo spec trong `rest_api_design_spec.md`:
* `POST /api/v1/bookings`: Khách hàng khởi tạo đơn đặt tour (Saga Initiator).
* `GET /api/v1/bookings/my-bookings`: Tìm kiếm danh sách đơn đặt tour của Tourist hiện tại (lấy `X-User-Id` từ `UserHeaderFilter`).
* `GET /api/v1/bookings/{id}`: Lấy chi tiết đơn đặt tour.
* `POST /api/v1/bookings/{id}/cancel`: Yêu cầu hủy đơn đặt tour.

---

### Bước 6: Tích hợp Transactional Outbox & Kafka Event Flow (`publisher/`, `listener/`, `config/`)
Theo quy tắc Saga Pattern trong `architecture.md`:
1. **Phát Event qua Outbox (`publisher/`):**
   * Đọc bảng `outbox_events` qua `@Scheduled` Polling (`OutboxPublisherScheduler`) và phát `BookingCreatedEvent` / `BookingCancelledEvent` sang Kafka qua `common-kafka`.
2. **Tiêu thụ Event (Kafka Consumer - `listener/`):**
   * Consumes `TourSeatsReservedEvent` từ `tour-service`: Chuyển trạng thái Booking sang `PAYMENT_PENDING`.
   * Consumes `TourSeatsReservationFailedEvent` từ `tour-service`: Chuyển trạng thái Booking sang `CANCELLED`.
   * Consumes `PaymentProcessedEvent` từ `payment-service`: Chuyển trạng thái Booking sang `CONFIRMED`.
   * Consumes `PaymentFailedEvent` từ `payment-service`: Chuyển trạng thái Booking sang `CANCELLED` & ghi Outbox `BookingCancelledEvent` để nhả chỗ bên `tour-service`.
   * Xử lý **Idempotent Consumer** (Redis / Status Check) để tránh xử lý trùng tin.

---

### Bước 7: Kiểm thử & Xác minh (Verification)
1. **Unit Test:** Kiểm thử `BookingSagaStateDomainService` với xử lý ma trận chuyển đổi trạng thái và logic nghiệp vụ.
2. **Integration Test:** Chạy `booking-service` độc lập kết nối `db_travel_booking` & Kafka, test các API bằng Postman và kiểm tra dữ liệu Outbox/Kafka.
