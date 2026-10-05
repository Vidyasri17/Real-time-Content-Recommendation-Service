FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q clean package

FROM maven:3.9.11-eclipse-temurin-21
WORKDIR /app
COPY --from=build /app /app
ENTRYPOINT ["java","-jar","target/profile-update-service-1.0.0.jar"]
