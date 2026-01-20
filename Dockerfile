# Use a minimal, production-ready Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the JAR built by Maven/Gradle into the container
COPY target/user-management-system.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 2023

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
