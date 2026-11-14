# BÁO CÁO THIẾT KẾ REST API (REST API DESIGN SPECIFICATION)
## HỆ THỐNG TRAVEL MICROSERVICES PLATFORM

---

> [!NOTE]
> Báo cáo này được lập bởi **Senior Backend Developer**, dựa trên Kiến trúc [architecture.md](file:///e:/Travel_Management/architecture.md), BA Spec [business_analysis_spec.md](file:///C:/Users/Admin/.gemini/antigravity-ide/brain/8586db8d-201a-467f-96c9-218ddfdef14d/business_analysis_spec.md) và RAG AI Spec [rag_ai_design_spec.md](file:///e:/Travel_Management/rag_ai_design_spec.md). Tài liệu quy định chuẩn RESTful API cho **5 Microservices** (**100% thuần thiết kế OpenAPI & JSON Contracts, không chứa code triển khai**).

---

# I. CHUẨN ĐỊNH DẠNG RESPONSE TOÀN HỆ THỐNG

Tất cả các REST API Endpoint bắt buộc sử dụng định dạng bao đóng (Wrapper) `ApiResponse<T>` từ `common-core`:

### JSON Response Thành Công (HTTP 200 OK / 201 Created):
```json
{
  "success": true,
  "code": "OK",
  "message": "Thao tác thực hiện thành công",
  "data": { ... },
  "errors": null,
  "path": "/api/v1/bookings",
  "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "timestamp": "2026-08-04T22:30:00Z"
}
```

### JSON Response Lỗi (HTTP 400 / 401 / 403 / 404 / 409 / 500):
```json
{
  "success": false,
  "code": "ERR_BOOKING_SCHEDULE_FULL",
  "message": "Lịch khởi hành đã hết chỗ khả dụng",
  "data": null,
  "errors": [
    "availableSeats (0) không đủ cho số lượng vé yêu cầu (2)"
  ],
  "path": "/api/v1/bookings",
  "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "timestamp": "2026-08-04T22:30:00Z"
}
```

---

# II. THIẾT KẾ CHI TIẾT REST API CHO TỪNG SERVICE

---

## 1. AUTH SERVICE (`auth-service` - Port 8081)

---

### 1.1. API Đăng ký Tài khoản (User Registration)
* **Method:** `POST`
* **URL:** `/api/v1/auth/register`
* **Access Control:** Public (Không yêu cầu Token).
* **Headers:** `Content-Type: application/json`
* **Request Body Schema & Validations:**
  * `email` (String, `@NotBlank`, `@Email`): Email đăng ký.
  * `password` (String, `@NotBlank`, `@Size(min=8, max=32)`): Mật khẩu mạnh.
  * `fullName` (String, `@NotBlank`, `@Size(max=100)`): Họ và tên.
  * `phoneNumber` (String, `@Pattern(regexp="^\\d{10}$")`): Số điện thoại (10 chữ số).
  * `accountType` (Enum: `TOURIST`, `GUIDE`, `@NotNull`).
* **Business Rules:**
  * Nếu `accountType = TOURIST`: Trạng thái khởi tạo `ACTIVE`, phân quyền `ROLE_TOURIST`.
  * Nếu `accountType = GUIDE`: Trạng thái khởi tạo `PENDING_APPROVAL`, phân quyền `ROLE_GUIDE` (chờ Admin duyệt).
* **Status Codes:** `201 Created` (Thành công), `400 Bad Request` (Sai định dạng), `409 Conflict` (Email đã tồn tại).

#### OpenAPI Contract (YAML Snippet):
```yaml
/api/v1/auth/register:
  post:
    summary: Đăng ký tài khoản người dùng mới
    tags: [Auth Service]
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required: [email, password, fullName, accountType]
            properties:
              email: { type: string, format: email, example: "tourist@example.com" }
              password: { type: string, format: password, example: "Password123@" }
              fullName: { type: string, example: "Nguyễn Văn A" }
              phoneNumber: { type: string, example: "0901234567" }
              accountType: { type: string, enum: [TOURIST, GUIDE], example: "TOURIST" }
    responses:
      '201':
        description: Tài khoản khởi tạo thành công
```

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "email": "nguyenvana@gmail.com",
    "password": "Password123@",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0901234567",
    "accountType": "TOURIST"
  }
  ```
* **Response JSON (201 Created):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Đăng ký tài khoản thành công",
    "data": {
      "userId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "email": "nguyenvana@gmail.com",
      "fullName": "Nguyễn Văn A",
      "status": "ACTIVE",
      "roles": ["ROLE_TOURIST"]
    },
    "errors": null,
    "path": "/api/v1/auth/register",
    "traceId": "t-1001",
    "timestamp": "2026-08-04T22:30:00Z"
  }
  ```

