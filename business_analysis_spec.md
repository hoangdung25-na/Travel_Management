# BÁO CÁO PHÂN TÍCH NGHIỆP VỤ (BUSINESS ANALYSIS SPECIFICATION)
## HỆ THỐNG TRAVEL MICROSERVICES PLATFORM

---

> [!NOTE]
> Báo cáo này được lập bởi **Business Analyst**, dựa trên kiến trúc hệ thống tại [architecture.md](file:///e:/Travel_Management/architecture.md). Tất cả các quy trình nghiệp vụ được chuẩn hóa đầy đủ theo 9 thành phần tiêu chuẩn cho từng chức năng của từng Microservice.

---

# I. AUTH SERVICE (auth-service)
**Nhiệm vụ:** Quản lý tài khoản, hồ sơ người dùng (Tourist, Tour Guide, Admin), xác thực và phân quyền hệ thống.

---

### 1.1. Chức năng: Đăng ký Tài khoản (User Registration)
* **Actor:** Du khách (Tourist), Hướng dẫn viên (Tour Guide).
* **Business Goal:** Cho phép người dùng mới tạo tài khoản để truy cập và sử dụng dịch vụ trên nền tảng.
* **Preconditions:** Người dùng chưa đăng nhập, chưa có tài khoản trùng Email/Username trong hệ thống.
* **Main Flow:**
  1. Người dùng nhập thông tin đăng ký (Họ tên, Email, Số điện thoại, Mật khẩu, Loại tài khoản).
  2. Hệ thống kiểm tra tính hợp lệ và sự tồn tại của Email/Số điện thoại.
  3. Hệ thống gọi `common-keycloak` để khởi tạo User trên Keycloak Server.
  4. Hệ thống lưu bản ghi User Profile tương ứng vào `db_travel_auth`.
  5. Hệ thống gửi email xác thực tài khoản và trả về thông báo thành công.
* **Alternative Flow:**
  * *AF1 (Đăng ký qua Social Login - Google/Facebook):* Người dùng chọn đăng ký nhanh bằng Google -> Hệ thống xác thực qua Keycloak Identity Provider -> Lưu User Profile tự động.
* **Validation:**
  * Email phải đúng định dạng RFC 5322.
  * Mật khẩu tối thiểu 8 ký tự, gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt.
  * Số điện thoại tuân theo định dạng chuẩn quốc tế hoặc Việt Nam (10 chữ số).
* **Business Rule:**
  * Mặc định phân quyền `ROLE_TOURIST` cho đăng ký thông thường.
  * Đăng ký tài khoản `ROLE_GUIDE` phải ở trạng thái `PENDING_APPROVAL` cho đến khi Admin duyệt hồ sơ năng lực.
* **Error Case:**
  * `ERR_AUTH_EMAIL_EXISTS`: Email đã được đăng ký trước đó.
  * `ERR_KEYCLOAK_UNAVAILABLE`: Dịch vụ Keycloak không phản hồi.
* **Output:** Tài khoản được tạo thành công, ApiResponse trả về `UserVm` (UserId, Email, Status).

---

### 1.2. Chức năng: Đăng nhập & Cấp Token (User Authentication)
* **Actor:** Tất cả người dùng (Tourist, Tour Guide, Admin).
* **Business Goal:** Xác thực danh tính người dùng và cấp phát JWT Token bảo mật để truy cập hệ thống.
* **Preconditions:** Tài khoản đã đăng ký và ở trạng thái `ACTIVE`.
* **Main Flow:**
  1. Người dùng gửi Username/Email và Password lên API Gateway (`/api/v1/auth/login`).
  2. Gateway chuyển tiếp đến `auth-service`.
  3. `auth-service` gửi request xác thực tới Keycloak token endpoint.
  4. Keycloak kiểm tra credentials và cấp cặp JWT `AccessToken` & `RefreshToken`.
  5. `auth-service` trả Access Token và Refresh Token về cho Client.
* **Alternative Flow:**
  * *AF1 (Làm mới Token - Refresh Token):* Access Token hết hạn -> Client gửi Refresh Token -> `auth-service` yêu cầu Keycloak cấp Access Token mới mà không cần nhập lại mật khẩu.
* **Validation:**
  * Username và Password không được để trống.
* **Business Rule:**
  * Access Token có thời hạn (TTL) là 30 phút, Refresh Token có TTL là 7 ngày.
  * Tài khoản bị khóa (BLOCKED) không thể đăng nhập.
  * Nhập sai mật khẩu quá 5 lần liên tiếp sẽ tự động khóa tài khoản tạm thời trong 15 phút.
* **Error Case:**
  * `ERR_AUTH_INVALID_CREDENTIALS`: Mật khẩu hoặc Tên đăng nhập không chính xác.
  * `ERR_AUTH_ACCOUNT_LOCKED`: Tài khoản đang bị khóa.
* **Output:** Cặp JWT Access Token, Refresh Token và thông tin cơ bản của User (`AuthTokenVm`).

---

### 1.3. Chức năng: Quản lý Hồ sơ Cá nhân (User Profile Management)
* **Actor:** Người dùng đã đăng nhập (Tourist, Tour Guide).
* **Business Goal:** Cho phép người dùng xem và cập nhật thông tin cá nhân (Avatar, Ngày sinh, Địa chỉ, SĐT liên hệ khẩn cấp).
* **Preconditions:** Người dùng đã xác thực thành công (có Token hợp lệ).
* **Main Flow:**
  1. Người dùng gửi yêu cầu cập nhật thông tin hồ sơ kèm file Avatar (nếu có).
  2. Nếu có Avatar, `auth-service` gọi `common-storage` để upload ảnh lên MinIO/S3.
  3. `auth-service` cập nhật dữ liệu trong `db_travel_auth`.
  4. Trả về thông tin hồ sơ đã cập nhật.
* **Alternative Flow:** Không.
* **Validation:**
  * File Avatar không vượt quá 5MB, chỉ chấp nhận định dạng JPG, PNG, WEBP.
* **Business Rule:**
  * Người dùng không được tự thay đổi Email và Role (phải thông qua quy trình riêng).
* **Error Case:**
  * `ERR_STORAGE_UPLOAD_FAILED`: Lỗi upload hình ảnh lên S3/MinIO.
* **Output:** Thông tin hồ sơ mới (`UserProfileVm`).

---

### 💡 ĐỀ XUẤT NGHIỆP VỤ BỔ SUNG CHO AUTH SERVICE:
1. **Chức năng Khôi phục Mật khẩu (Password Reset / Forgot Password):** Cho phép gửi OTP/Magic Link qua Email để đặt lại mật khẩu khi quên.
2. **Chức năng Quản lý Phiên đăng nhập (Session Management / Revoke Token):** Cho phép người dùng đăng xuất khỏi tất cả các thiết bị từ xa.
3. **Quy trình Phê duyệt Hướng dẫn viên (Tour Guide Verification Workflow):** Admin tải và thẩm định bằng cấp/thẻ hướng dẫn viên du lịch trước khi kích hoạt `ROLE_GUIDE`.

---

# II. TOUR SERVICE (tour-service)
**Nhiệm vụ:** Quản lý danh mục Tour, điểm đến, lịch trình chi tiết, bảng giá, số lượng chỗ còn trống và giữ chỗ cho luồng Saga.

---

### 2.1. Chức năng: Tạo mới & Cập nhật Tour (Tour Catalog Management)
* **Actor:** Admin, Quản trị viên du lịch.
* **Business Goal:** Thêm mới hoặc cập nhật thông tin danh mục Tour du lịch (Tên tour, mô tả, danh sách điểm đến, lịch trình theo ngày, gallery ảnh).
* **Preconditions:** Actor có quyền `ROLE_ADMIN`.
* **Main Flow:**
  1. Admin gửi thông tin cấu hình Tour và danh sách các điểm đến (Destinations), lịch trình (Itineraries).
  2. Hệ thống kiểm tra thông tin dữ liệu đầu vào.
  3. Lưu thông tin Tour (Aggregate Root) và các thực thể con vào `db_travel_tour`.
  4. Trả về thông tin Tour chi tiết.
* **Alternative Flow:**
  * *AF1 (Thay đổi trạng thái Tour):* Admin ẩn/hiện Tour (DRAFT, PUBLISHED, ARCHIVED).
* **Validation:**
  * Tên Tour không được trống, độ dài từ 10-250 ký tự.
  * Lịch trình theo ngày phải liên tục và khớp với tổng số ngày đêm của Tour.
* **Business Rule:**
  * Tour chỉ được mở bán khi ở trạng thái `PUBLISHED` và có ít nhất 1 Lịch khởi hành (TourSchedule) khả dụng.
* **Error Case:**
  * `ERR_TOUR_INVALID_ITINERARY`: Lịch trình tour không hợp lệ hoặc thiếu thông tin điểm đến.
* **Output:** Thông tin Tour hoàn chỉnh (`TourDetailVm`).

---

### 2.2. Chức năng: Quản lý Lịch khởi hành & Bảng giá (Tour Schedule & Seat Management)
* **Actor:** Admin.
* **Business Goal:** Thiết lập ngày khởi hành, giá vé (Người lớn, Trẻ em, Em bé) và hạn mức số chỗ (Total Capacity) cho từng chuyến đi.
* **Preconditions:** Tour đã tồn tại ở trạng thái `PUBLISHED`.
* **Main Flow:**
  1. Admin chọn Tour và tạo Lịch khởi hành `TourSchedule` (Ngày đi, Ngày về, Số chỗ mở bán `totalSeats`, Giá vé).
  2. Hệ thống khởi tạo `availableSeats = totalSeats`.
  3. Lưu thông tin vào `db_travel_tour`.
* **Alternative Flow:** Không.
* **Validation:**
  * Ngày khởi hành phải trong tương lai (Ngày đi > Ngày hiện tại).
  * Giá vé phải > 0.
* **Business Rule:**
  * Không cho phép sửa giảm `totalSeats` xuống thấp hơn số chỗ đã được đặt (Booked Seats).
* **Error Case:**
  * `ERR_SCHEDULE_DATE_INVALID`: Ngày khởi hành không hợp lệ hoặc bị trùng lịch đã tạo.
* **Output:** Thông tin Lịch khởi hành (`TourScheduleVm`).

---

### 2.3. Chức năng: Tìm kiếm & Tra cứu Danh mục Tour (Tour Search & Discovery)
* **Actor:** Du khách (Tourist), Khách vãng lai (Guest).
* **Business Goal:** Tìm kiếm Tour theo điểm đến, ngày đi, mức giá, từ khóa và sắp xếp kết quả.
* **Preconditions:** Không.
* **Main Flow:**
  1. Người dùng chọn các tiêu chí tìm kiếm (Ví dụ: Điểm đến = "Đà Nẵng", Giá từ 2tr-5tr, Khởi hành tháng 9).
  2. Hệ thống truy vấn các Tour thỏa mãn điều kiện và còn chỗ trống (`availableSeats > 0`).
  3. Trả về danh sách Tour có phân trang (Pagination).
* **Alternative Flow:** Không.
* **Validation:**
  * Số trang (`page`) >= 0, Kích thước trang (`size`) từ 1 đến 100.
* **Business Rule:**
  * Chỉ hiển thị các Tour đang ở trạng thái `PUBLISHED`.
* **Error Case:** Không.
* **Output:** Danh sách phân trang (`ApiResponse<Page<TourVm>>`).

---

### 2.4. Chức năng: Xử lý Trừ / Giữ chỗ Tạm thời (Reserve Tour Seats - Saga Event Participant)
* **Actor:** Hệ thống (`booking-service` thông qua Kafka Event `BookingCreatedEvent`).
* **Business Goal:** Giữ chỗ tạm thời cho khách hàng khi họ vừa tạo đơn đặt tour trong luồng Choreography Saga.
* **Preconditions:** Nhận được `BookingCreatedEvent` từ Kafka Topic `booking-created`.
* **Main Flow:**
  1. Consumer trong `tour-service` kiểm tra Redis Key Idempotency (`event:<eventId>`). Nếu trùng thì bỏ qua.
  2. Kiểm tra `availableSeats` trong `db_travel_tour` cho `TourScheduleId` tương ứng.
  3. **Nếu `availableSeats >= requestedSeats`:**
     - Trừ `availableSeats = availableSeats - requestedSeats`.
     - Lưu bản tin Event vào bảng `outbox` (`TourSeatsReservedEvent`).
     - Outbox Publisher bắn tin `TourSeatsReservedEvent` ra Kafka.
* **Alternative Flow:**
  * *AF1 (Hết chỗ - Allocation Failed):* Nếu `availableSeats < requestedSeats`:
    - Lưu Event `TourSeatsReservationFailedEvent` vào bảng `outbox`.
    - Outbox Publisher phát tin `TourSeatsReservationFailedEvent` ra Kafka để hủy đơn bên `booking-service`.
  * *AF2 (Hoàn chỗ - Compensating Transaction):* Nhận `PaymentFailedEvent` từ Kafka:
    - Cộng trả lại chỗ: `availableSeats = availableSeats + requestedSeats`.
* **Validation:**
  * Số lượng chỗ yêu cầu (`requestedSeats`) phải > 0.
* **Business Rule:**
  * Thao tác trừ chỗ trong DB phải sử dụng Pessimistic Locking hoặc Optimistic Locking (`@Version`) để chống Race Condition khi nhiều khách đặt cùng lúc.
* **Error Case:**
  * `ERR_TOUR_SCHEDULE_NOT_FOUND`: Lịch khởi hành không tồn tại.
* **Output:** Event `TourSeatsReservedEvent` hoặc `TourSeatsReservationFailedEvent` được lưu vào `outbox`.

---

### 💡 ĐỀ XUẤT NGHIỆP VỤ BỔ SUNG CHO TOUR SERVICE:
1. **Chức năng Quản lý Đánh giá & Phản hồi Tour (Tour Reviews & Ratings):** Du khách sau khi hoàn thành chuyến đi được đánh giá 1-5 sao và để lại bình luận.
2. **Chức năng Quản lý Chính sách Hủy Tour (Tour Cancellation Rules):** Cấu hình tỷ lệ hoàn tiền theo mốc thời gian hủy (Ví dụ: Hủy trước 7 ngày hoàn 100%, trước 3 ngày hoàn 50%).
3. **Chức năng Bảng giá theo Mùa / Ngày lễ (Dynamic Pricing):** Tự động điều chỉnh giá tour vào các dịp cao điểm, cuối tuần.

---

# III. BOOKING SERVICE (booking-service)
**Nhiệm vụ:** Quản lý toàn bộ vòng đời đơn đặt tour (PENDING, SEATS_RESERVED, PAYMENT_PENDING, CONFIRMED, CANCELLED) và khởi xướng luồng Saga.

---

### 3.1. Chức năng: Tạo Đơn Đặt Tour (Create Booking - Saga Initiator)
* **Actor:** Du khách (Tourist).
* **Business Goal:** Cho phép khách hàng đặt Tour và bắt đầu quy trình giữ chỗ & thanh toán.
* **Preconditions:** Người dùng đã đăng nhập, TourSchedule còn mở bán.
* **Main Flow:**
  1. Người dùng gửi thông tin đặt tour (`tourScheduleId`, số lượng vé, danh sách thông tin hành khách).
  2. Hệ thống tính toán tổng tiền tạm tính (Total Amount).
  3. Trong cùng 1 Database Transaction:
     - Tạo bản ghi `Booking` với trạng thái `PENDING` trong `db_travel_booking`.
     - Lưu thông tin danh sách hành khách `BookingPassenger`.
     - Lưu bản tin `BookingCreatedEvent` vào bảng `outbox`.
  4. Outbox Publisher quét bảng `outbox` và phát `BookingCreatedEvent` lên Kafka topic `booking-created`.
  5. Trả về thông tin đơn hàng ở trạng thái `PENDING` cho khách hàng.
* **Alternative Flow:** Không.
* **Validation:**
  * Số lượng hành khách phải >= 1.
  * Thông tin từng hành khách (Họ tên, Ngày sinh, CCCD/Passport) phải hợp lệ.
* **Business Rule:**
  * Đơn hàng vừa tạo chỉ có hiệu lực giữ chỗ trong 15 phút. Nếu quá thời gian thanh toán sẽ tự động bị hủy.
* **Error Case:**
  * `ERR_BOOKING_SCHEDULE_EXPIRED`: Lịch khởi hành đã đóng hoặc trôi qua.
* **Output:** Thông tin Booking vừa tạo với trạng thái `PENDING` (`BookingVm`).

---

### 3.2. Chức năng: Cập nhật Trạng thái Đơn hàng theo Saga Events (Saga State Machine)
* **Actor:** Hệ thống (`booking-service` tiêu thụ Kafka Events).
* **Business Goal:** Tự động chuyển đổi trạng thái đơn hàng dựa trên kết quả xử lý từ `tour-service` và `payment-service`.
* **Preconditions:** Nhận Event từ Kafka (`TourSeatsReservedEvent`, `TourSeatsReservationFailedEvent`, `PaymentProcessedEvent`, `PaymentFailedEvent`).
* **Main Flow:**
  1. Consumer kiểm tra Idempotency Key trên Redis. Nếu đã xử lý thì ngắt.
  2. **Trường hợp 1 (Nhận `TourSeatsReservedEvent`):**
     - Cập nhật trạng thái Booking từ `PENDING` -> `SEATS_RESERVED` / `PAYMENT_PENDING`.
  3. **Trường hợp 2 (Nhận `PaymentProcessedEvent`):**
     - Cập nhật trạng thái Booking từ `PAYMENT_PENDING` -> `CONFIRMED`.
     - Gửi email xác nhận đặt tour thành công cho khách hàng.
  4. **Trường hợp 3 (Nhận `TourSeatsReservationFailedEvent` hoặc `PaymentFailedEvent`):**
     - Cập nhật trạng thái Booking -> `CANCELLED`.
     - Gửi thông báo đặt tour thất bại kèm lý do cho khách hàng.
* **Alternative Flow:** Không.
* **Validation:**
  * Trạng thái chuyển đổi phải tuân theo đúng Ma trận trạng thái (State Machine Matrix).
* **Business Rule:**
  * Không cho phép chuyển trạng thái đơn hàng từ `CANCELLED` sang `CONFIRMED`.
* **Error Case:**
  * `ERR_BOOKING_INVALID_STATE_TRANSITION`: Chuyển đổi trạng thái không hợp lệ.
* **Output:** Trạng thái đơn hàng trong `db_travel_booking` được cập nhật chính xác.

---

### 3.3. Chức năng: Xem Lịch sử & Chi tiết Đơn Đặt Tour (Booking History & Detail)
* **Actor:** Du khách (Tourist), Admin.
* **Business Goal:** Cho phép khách hàng xem lại các đơn đặt tour của mình và Admin quản lý toàn bộ đơn hàng hệ thống.
* **Preconditions:** Người dùng đã đăng nhập.
* **Main Flow:**
  1. Người dùng gửi yêu cầu lấy danh sách đơn đặt tour của cá nhân (hoặc Admin lọc theo trạng thái/ngày).
  2. `booking-service` truy vấn dữ liệu từ `db_travel_booking`.
  3. Gọi `tour-service` qua OpenFeign (nếu cần) để lấy thông tin vắn tắt của Tour.
  4. Trả về danh sách đơn đặt tour kèm trạng thái hiện tại.
* **Alternative Flow:** Không.
* **Validation:**
  * Du khách chỉ được xem đơn đặt tour của chính mình (`X-User-Id` khớp với `booking.userId`).
* **Business Rule:**
  * Admin có quyền xem và tra cứu đơn hàng của tất cả người dùng.
* **Error Case:**
  * `ERR_BOOKING_NOT_FOUND`: Đơn hàng không tồn tại.
* **Output:** Danh sách hoặc chi tiết đơn đặt tour (`BookingDetailVm`).

---

### 3.4. Chức năng: Yêu cầu Hủy Đơn Đặt Tour (Customer Cancel Booking)
* **Actor:** Du khách (Tourist).
* **Business Goal:** Cho phép khách hàng tự hủy đơn đặt tour khi chưa thanh toán hoặc khi còn trong thời hạn cho phép hủy.
* **Preconditions:** Đơn hàng đang ở trạng thái `PENDING`, `SEATS_RESERVED` hoặc `CONFIRMED` (thỏa mãn điều kiện hủy).
* **Main Flow:**
  1. Khách hàng gửi yêu cầu Hủy đơn hàng kèm lý do.
  2. `booking-service` chuyển trạng thái đơn hàng thành `CANCELLED`.
  3. Ghi Event `BookingCancelledEvent` vào bảng `outbox`.
  4. Outbox Publisher bắn `BookingCancelledEvent` lên Kafka để `tour-service` hoàn lại chỗ và `payment-service` thực hiện hoàn tiền (nếu đã thanh toán).
* **Alternative Flow:** Không.
* **Validation:**
  * Lý do hủy không được để trống.
* **Business Rule:**
  * Đơn hàng đã hủy không thể phục hồi lại.
* **Error Case:**
  * `ERR_BOOKING_CANNOT_CANCEL`: Đơn hàng đã sắp khởi hành (dưới 24h) không được tự hủy qua ứng dụng.
* **Output:** Trạng thái đơn hàng đổi thành `CANCELLED`, Event hủy được phát đi.

---

### 💡 ĐỀ XUẤT NGHIỆP VỤ BỔ SUNG CHO BOOKING SERVICE:
1. **Chức năng Áp dụng Mã Giảm Giá (Voucher / Promo Code):** Nhập mã ưu đãi để trừ tiền trước khi khởi tạo thanh toán.
2. **Chức năng Xuất Vé Du Lịch Điện Tử (E-Ticket PDF Generation):** Sau khi đơn hàng `CONFIRMED`, tự động tạo vé điện tử QR Code để gửi qua email và hiển thị trên App.
3. **Chức năng Nhắc nhở Thanh toán (Payment Timeout Reminder):** Gửi thông báo đẩy (Push Notification/Email) trước 5 phút khi đơn hàng sắp hết hạn giữ chỗ.

---

# IV. PAYMENT SERVICE (payment-service)
**Nhiệm vụ:** Xử lý các giao dịch thanh toán (VNPAY, MOMO, Stripe), tiếp nhận Webhook và xử lý hoàn tiền (Refund).

---

### 4.1. Chức năng: Khởi tạo Giao dịch Thanh toán (Create Payment URL)
* **Actor:** Du khách (Tourist) hoặc Hệ thống (`booking-service` kích hoạt qua Saga).
* **Business Goal:** Tạo liên kết thanh toán an toàn để chuyển hướng người dùng sang cổng thanh toán thứ 3 (VNPAY, MOMO, Stripe).
* **Preconditions:** Đơn đặt tour ở trạng thái `SEATS_RESERVED` / `PAYMENT_PENDING`.
* **Main Flow:**
  1. Hệ thống nhận yêu cầu thanh toán cho `bookingId` kèm cổng thanh toán lựa chọn (VNPAY/MOMO/Stripe).
  2. `payment-service` khởi tạo bản ghi `Payment` ở trạng thái `PENDING` trong `db_travel_payment`.
  3. Gọi SDK/API của cổng thanh toán tương ứng để tạo Payment Gateway URL (chứa mã chữ ký HASH checksum).
  4. Trả về liên kết thanh toán cho Client.
* **Alternative Flow:** Không.
* **Validation:**
  * Số tiền thanh toán (`amount`) phải > 0 và khớp chính xác với tổng tiền đơn hàng bên `booking-service`.
* **Business Rule:**
  * Mỗi Payment URL có thời gian sống (TTL) đúng bằng thời gian giữ chỗ của đơn hàng (15 phút).
* **Error Case:**
  * `ERR_PAYMENT_GATEWAY_ERROR`: Lỗi kết nối tới cổng thanh toán thứ 3.
* **Output:** Đường dẫn thanh toán (`PaymentUrlVm`).

---

### 4.2. Chức năng: Xử lý Webhook / Callback Thanh toán (Payment Webhook Handler)
* **Actor:** Cổng thanh toán thứ 3 (VNPAY, MOMO, Stripe IPN Webhook).
* **Business Goal:** Tiếp nhận kết quả thanh toán thực tế từ cổng thanh toán và cập nhật giao dịch.
* **Preconditions:** Cổng thanh toán gửi yêu cầu HTTP POST/GET IPN chứa chữ ký bảo mật (Secure Hash).
* **Main Flow:**
  1. `payment-service` nhận Webhook từ cổng thanh toán.
  2. Kiểm tra tính toàn vẹn dữ liệu bằng cách tính và so sánh chữ ký Secure Hash (HMACSHA512).
  3. Nếu chữ ký hợp lệ và mã phản hồi = "00" (Thành công):
     - Cập nhật bản ghi Payment trong `db_travel_payment` -> `SUCCESS`.
     - Lưu bản tin `PaymentProcessedEvent` vào bảng `outbox`.
     - Outbox Publisher bắn `PaymentProcessedEvent` ra Kafka để `booking-service` đổi trạng thái thành `CONFIRMED`.
* **Alternative Flow:**
  * *AF1 (Thanh toán Thất bại / Khách hủy giao dịch):*
    - Cập nhật Payment -> `FAILED`.
    - Ghi `PaymentFailedEvent` vào bảng `outbox`.
    - Outbox Publisher phát `PaymentFailedEvent` ra Kafka để kích hoạt Compensating Transaction (Release seats bên `tour-service`, Cancel booking bên `booking-service`).
* **Validation:**
  * Chữ ký Secure Hash phải khớp 100% với khóa Secret Key đã cấu hình.
* **Business Rule:**
  * Webhook phải đảm bảo xử lý Idempotent (IPN xử lý nhiều lần cùng 1 giao dịch không gây sai lệch dữ liệu).
* **Error Case:**
  * `ERR_PAYMENT_INVALID_CHECKSUM`: Chữ ký bảo mật không đúng (nguy cơ giả mạo dữ liệu).
* **Output:** Phản hồi chuẩn theo yêu cầu cổng thanh toán (ví dụ: `{"RspCode":"00","Message":"Confirm Success"}`).

---

### 4.3. Chức năng: Xử lý Hoàn tiền (Payment Refund Process)
* **Actor:** Admin hoặc Hệ thống (Khi chuyến đi bị hủy do lỗi từ nhà tổ chức).
* **Business Goal:** Hoàn lại tiền cho khách hàng qua cổng thanh toán ban đầu khi đơn hàng bị hủy hợp lệ.
* **Preconditions:** Giao dịch thanh toán đã ở trạng thái `SUCCESS` và đơn hàng bị hủy thỏa điều kiện hoàn tiền.
* **Main Flow:**
  1. Admin gửi yêu cầu Hoàn tiền cho `paymentId` kèm số tiền hoàn.
  2. `payment-service` gọi API Refund của VNPAY/Stripe.
  3. Cập nhật trạng thái Payment -> `REFUNDED` và lưu lịch sử `PaymentTransaction`.
  4. Trả về kết quả hoàn tiền.
* **Alternative Flow:** Không.
* **Validation:**
  * Số tiền hoàn tiền phải <= Số tiền đã thanh toán ban đầu.
* **Business Rule:**
  * Tiền hoàn phải được trả đúng về tài khoản/thẻ mà khách đã dùng để thanh toán.
* **Error Case:**
  * `ERR_PAYMENT_REFUND_FAILED`: Cổng thanh toán từ chối hoàn tiền (do hết hạn thời gian hoàn tiền theo chính sách cổng).
* **Output:** Quyết định và mã giao dịch hoàn tiền (`RefundResultVm`).

---

### 💡 ĐỀ XUẤT NGHIỆP VỤ BỔ SUNG CHO PAYMENT SERVICE:
1. **Chức năng Đối soát Giao dịch Tự động (Reconciliation Engine):** Chạy tác vụ định kỳ cuối ngày so sánh đối soát file log giao dịch từ VNPAY/Stripe với DB nội bộ để phát hiện lệch tiền.
2. **Chức năng Thanh toán Trả góp / Đặt cọc (Deposit & Installment Payment):** Cho phép khách đặt cọc trước 30-50% tiền tour, phần còn lại thanh toán trước ngày đi 3 ngày.

---

# V. AI SERVICE (ai-service)
**Nhiệm vụ:** Cung cấp Trợ lý AI tư vấn lịch trình du lịch thông minh, tìm kiếm ngữ nghĩa (RAG) và Chatbot tương tác bằng Spring AI & pgvector.

---

### 5.1. Chức năng: Gợi ý Lịch trình Du lịch Thông minh (Smart Trip Recommendation)
* **Actor:** Du khách (Tourist), Khách vãng lai.
* **Business Goal:** Tự động tạo gợi ý lịch trình du lịch cá nhân hóa dựa trên sở thích, ngân sách, số ngày đi và thành phần đoàn du lịch.
* **Preconditions:** Dịch vụ AI Service hoạt động, LLM Model (Google Gemini / Ollama / OpenAI) sẵn sàng.
* **Main Flow:**
  1. Khách hàng nhập nhu cầu (Ví dụ: "Tôi muốn đi du lịch nghỉ dưỡng 3 ngày 2 đêm tại Nha Trang cho gia đình có con nhỏ, ngân sách 10 triệu").
  2. `ai-service` chuyển đổi câu hỏi thành Vector Embedding (1536 chiều).
  3. Kiểm tra **Semantic Cache** trên Redis (`Similarity >= 0.95`). Nếu trúng Cache, trả về ngay kết quả câu trả lời cũ (tiết kiệm chi phí gọi LLM).
  4. Nếu không trúng Cache, truy vấn RAG (HNSW Cosine Vector Search + Metadata Filter) trên `db_travel_ai` (`pgvector`) để lấy các Chunks Tour phù hợp nhất.
  5. Đưa qua bộ Re-Ranker để chọn Top 3-5 Chunks chuẩn nhất, ghép vào Anti-Hallucination Prompt Template gửi LLM.
  6. Trả về kết quả tư vấn dạng cấu trúc JSON chứa link đặt tour thực tế cho Client và lưu kết quả vào Semantic Cache.
* **Alternative Flow:** Không.
* **Validation:**
  * Câu hỏi/yêu cầu của người dùng không vượt quá 1000 ký tự.
* **Business Rule:**
  * **Anti-Hallucination Guardrail:** AI tuyệt đối không được tự ý sáng tạo ra các Tour hoặc giá vé không có sẵn trong dữ liệu ngữ cảnh RAG.
  * Các gợi ý Tour trong bài tư vấn phải bắt buộc đính kèm ID và liên kết tới Tour thật đang mở bán trong hệ thống.
* **Error Case:**
  * `ERR_AI_MODEL_TIMEOUT`: LLM API không phản hồi kịp thời (quá 15s).
* **Output:** Lịch trình gợi ý chi tiết kèm danh sách Tour liên quan (`TripRecommendationVm`).

---

### 5.2. Chức năng: AI Chatbot Tư vấn Khách hàng (Conversational Assistant)
* **Actor:** Du khách (Tourist).
* **Business Goal:** Phản hồi tức thì các thắc mắc của khách hàng về chính sách tour, hành lý, thời tiết, quy trình đặt vé 24/7.
* **Preconditions:** Người dùng đã khởi tạo phiên chat (`ChatSession`).
* **Main Flow:**
  1. Người dùng gửi câu hỏi trong khung Chatbot.
  2. `ai-service` tìm kiếm câu trả lời khớp nhất từ cơ sở tri thức (Knowledge Base / Embeddings của tài liệu chính sách).
  3. Sinh câu trả lời tự nhiên và trả về cho người dùng.
  4. Lưu lịch sử hội thoại vào `ChatSession` trong `db_travel_ai`.
* **Alternative Flow:**
  * *AF1 (Không tìm thấy câu trả lời):* Nếu AI không chắc chắn (Độ tin cậy Vector Search < 0.6) -> Gợi ý người dùng kết nối với Tổng đài viên hỗ trợ trực tiếp.
* **Validation:**
  * Tin nhắn không chứa từ ngữ vi phạm tiêu chuẩn cộng đồng hoặc độc hại (Content Safety Check).
* **Business Rule:**
  * AI Chatbot không được đưa ra cam kết giá vé sai lệch so với giá niêm yết chính thức trong DB.
* **Error Case:**
  * `ERR_AI_QUOTA_EXCEEDED`: Vượt quá giới hạn gọi API LLM trong ngày.
* **Output:** Câu trả lời của Chatbot (`ChatMessageVm`).

---

### 5.3. Chức năng: Tìm kiếm Tour theo Ngữ nghĩa (Semantic Tour Search)
* **Actor:** Du khách (Tourist).
* **Business Goal:** Cho phép tìm kiếm Tour theo ý định trải nghiệm thay vì chỉ tìm theo từ khóa chính xác.
* **Preconditions:** Dữ liệu Tour đã được Vectorize (tạo Embedding) và lưu vào `pgvector`.
* **Main Flow:**
  1. Người dùng nhập cụm từ tìm kiếm tự do (Ví dụ: "Nơi nào leo núi ngắm mây mùa thu đẹp nhất?").
  2. `ai-service` chuyển cụm từ thành Vector Embedding.
  3. Thực hiện truy vấn khoảng cách Cosine/Euclidean trong `pgvector` để tìm các Tour có nội dung tương đồng về ngữ nghĩa.
  4. Trả về danh sách Tour xếp theo độ tương đồng giảm dần.
* **Alternative Flow:** Không.
* **Validation:** Cụm từ tìm kiếm không được để trống.
* **Business Rule:** Chỉ trả về các Tour khả dụng có sẵn trong `tour-service`.
* **Error Case:** Không.
* **Output:** Danh sách Tour tương đồng ngữ nghĩa (`List<TourVm>`).

---

### 💡 ĐỀ XUẤT NGHIỆP VỤ BỔ SUNG CHO AI SERVICE:
1. **Chức năng Phân tích Cảm xúc Phản hồi Khách hàng (Sentiment Analysis):** Tự động phân tích đánh giá của khách hàng (Tích cực, Tiêu cực, Trung tính) để cảnh báo cho đội ngũ CSKH khi có đánh giá xấu.
2. **Chức năng Cá nhân hóa Trang chủ (AI Personalization Recommendation):** Đề xuất danh sách Tour yêu thích ngay tại Trang chủ dựa trên lịch sử tìm kiếm và đặt vé trước đó của du khách.

---

# VI. BẢNG TỔNG HỢP MA TRẬN CHỨC NĂNG & THIẾU SÓT NGHIỆP VỤ ĐỀ XUẤT

| Service | Số lượng Chức năng Hiện có | Các Nghiệp vụ Đề xuất Bổ sung |
| :--- | :--- | :--- |
| **auth-service** | 3 Chức năng cốt lõi | Quên mật khẩu OTP, Đăng xuất đa thiết bị, Phê duyệt Hướng dẫn viên |
| **tour-service** | 4 Chức năng cốt lõi | Đánh giá Tour (Reviews), Chính sách Hủy Tour, Giá Tour linh hoạt theo mùa |
| **booking-service** | 4 Chức năng cốt lõi | Mã giảm giá (Voucher), Xuất vé điện tử E-Ticket PDF, Nhắc nhở hết hạn đặt chỗ |
| **payment-service** | 3 Chức năng cốt lõi | Đối soát tự động cuối ngày (Reconciliation), Thanh toán Đặt cọc / Trả góp |
| **ai-service** | 3 Chức năng cốt lõi | Phân tích cảm xúc review khách hàng, Cá nhân hóa gợi ý Tour theo hành vi |
