# Build stage
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Grant execute permission for gradlew
RUN chmod +x gradlew
RUN ./gradlew --no-daemon

# Copy all subproject build files
COPY adapter adapter
COPY app app
COPY core core
COPY domain domain
COPY port port
COPY support support

# Build the application
RUN ./gradlew :app:api:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built jar from build stage
COPY --from=build /app/app/api/build/libs/*.jar app.jar

# Chown spring:spring
RUN mkdir -p /var/log/app && chown -R spring:spring /var/log/app

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring
EXPOSE 8080

ARG PHASE
ENV JAVA_OPTS="-Dspring.profiles.active=${PHASE:-sandbox}"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