---

### 1.2. API Đăng nhập & Cấp Token (User Login)
* **Method:** `POST`
* **URL:** `/api/v1/auth/login`
* **Access Control:** Public.
* **Request Body & Validations:** `username` (`@NotBlank`), `password` (`@NotBlank`).
* **Business Rules:** Xác thực credentials qua Keycloak Token Endpoint. Cấp JWT Access Token (TTL 30m) & Refresh Token (TTL 7d).
* **Status Codes:** `200 OK`, `401 Unauthorized` (Sai mật khẩu/Username hoặc tài khoản bị khóa).

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "username": "nguyenvana@gmail.com",
    "password": "Password123@"
  }
  ```
* **Response JSON (200 OK):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Đăng nhập thành công",
    "data": {
      "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "expiresIn": 1800
    },
    "errors": null,
    "path": "/api/v1/auth/login",
    "traceId": "t-1002",
    "timestamp": "2026-08-04T22:31:00Z"
  }
  ```

---

## 2. TOUR SERVICE (`tour-service` - Port 8082)

---

### 2.1. API Tạo Mới Tour (Create Tour Catalog)
* **Method:** `POST`
* **URL:** `/api/v1/tours`
* **Access Control:** Yêu cầu Token mang quyền `ROLE_ADMIN` (Kiểm tra qua Header Gateway `X-User-Roles`).
* **Headers:** `Authorization: Bearer <JWT>`, `X-User-Id`, `X-User-Roles`
* **Request Body & Validations:**
  * `title` (String, `@NotBlank`, `@Size(min=10, max=250)`): Tên tour.
  * `code` (String, `@NotBlank`, `@Pattern(regexp="^[A-Z0-9-]+$")`): Mã tour duy nhất.
  * `description` (String, `@NotBlank`): Mô tả chi tiết.
  * `itineraries` (List, `@NotEmpty`): Danh sách lịch trình theo ngày.
* **Status Codes:** `201 Created`, `400 Bad Request`, `403 Forbidden` (Không có quyền Admin), `409 Conflict` (Mã Tour trùng).

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "code": "TOUR-DN-3N2D",
    "title": "Tour Khám Phá Đà Nẵng - Ba Na Hills - Hội An 3N2Đ",
    "description": "Hành trình nghỉ dưỡng và khám phá danh thắng miền Trung tuyệt đẹp.",
    "itineraries": [
      { "dayNumber": 1, "title": "Bà Nà Hills - Cầu Vàng", "content": "Vui chơi tại Bà Nà Hills..." },
      { "dayNumber": 2, "title": "Phố Cổ Hội An", "content": "Tham quan phố cổ về đêm..." }
    ]
  }
  ```
* **Response JSON (201 Created):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Khởi tạo Tour thành công",
    "data": {
      "tourId": "f9a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c",
      "code": "TOUR-DN-3N2D",
      "title": "Tour Khám Phá Đà Nẵng - Ba Na Hills - Hội An 3N2Đ",
      "status": "DRAFT",
      "createdAt": "2026-08-04T22:35:00Z"
    },
    "errors": null,
    "path": "/api/v1/tours",
    "traceId": "t-1003",
    "timestamp": "2026-08-04T22:35:00Z"
  }
  ```

---

### 2.2. API Tìm Kiếm & Tra Cứu Danh Mục Tour (Tour Search)
* **Method:** `GET`
* **URL:** `/api/v1/tours`
* **Access Control:** Public.
* **Query Parameters:**
  * `keyword` (String, optional): Từ khóa tìm kiếm.
  * `destination` (String, optional): Thành phố điểm đến.
  * `minPrice`, `maxPrice` (BigDecimal, optional, `@PositiveOrZero`).
  * `page` (Integer, default 0), `size` (Integer, default 10).
* **Business Rules:** Chỉ hiển thị các Tour đang ở trạng thái `PUBLISHED` và có Lịch khởi hành khả dụng (`availableSeats > 0`).
* **Status Codes:** `200 OK`.

