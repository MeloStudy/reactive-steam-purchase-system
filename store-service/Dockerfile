# ── Build Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline -q

# Copy source and build the fat JAR (skip tests — already validated locally)
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Runtime Stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/store-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
