FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package

FROM eclipse-temurin:17-alpine
COPY --from=build /app/target/app.jar /app.jar
EXPOSE 5050
ENTRYPOINT ["java", "-jar", "/app.jar"]