#### JSON Examples:
* **Response JSON (200 OK):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Lấy danh sách Tour thành công",
    "data": {
      "content": [
        {
          "tourId": "f9a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c",
          "code": "TOUR-DN-3N2D",
          "title": "Tour Khám Phá Đà Nẵng - Ba Na Hills - Hội An 3N2Đ",
          "minPrice": 4500000.00,
          "availableSeats": 15,
          "status": "PUBLISHED"
        }
      ],
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1
    },
    "errors": null,
    "path": "/api/v1/tours",
    "traceId": "t-1004",
    "timestamp": "2026-08-04T22:36:00Z"
  }
  ```

---

## 3. BOOKING SERVICE (`booking-service` - Port 8083)

---

### 3.1. API Tạo Đơn Đặt Tour (Create Booking - Saga Initiator)
* **Method:** `POST`
* **URL:** `/api/v1/bookings`
* **Access Control:** Khách hàng đã đăng nhập (`ROLE_TOURIST` hoặc `ROLE_GUIDE`).
* **Headers:** `Authorization: Bearer <JWT>`, `X-User-Id: <UUID>` (Gateway forward).
* **Request Body & Validations:**
  * `tourScheduleId` (UUID, `@NotNull`): ID Lịch khởi hành.
  * `passengers` (List, `@NotEmpty`, `@Valid`): Danh sách hành khách.
    * `fullName` (String, `@NotBlank`).
    * `passengerType` (Enum: `ADULT`, `CHILD`, `INFANT`, `@NotNull`).
    * `idCardNumber` (String, optional).
* **Business Rules:**
  1. Tạo Booking ở trạng thái `PENDING`.
  2. Ghi bản tin `BookingCreatedEvent` vào bảng `outbox` trong cùng DB Transaction.
  3. Đơn hàng chỉ có hiệu lực giữ chỗ trong 15 phút.
* **Status Codes:** `201 Created` (Thành công), `400 Bad Request` (Dữ liệu không hợp lệ), `401 Unauthorized`.

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "tourScheduleId": "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f",
    "passengers": [
      {
        "fullName": "Nguyễn Văn A",
        "passengerType": "ADULT",
        "idCardNumber": "040099123456"
      },
      {
        "fullName": "Nguyễn Thị B",
        "passengerType": "ADULT",
        "idCardNumber": "040099654321"
      }
    ]
  }
  ```
* **Response JSON (201 Created):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Đơn đặt tour đã được tạo thành công, đang giữ chỗ tạm thời",
    "data": {
      "bookingId": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
      "bookingCode": "BK20260804-X9A",
      "userId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "tourScheduleId": "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f",
      "totalAmount": 9000000.00,
      "status": "PENDING",
      "paymentDeadline": "2026-08-04T22:45:00Z",
      "createdAt": "2026-08-04T22:30:00Z"
    },
    "errors": null,
    "path": "/api/v1/bookings",
    "traceId": "t-1005",
    "timestamp": "2026-08-04T22:30:00Z"
  }
  ```

---

### 3.2. API Yêu cầu Hủy Đơn Đặt Tour (Cancel Booking)
* **Method:** `POST`
* **URL:** `/api/v1/bookings/{id}/cancel`
* **Access Control:** Người sở hữu đơn hàng (`X-User-Id` khớp) hoặc `ROLE_ADMIN`.
* **Path Variable:** `id` (UUID - BookingId).
* **Request Body:** `reason` (String, `@NotBlank`).
* **Business Rules:** Cập nhật trạng thái Booking $\rightarrow$ `CANCELLED`, ghi Event `BookingCancelledEvent` vào Outbox để `tour-service` hoàn lại chỗ.
* **Status Codes:** `200 OK`, `400 Bad Request` (Đã qua thời gian hủy), `404 Not Found`.

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "reason": "Thay đổi kế hoạch cá nhân đột xuất"
  }
  ```
* **Response JSON (200 OK):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Hủy đơn đặt tour thành công",
    "data": {
      "bookingId": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
      "bookingCode": "BK20260804-X9A",
      "status": "CANCELLED",
      "cancelledAt": "2026-08-04T22:35:00Z"
    },
    "errors": null,
    "path": "/api/v1/bookings/b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e/cancel",
    "traceId": "t-1006",
    "timestamp": "2026-08-04T22:35:00Z"
  }
  ```

---

## 4. PAYMENT SERVICE (`payment-service` - Port 8084)

---

### 4.1. API Khởi Tạo Liên Kết Thanh Toán (Create Payment URL)
* **Method:** `POST`
* **URL:** `/api/v1/payments/create-url`
* **Access Control:** Khách hàng sở hữu đơn hàng.
* **Request Body & Validations:**
  * `bookingId` (UUID, `@NotNull`): ID Đơn hàng.
  * `paymentMethod` (Enum: `VNPAY`, `MOMO`, `STRIPE`, `@NotNull`).
* **Business Rules:** Tạo bản ghi Payment `PENDING` và sinh mã chữ ký HASH HMACSHA512 kèm URL chuyển hướng thanh toán VNPAY/Stripe.
* **Status Codes:** `200 OK`, `400 Bad Request`, `404 Not Found` (Booking không tồn tại).

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "bookingId": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
    "paymentMethod": "VNPAY"
  }
  ```
