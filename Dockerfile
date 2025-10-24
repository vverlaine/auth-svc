FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/build/libs/auth-svc-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_FLYWAY_ENABLED=false \
    SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
