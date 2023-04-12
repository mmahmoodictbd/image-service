FROM openjdk:17-alpine as builder

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

FROM openjdk:17-alpine

COPY --from=builder target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