* **Response JSON (200 OK):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "Khởi tạo liên kết thanh toán thành công",
    "data": {
      "paymentId": "p1e2f3a4-b5c6-7d8e-9f0a-1b2c3d4e5f6a",
      "bookingId": "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e",
      "amount": 9000000.00,
      "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=900000000&vnp_Command=pay&vnp_CreateDate=20260804223000&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_Merchant=TRAVELAPP&vnp_OrderInfo=Thanh+toan+don+hang+BK20260804-X9A&vnp_OrderType=other&vnp_ReturnUrl=https%3A%2F%2Fapp.travel.com%2Fpayment-callback&vnp_TmnCode=TRAVEL01&vnp_TxnRef=VNP20260804-991&vnp_Version=2.1.0&vnp_SecureHash=8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b"
    },
    "errors": null,
    "path": "/api/v1/payments/create-url",
    "traceId": "t-1007",
    "timestamp": "2026-08-04T22:30:00Z"
  }
  ```

---

### 4.2. API Callback Tiếp Nhận Webhook IPN Thanh Toán (VNPAY IPN Webhook)
* **Method:** `GET` hoặc `POST`
* **URL:** `/api/v1/payments/vnpay-callback`
* **Access Control:** Public (Gọi trực tiếp từ VNPAY Server).
* **Query Parameters:** Các tham số IPN (`vnp_ResponseCode`, `vnp_TxnRef`, `vnp_SecureHash`, `vnp_Amount`...).
* **Business Rules:**
  1. Kiểm tra chữ ký `vnp_SecureHash` HMACSHA512.
  2. Nếu `vnp_ResponseCode = "00"`: Đổi Payment status $\rightarrow$ `SUCCESS`, phát `PaymentProcessedEvent` sang Kafka qua Outbox Table.
  3. Trả về đúng JSON theo quy định của VNPAY Gateway.
* **Status Codes:** `200 OK`.

#### JSON Examples:
* **Response JSON (Dành riêng cho VNPAY Server Ack):**
  ```json
  {
    "RspCode": "00",
    "Message": "Confirm Success"
  }
  ```

---

## 5. AI SERVICE (`ai-service` - Port 8085)

---

### 5.1. API AI Gợi Ý Lịch Trình Du Lịch Smart (Smart RAG Recommendation)
* **Method:** `POST`
* **URL:** `/api/v1/ai/recommendations`
* **Access Control:** Public / Authenticated.
* **Request Body & Validations:**
  * `promptText` (String, `@NotBlank`, `@Size(max=1000)`): Câu hỏi / Nhu cầu của khách hàng.
* **Business Rules:**
  1. Chuyển `promptText` thành Vector 1536 chiều.
  2. Kiểm tra Semantic Cache trên Redis (`Similarity >= 0.95`).
  3. Nếu không trúng Cache: Tìm kiếm HNSW Cosine Similarity Search trên `pgvector`, ghép Anti-Hallucination Prompt Template gửi LLM.
  4. Trả về JSON định dạng `TripRecommendationVm` kèm link đặt tour thật.
* **Status Codes:** `200 OK`, `400 Bad Request`, `429 Too Many Requests` (Quá hạn mức gọi AI).

#### OpenAPI Contract (YAML Snippet):
```yaml
/api/v1/ai/recommendations:
  post:
    summary: AI Tư vấn & Gợi ý lịch trình du lịch thông minh (RAG + pgvector)
    tags: [AI Service]
    requestBody:
      required: true
      content:
        application/json:
          schema:
            type: object
            required: [promptText]
            properties:
              promptText:
                type: string
                example: "Tôi muốn đi du lịch 3 ngày 2 đêm tại Nha Trang cho gia đình 4 người, ngân sách 10 triệu"
    responses:
      '200':
        description: Bài tư vấn và lịch trình gợi ý định dạng JSON cấu trúc
