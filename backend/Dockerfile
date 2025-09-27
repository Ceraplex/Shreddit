# Build stage
FROM maven:3.9.9-eclipse-temurin-23 AS builder
WORKDIR /app
# Copy only the backend module contents
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=builder /app/target/shreddit-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
