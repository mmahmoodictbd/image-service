FROM openjdk:17-alpine as builder
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src
RUN sed -i "s/localhost:3306/mariadb:3306/g" src/main/resources/logback-spring.xml
RUN ./mvnw package -DskipTests

FROM openjdk:17-alpine
COPY --from=builder target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
