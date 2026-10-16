# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-26 AS builder

WORKDIR /app

# Copy dependency configuration first
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw

# Cache dependencies unless Maven configuration changes
RUN ./mvnw -B dependency:go-offline

# Copy frequently changing source code afterwards
COPY src/ src/

RUN ./mvnw -B package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:26-jre

WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup appuser

COPY --from=builder --chown=appuser:appgroup \
    /app/target/*.jar /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
