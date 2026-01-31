# --- Stage 1: Build the JAR ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy only the pom.xml to download dependencies (improves caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 3. Create a non-root user for better security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# 4. Copy the compiled JAR from the build stage
# Replace 'auth-service' with your actual jar name or use a wildcard
COPY --from=build /app/target/*.jar app.jar

# 5. Define the entry point to allow passing environment variables
ENTRYPOINT ["java", "-jar", "app.jar"]