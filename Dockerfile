# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests  # Збираємо JAR, ігноруємо тести (для швидкості)

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE ${PORT:-9000}  # Експонуємо динамічний порт
ENTRYPOINT ["java", "-jar", "app.jar"]