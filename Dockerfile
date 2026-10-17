# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline

COPY src ./src
RUN mvn -B -ntp package -DskipTests


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S -g 10001 appgroup \
    && adduser -S -D -H -u 10001 -G appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup \
    /build/target/*.jar /app/app.jar

USER appuser

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
