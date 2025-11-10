# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---- Runtime stage (modern Ubuntu + Tesseract + Java 21) ----
FROM ubuntu:22.04

# Install Java, Tesseract, and PDF tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    tesseract-ocr \
    poppler-utils \
    ghostscript \
    curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/ocr-worker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
