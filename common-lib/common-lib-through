# Walkthrough - Tạo Bộ Khung Hạ Tầng Cho common-lib

Đã hoàn tất việc xây dựng bộ khung (Skeleton & Base Infrastructure Classes) cho cả 7 sub-modules thuộc `common-lib` dựa trên [architecture.md](file:///e:/Travel_Management/architecture.md).

## Completed Framework Components

### 1. `common-core`
- [ApiResponse.java](file:///e:/Travel_Management/common-lib/common-core/src/main/java/com/travel/common/core/dto/ApiResponse.java): Record chuẩn wrapper Response cho toàn hệ thống (`ok()`, `error()`).
- [ErrorVm.java](file:///e:/Travel_Management/common-lib/common-core/src/main/java/com/travel/common/core/dto/ErrorVm.java): Record lỗi chi tiết.
- [MdcKey.java](file:///e:/Travel_Management/common-lib/common-core/src/main/java/com/travel/common/core/constant/MdcKey.java): Hằng số MDC (`traceId`, `userId`).
- Exception Classes: `NotFoundException.java`, `BadRequestException.java`, `DuplicatedException.java`.
- Utilities: `DateTimeUtils.java`, `JsonUtils.java`.

### 2. `common-spring`
- [GlobalExceptionHandler.java](file:///e:/Travel_Management/common-lib/common-spring/src/main/java/com/travel/common/spring/exception/GlobalExceptionHandler.java): Xử lý ngoại lệ tập trung (`@RestControllerAdvice`) bắt `NotFoundException`, `BadRequestException`, `DuplicatedException` và `MethodArgumentNotValidException`.
- [OpenApiConfig.java](file:///e:/Travel_Management/common-lib/common-spring/src/main/java/com/travel/common/spring/config/OpenApiConfig.java): Swagger OpenAPI config bean.
- [WebCorsConfig.java](file:///e:/Travel_Management/common-lib/common-spring/src/main/java/com/travel/common/spring/config/WebCorsConfig.java): CORS config bean.

### 3. `common-security`
- [UserContext.java](file:///e:/Travel_Management/common-lib/common-security/src/main/java/com/travel/common/security/context/UserContext.java): ThreadLocal helper lưu trữ thông tin User hiện tại.
- [UserHeaderFilter.java](file:///e:/Travel_Management/common-lib/common-security/src/main/java/com/travel/common/security/filter/UserHeaderFilter.java): Filter bóc tách HTTP Headers (`X-User-Id`, `X-User-Roles`, `X-User-Email`) đã xác thực từ Gateway.

### 4. `common-keycloak`
- [KeycloakProperties.java](file:///e:/Travel_Management/common-lib/common-keycloak/src/main/java/com/travel/common/keycloak/config/KeycloakProperties.java): Configuration properties cho Keycloak.
- [KeycloakClientBridge.java](file:///e:/Travel_Management/common-lib/common-keycloak/src/main/java/com/travel/common/keycloak/bridge/KeycloakClientBridge.java): Interface cho kết nối Keycloak Server.

### 5. `common-kafka`
- [AbstractEventPayload.java](file:///e:/Travel_Management/common-lib/common-kafka/src/main/java/com/travel/common/kafka/event/AbstractEventPayload.java): Base class cho tất cả Kafka Events (`eventId`, `eventType`, `timestamp`).
- [IdempotencyService.java](file:///e:/Travel_Management/common-lib/common-kafka/src/main/java/com/travel/common/kafka/idempotency/IdempotencyService.java): Service kiểm tra `idempotencyKey` (`event:<eventId>`) trên Redis bằng Atomic `SETNX` với TTL 24h.

### 6. `common-storage`
- [StorageProperties.java](file:///e:/Travel_Management/common-lib/common-storage/src/main/java/com/travel/common/storage/config/StorageProperties.java): Configuration properties cho MinIO / S3.
- [StorageService.java](file:///e:/Travel_Management/common-lib/common-storage/src/main/java/com/travel/common/storage/service/StorageService.java): Interface chuẩn hóa thao tác upload/delete file.

### 7. `common-logging`
- [MdcLoggingFilter.java](file:///e:/Travel_Management/common-lib/common-logging/src/main/java/com/travel/common/logging/filter/MdcLoggingFilter.java): Filter tự động bóc tách hoặc sinh `traceId` và đẩy vào MDC.
- [LoggingAspect.java](file:///e:/Travel_Management/common-lib/common-logging/src/main/java/com/travel/common/logging/aspect/LoggingAspect.java): SLF4J AOP Aspect log thời gian thực thi phương thức `@Service` và `@RestController`.
