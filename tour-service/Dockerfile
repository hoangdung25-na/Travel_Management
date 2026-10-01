# ==============================================================================
# STAGE 1: BUILD STAGE
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY . .

RUN mvn clean package -pl tour-service -am -DskipTests

# ==============================================================================
# STAGE 2: RUNTIME STAGE
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8082

COPY --from=builder /build/tour-service/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
