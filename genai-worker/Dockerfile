FROM maven:3.9.6-eclipse-temurin-17 as build
WORKDIR /app
COPY . .
RUN mvn -q -e -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/genai-worker-*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport
EXPOSE 8091
ENTRYPOINT ["java","-jar","/app/app.jar"]
