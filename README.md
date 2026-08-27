#  Travel Management Platform

**Travel Management Platform** là một hệ thống quản lý và đặt tour du lịch đa dịch vụ được xây dựng theo kiến trúc **Microservices (Monorepo Maven Multi-Module)** kết hợp với **Event-Driven Architecture (Apache Kafka)** và trợ lý **AI Tư vấn Lịch trình Du lịch (Spring AI & Google Gemini)**.

Hệ thống cung cấp giải pháp toàn diện từ đăng ký/đăng nhập, tìm kiếm tour, lập lịch trình, đặt chỗ (booking), xử lý thanh toán bất đồng bộ cho tới tư vấn du lịch thông minh bằng trí tuệ nhân tạo.

---

## 🛠️ Công Nghệ Sử Dụng

### 1. Frontend

* **Framework:** [Next.js 16](https://nextjs.org/) (App Router, React 19)
* **Ngôn ngữ:** TypeScript
* **Styling:** TailwindCSS v4, Lucide React Icons
* **Xử lý Form & Validation:** React Hook Form, Zod
* **HTTP Client & Utilities:** Axios, Date-fns, JS-Cookie, JWT Decode

### 2. Backend

* **Ngôn ngữ & Runtime:** Java 21
* **Framework:** Spring Boot 3.3.4, Spring Cloud 2023.0.3
* **Quản lý Dự án:** Maven Multi-Module Monorepo
* **Mapper & Tooling:** MapStruct 1.5.5, Lombok
* **API Gateway:** Spring Cloud Gateway (Port 8080)
* **Event Broker:** Apache Kafka 7.6.1 (KRaft Mode) & Kafka UI (Port 8088) cho luồng Event-Driven / Saga Choreography
* **Authentication & Security:** Spring Security, JWT

### 3. Cơ Sở Dữ Liệu

* **Chính (Relational Database):** PostgreSQL 16 (tích hợp extension `pgvector` phục vụ vector search cho AI Service)
* **Mô hình Database:** Mỗi Microservice sở hữu database riêng biệt (Database-per-service):
  * `db_travel_auth`: Quản lý tài khoản & phân quyền
  * `db_travel_tour`: Quản lý tour, địa điểm & lịch trình
  * `db_travel_booking`: Quản lý đơn đặt tour & trạng thái booking
  * `db_travel_payment`: Quản lý giao dịch thanh toán
  * `db_travel_ai`: Lưu trữ vector embedding & lịch sử tư vấn AI
* **Caching & Idempotency Store:** Redis 7 (lưu cache và chống trùng lặp sự kiện Kafka)

### 4. Công Nghệ Bổ Sung

* **Docker & Docker Compose:** Đóng gói container cho toàn bộ hạ tầng (PostgreSQL, Redis, Kafka, Kafka UI) và các ứng dụng backend/frontend.
* **AI Integration:** Spring AI tích hợp Google Gemini API & Vector Search (`pgvector`) tư vấn lịch trình thông minh.

---

## 🚀 Tính Năng Chính

### 👤 Khách Du Lịch (Tourist)

* **Xác thực & Tài khoản:** Đăng ký, đăng nhập, quản lý thông tin cá nhân.
* **Khám phá Tour:** Tìm kiếm, lọc và xem thông tin chi tiết tour du lịch, lịch trình chi tiết theo từng ngày, bảng giá và số chỗ còn trống.
* **Đặt Tour (Booking):** Đặt giữ chỗ cho tour du lịch với quy trình xử lý trạng thái tự động.
* **Thanh Toán:** Thanh toán đơn đặt tour an toàn qua hệ thống Payment Service.
* **Trợ Lý AI Tư Vấn:** Chat và nhận gợi ý lịch trình du lịch cá nhân hóa từ AI Assistant.

### 👑 Quản Trị Viên (Admin) & Hướng Dẫn Viên (Guide)

* **Quản lý Tour:** Tạo mới, cập nhật danh mục tour, quản lý điểm đến và lịch trình chi tiết.
* **Quản lý Booking:** Theo dõi trạng thái các đơn đặt tour của khách hàng.
* **Quản lý Tài Khoản:** Quản lý danh sách người dùng và phân quyền hệ thống.

---

## 🔑 Tài Khoản Thử Nghiệm

Hệ thống tự động khởi tạo dữ liệu tài khoản mẫu khi dịch vụ `auth-service` khởi chạy:

| Vai trò | Tài khoản (Email) | Mật khẩu | Ghi chú |
| ------- | ----------------- | -------- | ------- |
| **Quản trị viên** | `admin@example.com` | `12345678` | Quyền Quản trị hệ thống (`ROLE_ADMIN`) |
| **Hướng dẫn viên** | `guide@example.com` | `12345678` | Quyền Hướng dẫn viên (`ROLE_GUIDE`) |
| **Khách du lịch** | `tourist@example.com` | `12345678` | Quyền Khách du lịch (`ROLE_TOURIST`) |

---

## 🏃 Hướng Dẫn Chạy Chương Trình

### 📋 Yêu Cầu Hệ Thống

Trước khi bắt đầu, đảm bảo máy tính của bạn đã cài đặt:

* **Java SDK 21** trở lên
* **Node.js** (phiên bản v18+ trở lên) & **npm**
* **Apache Maven 3.x**
* **Docker** & **Docker Desktop** (đang chạy)

---

### Bước 1: Cài đặt thư viện

1. **Biên dịch Backend Java (Maven):**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Cài đặt dependencies Frontend (Next.js):**
   ```bash
   cd travel-frontend
   npm install
   cd ..
   ```

---

### Bước 2: Cấu hình biến môi trường

Tạo file `.env` từ file mẫu `.env.example` tại thư mục gốc của dự án:

```bash
cp .env.example .env
```

Các biến môi trường cơ bản trong `.env`:
```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgrespassword
POSTGRES_DB=db_travel
POSTGRES_PORT=5432
REDIS_PORT=6379
KAFKA_PORT_INTERNAL=9092
KAFKA_PORT_EXTERNAL=29092
KAFKA_UI_PORT=8088
```

---

### Bước 3: Cấu hình Database

Dữ liệu PostgreSQL và 5 cơ sở dữ liệu riêng biệt cho các microservices (`db_travel_auth`, `db_travel_tour`, `db_travel_booking`, `db_travel_payment`, `db_travel_ai`) kèm extension `pgvector` sẽ được **tự động khởi tạo** khi khởi động container PostgreSQL thông qua script `./scripts/init-databases.sh`.

---

### Bước 4: Khởi động hệ thống

#### Cách 1: Khởi chạy trọn gói bằng Script (Khuyên dùng trên Windows)

Chạy script PowerShell tự động khởi chạy Docker Infra, build Maven, khởi động 6 Backend Microservices và Next.js Frontend:

```powershell
.\start-all.ps1
```

Hoặc chạy file batch:
```cmd
start-all.bat
```

#### Cách 2: Khởi động từng phần thủ công

1. **Khởi động Hạ tầng Docker (PostgreSQL, Redis, Kafka, Kafka UI):**
   ```bash
   docker-compose up -d
   ```

2. **Khởi động 6 Backend Microservices:**
   ```bash
   java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
   java -jar auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
   java -jar tour-service/target/tour-service-1.0.0-SNAPSHOT.jar
   java -jar booking-service/target/booking-service-1.0.0-SNAPSHOT.jar
   java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar
   java -jar ai-service/target/ai-service-1.0.0-SNAPSHOT.jar
   ```

3. **Khởi động Frontend Client:**
   ```bash
   cd travel-frontend
   npm run dev
   ```

---

### 🌐 Địa Chỉ Truy Cập Dịch Vụ

| Dịch vụ | URL / Địa chỉ |
| ------- | ------------- |
| **Website Frontend (Next.js)** | `http://localhost:3000` |
| **API Gateway** | `http://localhost:8080` |
| **Kafka UI Dashboard** | `http://localhost:8088` |
| **Auth Service** | `http://localhost:8081` |
| **Tour Service** | `http://localhost:8082` |
| **Booking Service** | `http://localhost:8083` |
| **Payment Service** | `http://localhost:8084` |
| **AI Service** | `http://localhost:8085` |

---

## 📂 Cấu Trúc Thư Mục Dự Án

```text
Travel_Management/
├── api-gateway/          # Spring Cloud Gateway (Port 8080)
├── auth-service/         # Microservice Quản lý Người dùng & Xác thực (Port 8081)
├── tour-service/         # Microservice Quản lý Tour, Lịch trình & Giữ chỗ (Port 8082)
├── booking-service/      # Microservice Quản lý Đơn đặt tour & Saga Event (Port 8083)
├── payment-service/      # Microservice Xử lý Thanh toán (Port 8084)
├── ai-service/           # Microservice AI Tư vấn Lịch trình & RAG (Port 8085)
├── common-lib/           # Thư viện dùng chung (core, security, kafka, storage, logging)
├── travel-frontend/      # Ứng dụng Web Frontend (Next.js, React 19, TailwindCSS)
├── scripts/              # Scripts khởi tạo Database (`init-databases.sh`)
├── docker-compose.yml    # Hạ tầng Docker cho PostgreSQL pgvector, Redis, Kafka, Kafka UI
├── pom.xml               # Root Maven Parent POM quản lý dependency tập trung
├── start-all.ps1         # Script PowerShell khởi chạy tự động toàn bộ hệ thống
├── start-all.bat         # Windows Batch script gọi start-all.ps1
├── .env.example          # Mẫu cấu hình biến môi trường
└── README.md             # Tài liệu hướng dẫn dự án
```
