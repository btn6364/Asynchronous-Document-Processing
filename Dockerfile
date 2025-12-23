# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first to leverage Docker layer cache for dependencies
COPY pom.xml ./
RUN mvn -B -DskipTests -q dependency:go-offline

# Copy source and build artifact
COPY src ./src
RUN mvn -B -DskipTests -q clean package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage (uses wildcard to match the generated jar)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]