# BÁO CÁO THIẾT KẾ MÔ HÌNH MIỀN (DOMAIN-DRIVEN DESIGN SPECIFICATION)
## HỆ THỐNG TRAVEL MICROSERVICES PLATFORM

---

> [!NOTE]
> Báo cáo này được lập bởi **Domain-Driven Design (DDD) Expert**, dựa trên các Quy tắc Nghiệp vụ từ [business_analysis_spec.md](file:///C:/Users/Admin/.gemini/antigravity-ide/brain/8586db8d-201a-467f-96c9-218ddfdef14d/business_analysis_spec.md) và Kiến trúc Hệ thống [architecture.md](file:///e:/Travel_Management/architecture.md). Tài liệu chuẩn hóa thiết kế **Tactical DDD (Domain Model)** cho từng Bounded Context (**100% thuần khái niệm thiết kế, không chứa code**).

---

# I. TỔNG QUAN NGUYÊN TẮC THIẾT KẾ DDD HỆ THỐNG

1. **Bounded Context Cô lập:** Mỗi Microservice quản lý một Bounded Context riêng biệt với ngôn ngữ chung (Ubiquitous Language) và Cơ sở dữ liệu riêng.
2. **Aggregate Boundary (Ranh giới Aggregate):** Đảm bảo tính bất biến (Invariants) dữ liệu bên trong Aggregate. Mọi truy cập vào các Entity con đều phải đi qua Aggregate Root.
3. **Value Objects Bất biến (Immutable):** Sử dụng Value Objects để đại diện cho các khái niệm giá trị (Email, Money, Address, SeatAvailability) không có tính định danh riêng.
4. **Loại bỏ Liên kết Chéo Entity:** Giữa các Bounded Context, các Aggregate Root chỉ tham chiếu nhau bằng Identity (ID dạng Value Object), không chứa tham chiếu trực tiếp đến đối tượng của nhau.

---

# II. THIẾT KẾ DOMAIN MODEL CHO TỪNG MICROSERVICE

---

## 1. BOUNDED CONTEXT: AUTHENTICATION & USER MANAGEMENT (`auth-service`)

### 1.1. Các Thành Phần Domain Model

* **Aggregate Root:**
  * `User`: Quản lý toàn bộ danh tính, trạng thái tài khoản và phân quyền của người dùng trong hệ thống.

* **Entities (Thực thể thuộc Aggregate):**
  * `UserProfile`: Chứa thông tin chi tiết cá nhân (Họ tên, ngày sinh, địa chỉ, avatar URL, SĐT liên hệ khẩn cấp). Sở hữu ID riêng nhưng vòng đời gắn liền với `User`.

* **Value Objects (Bản thể Giá trị):**
  * `UserId`: Định danh duy nhất (UUID).
  * `Email`: Email hợp lệ (chuẩn hóa RFC 5322).
  * `PhoneNumber`: Số điện thoại hợp lệ.
  * `UserRole`: Enum chứa các quyền (`ROLE_TOURIST`, `ROLE_GUIDE`, `ROLE_ADMIN`).
  * `AccountStatus`: Enum trạng thái (`PENDING_APPROVAL`, `ACTIVE`, `BLOCKED`).

* **Repository (Giao diện Lưu trữ):**
  * `UserRepository`: Giao diện nạp/lưu Aggregate Root `User` (`findByEmail`, `findById`, `save`).

* **Domain Service:**
  * `UserRegistrationDomainService`: Xử lý logic nghiệp vụ đăng ký liên-thực thể: Kiểm tra trùng lặp Email/Số điện thoại trên toàn hệ thống và quyết định trạng thái tài khoản ban đầu dựa trên `UserRole`.

* **Factory:**
  * `UserFactory`: Khởi tạo Aggregate Root `User` cùng `UserProfile` mặc định với trạng thái hợp lệ.

* **Domain Events:**
  * `UserRegisteredDomainEvent`: Bắn ra khi User đăng ký thành công.
  * `UserProfileUpdatedDomainEvent`: Bắn ra khi thông tin Profile thay đổi.

### 1.2. Giải Thích Lý Do Phân Chia (DDD Rationale)
* **Vì sao `User` là Aggregate Root duy nhất?** `UserProfile` không thể tồn tại độc lập mà không có `User`. Mọi thao tác sửa đổi thông tin cá nhân hay thay đổi trạng thái tài khoản (ví dụ: bị Khóa/Block) phải được kiểm soát qua `User` để đảm bảo quy tắc phân quyền và tính nhất quán dữ liệu.

---

## 2. BOUNDED CONTEXT: TOUR CATALOG & SCHEDULE (`tour-service`)

### 2.1. Các Thành Phần Domain Model

* **Aggregate Root:**
  * `Tour`: Quản lý thông tin chi tiết Tour du lịch, quy định danh mục và giữ tính nhất quán cho các chuyến đi.

* **Entities (Thực thể thuộc Aggregate):**
  * `TourSchedule`: Lịch khởi hành cụ thể của chuyến đi (Ngày đi, Ngày về, Bảng giá, Hạn hạn số chỗ).
  * `Itinerary`: Lịch trình chi tiết từng ngày (Ngày 1, Ngày 2, Điểm tham quan, Bữa ăn).
  * `Destination`: Điểm đến du lịch (Tên thành phố, Quốc gia, Mô tả điểm đến).

* **Value Objects (Bản thể Giá trị):**
  * `TourId`, `ScheduleId`, `DestinationId`: UUID định danh.
  * `Money`: Số tiền và Đơn vị tiền tệ (Ví dụ: `5,000,000 VND`).
  * `SeatAvailability`: Quản lý tổng số chỗ (`totalSeats`) và số chỗ khả dụng (`availableSeats`).
  * `TourStatus`: Trạng thái Tour (`DRAFT`, `PUBLISHED`, `ARCHIVED`).

* **Repository (Giao diện Lưu trữ):**
  * `TourRepository`: Nạp/lưu Aggregate Root `Tour` cùng các Schedules và Itineraries con.

* **Domain Service:**
  * `SeatAllocationDomainService`: Xử lý logic trừ chỗ / hoàn chỗ phức tạp: Kiểm tra điều kiện `availableSeats >= requestedSeats`, áp dụng cơ chế khóa chống xung đột (Race Condition) và thực hiện trừ chỗ nguyên tố.

* **Factory:**
  * `TourFactory`: Dựng Aggregate Root `Tour` phức tạp bao gồm việc gắn danh sách các đối tượng `Itinerary` theo thứ tự ngày và các `TourSchedule` khởi tạo.

* **Domain Events:**
  * `TourUpdatedDomainEvent`: Bắn ra khi Tour/Lịch trình được tạo hoặc sửa đổi (được tiêu thụ bởi `ai-service` để tự động báo chế Vector Embedding).
  * `TourSeatsReservedDomainEvent`: Phát ra khi trừ chỗ thành công cho đơn đặt tour.
  * `TourSeatsReservationFailedDomainEvent`: Phát ra khi không đủ chỗ trống.
  * `TourSeatsReleasedDomainEvent`: Phát ra khi hoàn lại chỗ (Compensating Transaction).

### 2.2. Giải Thích Lý Do Phân Chia (DDD Rationale)
* **Vì sao `TourSchedule` nằm trong Aggregate `Tour`?** Việc giữ chỗ (`SeatAvailability`) thuộc về từng chuyến đi cụ thể, nhưng tính hợp lệ của chuyến đi lại phụ thuộc vào quy định tổng thể của `Tour` (trạng thái `PUBLISHED`, chính sách giá). Đưa `TourSchedule` vào trong Aggregate `Tour` giúp bảo vệ quy tắc: "Không được mở bán hoặc trừ chỗ chuyến đi của một Tour đang bị Ẩn/DRAFT".

---

## 3. BOUNDED CONTEXT: BOOKING & SAGA MANAGEMENT (`booking-service`)

### 3.1. Các Thành Phần Domain Model

* **Aggregate Root:**
  * `Booking`: Quản lý đơn đặt tour, trạng thái đơn hàng và điều phối quy trình Saga.

* **Entities (Thực thể thuộc Aggregate):**
  * `BookingPassenger`: Thông tin hành khách tham gia chuyến đi (Họ tên, Ngày sinh, Loại vé, CCCD/Passport).

* **Value Objects (Bản thể Giá trị):**
  * `BookingId`, `TourScheduleId`, `UserId`: ID định danh dạng Value Object.
  * `PassengerInfo`: Thông tin chi tiết cá nhân hành khách.
  * `BookingStatus`: Trạng thái vòng đời Booking (`PENDING`, `SEATS_RESERVED`, `PAYMENT_PENDING`, `CONFIRMED`, `CANCELLED`).
  * `OutboxMessage`: Payload sự kiện được ghi vào bảng Outbox trong cùng DB Transaction.

* **Repository (Giao diện Lưu trữ):**
  * `BookingRepository`: Nạp/Lưu Aggregate Root `Booking` kèm danh sách `BookingPassenger` và trạng thái Outbox.

* **Domain Service:**
  * `BookingSagaStateDomainService`: Kiểm soát Ma trận chuyển đổi trạng thái đơn hàng (State Machine Invariants). Đảm bảo các quy tắc nghiêm ngặt như: "Đơn hàng chỉ được chuyển sang `CONFIRMED` khi đã ở trạng thái `PAYMENT_PENDING` và nhận được sự kiện thanh toán thành công".

* **Factory:**
  * `BookingFactory`: Khởi tạo Aggregate Root `Booking` từ yêu cầu của khách hàng, tự động tính tổng tiền dựa trên số lượng hành khách và khởi tạo sự kiện `BookingCreated`.

* **Domain Events:**
  * `BookingCreatedDomainEvent`: Khởi tạo luồng Saga.
  * `BookingConfirmedDomainEvent`: Xác nhận đơn hàng thành công.
  * `BookingCancelledDomainEvent`: Hủy đơn hàng.

### 3.2. Giải Thích Lý Do Phân Chia (DDD Rationale)
* **Vì sao `Booking` không chứa đối tượng `Tour` hay `User`?** Trong DDD, các Bounded Context độc lập chỉ tham chiếu nhau qua Identity (`UserId`, `TourScheduleId`). `Booking` không cần sở hữu dữ liệu chi tiết của Tour hay User, giúp giảm sự phụ thuộc dữ liệu và đảm bảo hiệu năng tối đa khi ghi đơn hàng.

---

## 4. BOUNDED CONTEXT: PAYMENT & TRANSACTIONS (`payment-service`)

### 4.1. Các Thành Phần Domain Model

* **Aggregate Root:**
  * `Payment`: Quản lý thông tin thanh toán cho đơn hàng và kết quả giao dịch từ cổng thanh toán thứ 3.

* **Entities (Thực thể thuộc Aggregate):**
  * `PaymentTransaction`: Nhật ký ghi lại từng lần tương tác giao dịch (Gọi khởi tạo URL, Nhận IPN Webhook, Yêu cầu Refund).

* **Value Objects (Bản thể Giá trị):**
  * `PaymentId`, `BookingId`: UUID định danh.
  * `PaymentMethod`: Enum cổng thanh toán (`VNPAY`, `MOMO`, `STRIPE`).
  * `TransactionRef`: Mã tham chiếu giao dịch phía Cổng thanh toán (ví dụ: `vnp_TxnRef`).
  * `PaymentStatus`: Trạng thái thanh toán (`PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`).

* **Repository (Giao diện Lưu trữ):**
  * `PaymentRepository`: Lưu vết và truy vấn Aggregate Root `Payment`.

* **Domain Service:**
  * `PaymentVerificationDomainService`: Kiểm tra tính toàn vẹn và xác thực chữ ký số (Checksum HMACSHA512), đối soát số tiền nhận được với số tiền cần thanh toán của đơn hàng.

* **Factory:**
  * `PaymentFactory`: Tạo lập đối tượng `Payment` mới kèm theo giao dịch khởi tạo ban đầu `PaymentTransaction`.

* **Domain Events:**
  * `PaymentProcessedDomainEvent`: Phát ra khi thanh toán IPN thành công.
  * `PaymentFailedDomainEvent`: Phát ra khi thanh toán thất bại hoặc bị hủy.
  * `PaymentRefundedDomainEvent`: Phát ra khi hoàn tiền thành công.

### 4.2. Giải Thích Lý Do Phân Chia (DDD Rationale)
* **Vì sao `Payment` là một Bounded Context riêng biệt với `Booking`?** Giao dịch tài chính có vòng đời và các quy tắc kiểm soát khác hoàn toàn với Đặt tour. Tách biệt `Payment` giúp hệ thống dễ dàng tích hợp thêm các Cổng thanh toán mới (như ZaloPay, PayOS) hoặc thay đổi quy trình đối soát tài chính mà không ảnh hưởng tới logic quản lý Đơn hàng bên `booking-service`.

---

## 5. BOUNDED CONTEXT: AI TRAVEL ASSISTANT (`ai-service`)

### 5.1. Các Thành Phần Domain Model

* **Aggregate Root:**
  * `TripRecommendation`: Quản lý bài tư vấn lịch trình du lịch thông minh được sinh ra từ AI.
  * `ChatSession`: Quản lý hội thoại tư vấn giữa khách hàng và AI Chatbot.
  * `TourEmbedding`: Quản lý kho dữ liệu Vector Embedding phục vụ RAG Semantic Search.

* **Entities (Thực thể thuộc Aggregate):**
  * `ChatMessage`: Nội dung câu hỏi của người dùng và câu phản hồi của AI Chatbot theo thứ tự thời gian.
  * `TourEmbeddingChunk`: Đoạn văn bản phân đoạn (Metadata Chunk hoặc Daily Itinerary Chunk) kèm mảng Vector 1536 chiều.

* **Value Objects (Bản thể Giá trị):**
  * `RecommendationId`, `SessionId`, `ChunkId`: UUID định danh.
  * `VectorEmbedding`: Mảng 1536 chiều đại diện cho tọa độ không gian ngữ nghĩa (`vector(1536)`).
  * `SimilarityScore`: Điểm độ tương đồng Cosine giữa yêu cầu người dùng và kho dữ liệu Tour.
  * `PromptContext`: Ngữ cảnh dữ liệu được chuẩn bị để gửi cho LLM.
  * `SemanticCacheKey`: Key lưu cặp Query Vector - LLM Result trên Redis Cache.

* **Repository (Giao diện Lưu trữ):**
  * `TripRecommendationRepository`: Lưu trữ bài gợi ý lịch trình.
  * `ChatSessionRepository`: Lưu vết lịch sử trò chuyện.
  * `TourEmbeddingRepository`: Thao tác Vector Cosine Search trên `pgvector` HNSW index.

* **Domain Service:**
  * `RAGRetrievalDomainService`: Thực hiện quy trình RAG 3 bước: Nhận yêu cầu $\rightarrow$ Check Semantic Cache $\rightarrow$ Truy vấn HNSW Vector DB + Re-Ranker $\rightarrow$ Dựng Prompt Anti-Hallucination gửi LLM.

* **Factory:**
  * `TripRecommendationFactory`: Khởi tạo Aggregate `TripRecommendation` từ kết quả phản hồi định dạng JSON của LLM.

* **Domain Events:**
  * `RecommendationGeneratedDomainEvent`: Phát ra khi tạo thành công một bài tư vấn lịch trình.

### 5.2. Giải Thích Lý Do Phân Chia (DDD Rationale)
* **Vì sao `ai-service` tách riêng Aggregate `TripRecommendation` và `ChatSession`?** `TripRecommendation` là kết quả tư vấn dùng một lần hoặc có thể chia sẻ (Shareable Itinerary), trong khi `ChatSession` là chuỗi hội thoại có trạng thái (Stateful Conversation). Tách riêng giúp tối ưu hóa việc lưu trữ Vector Embedding và lịch sử chat mà không làm phình to dữ liệu miền.

---

# III. TỔNG KẾT BẢNG MÔ HÌNH DDD TOÀN HỆ THỐNG

| Bounded Context | Aggregate Root | Entities | Value Objects | Key Domain Services | Domain Events |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **auth-service** | `User` | `UserProfile` | `UserId`, `Email`, `UserRole`, `AccountStatus` | `UserRegistrationDomainService` | `UserRegisteredDomainEvent`, `UserProfileUpdatedDomainEvent` |
| **tour-service** | `Tour` | `TourSchedule`, `Itinerary`, `Destination` | `TourId`, `ScheduleId`, `Money`, `SeatAvailability` | `SeatAllocationDomainService` | `TourSeatsReservedDomainEvent`, `TourSeatsReservationFailedDomainEvent`, `TourSeatsReleasedDomainEvent` |
| **booking-service** | `Booking` | `BookingPassenger` | `BookingId`, `PassengerInfo`, `BookingStatus`, `OutboxMessage` | `BookingSagaStateDomainService` | `BookingCreatedDomainEvent`, `BookingConfirmedDomainEvent`, `BookingCancelledDomainEvent` |
| **payment-service** | `Payment` | `PaymentTransaction` | `PaymentId`, `PaymentMethod`, `TransactionRef`, `PaymentStatus` | `PaymentVerificationDomainService` | `PaymentProcessedDomainEvent`, `PaymentFailedDomainEvent`, `PaymentRefundedDomainEvent` |
| **ai-service** | `TripRecommendation`, `ChatSession` | `ChatMessage` | `VectorEmbedding`, `SimilarityScore`, `PromptContext` | `RAGRetrievalDomainService` | `RecommendationGeneratedDomainEvent` |
