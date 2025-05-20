FROM maven:3.9.2-eclipse-temurin-20 AS build

WORKDIR /app

COPY . .

RUN mvn -pl client -am package -DskipTests

FROM eclipse-temurin:20-jre

WORKDIR /app

COPY --from=build /app/client/target/client-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]