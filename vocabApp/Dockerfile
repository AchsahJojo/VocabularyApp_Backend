# Use Maven to build the application
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory inside container
WORKDIR /app

# Copy only the pom.xml first to leverage Docker layer caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the Spring Boot fat JAR
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the JAR from the builder image
COPY --from=builder /app/target/*.jar app.jar

# Optional: set active Spring profile (can override via `-e SPRING_PROFILES_ACTIVE=prod`)
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the app port (change if different)
EXPOSE 8080

# Start the Spring Boot app
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
