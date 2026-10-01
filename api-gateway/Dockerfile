# ==============================================================================
# STAGE 1: BUILD STAGE (Biên dịch mã nguồn Java với Maven)
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Đặt thư mục làm việc trong Container Build
WORKDIR /build

# Copy toàn bộ cấu trúc dự án monorepo (đã qua lọc bởi .dockerignore)
COPY . .

# Biên dịch module api-gateway và các thư viện common-lib phụ thuộc (-am)
RUN mvn clean package -pl api-gateway -am -DskipTests

# ==============================================================================
# STAGE 2: RUNTIME STAGE (Môi trường chạy sản phẩm siêu nhẹ)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

# Đặt thư mục làm việc cho ứng dụng khi chạy
WORKDIR /app

# Biến môi trường tối ưu dung lượng bộ nhớ RAM (JVM Heap)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Mở cổng 8080 (Port mặc định của API Gateway)
EXPOSE 8080

# Chép duy nhất file JAR đã được đóng gói từ Stage Builder sang Stage Runtime
COPY --from=builder /build/api-gateway/target/*.jar app.jar

# Lệnh chạy ứng dụng khi Container khởi động
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