```

#### JSON Examples:
* **Request JSON:**
  ```json
  {
    "promptText": "Tôi muốn đi du lịch 3 ngày 2 đêm tại Đà Nẵng cho gia đình có con nhỏ, ngân sách dưới 10 triệu VNĐ"
  }
  ```
* **Response JSON (200 OK):**
  ```json
  {
    "success": true,
    "code": "OK",
    "message": "AI đã khởi tạo thành công bài tư vấn lịch trình",
    "data": {
      "introduction": "Chào bạn! Dựa trên nhu cầu du lịch nghỉ dưỡng 3N2Đ tại Đà Nẵng dành cho gia đình có con nhỏ, tôi xin gợi ý Tour khám phá Bà Nà Hills - Phố cổ Hội An với chi phí cực kỳ tối ưu.",
      "recommendedTours": [
        {
          "tourId": "f9a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c",
          "code": "TOUR-DN-3N2D",
          "title": "Tour Khám Phá Đà Nẵng - Ba Na Hills - Hội An 3N2Đ",
          "pricePerAdult": 4500000.00,
          "bookingUrl": "/tours/f9a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c"
        }
      ],
      "detailedItinerary": [
        {
          "day": 1,
          "title": "Đón sân bay - Vui chơi Bà Nà Hills & Cầu Vàng",
          "description": "Đoàn di chuyển cáp treo lên Bà Nà Hills, tham quan Cầu Vàng và cho bé vui chơi tại Công viên Fantasy Park."
        },
        {
          "day": 2,
          "title": "Tắm biển Mỹ Khê - Tham quan Phố cổ Hội An về đêm",
          "description": "Buổi sáng tự do tắm biển, buổi chiều di chuyển vào Hội An ngắm đèn lồng và đi thuyền thả hoa đăng."
        },
        {
          "day": 3,
          "title": "Mua sắm chợ Hàn - Tiễn sân bay",
          "description": "Mua sắm đặc sản Chợ Hàn và kết thúc chuyến đi."
        }
      ],
      "cached": false
    },
    "errors": null,
    "path": "/api/v1/ai/recommendations",
    "traceId": "t-1008",
    "timestamp": "2026-08-04T22:30:00Z"
  }
  ```

---

# III. BẢNG TỔNG HỢP DANH SÁCH REST API MA TRẬN HỆ THỐNG

| Service | Method | Route Endpoint | Access Control | Mục đích Nghiệp vụ |
| :--- | :--- | :--- | :--- | :--- |
| **auth-service** | `POST` | `/api/v1/auth/register` | Public | Đăng ký tài khoản Tourist/Guide. |
| **auth-service** | `POST` | `/api/v1/auth/login` | Public | Đăng nhập Keycloak cấp cặp JWT Token. |
| **auth-service** | `GET` | `/api/v1/auth/me` | Authenticated | Lấy thông tin Hồ sơ người dùng hiện tại. |
| **tour-service** | `POST` | `/api/v1/tours` | Admin | Tạo mới danh mục Tour. |
| **tour-service** | `GET` | `/api/v1/tours` | Public | Tìm kiếm & Lọc Tour công khai. |
| **tour-service** | `GET` | `/api/v1/tours/{id}` | Public | Xem chi tiết thông tin Tour & Lịch đi. |
| **tour-service** | `POST` | `/api/v1/tours/{id}/schedules` | Admin | Thêm Lịch khởi hành & Hạn mức số chỗ. |
| **booking-service** | `POST` | `/api/v1/bookings` | Tourist | Tạo đơn đặt tour (Khởi xướng Saga Event). |
| **booking-service** | `GET` | `/api/v1/bookings/my-bookings` | Tourist | Danh sách đơn đặt tour của tôi. |
| **booking-service** | `POST` | `/api/v1/bookings/{id}/cancel` | Owner / Admin | Hủy đơn đặt tour và hoàn lại chỗ. |
| **payment-service** | `POST` | `/api/v1/payments/create-url` | Tourist | Tạo liên kết thanh toán VNPAY/Stripe. |
| **payment-service** | `GET/POST` | `/api/v1/payments/vnpay-callback` | Webhook IPN | Tiếp nhận kết quả thanh toán VNPAY. |
| **payment-service** | `POST` | `/api/v1/payments/{id}/refund` | Admin | Yêu cầu hoàn tiền giao dịch. |
| **ai-service** | `POST` | `/api/v1/ai/recommendations` | Public / Tourist | AI Tư vấn Lịch trình Smart (RAG + pgvector). |
| **ai-service** | `POST` | `/api/v1/ai/chat` | Tourist | AI Chatbot tư vấn chính sách 24/7. |
