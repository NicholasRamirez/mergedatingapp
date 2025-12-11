# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and download dependencies first (better caching)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the jar
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT env var; Spring Boot picks it up via server.port=${PORT:8080}
ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
