FROM maven:3.9.9-eclipse-temurin-21 AS build

ENV MAVEN_CONFIG=

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package \
	&& jar="$(ls target/*.jar | grep -v 'original' | head -n 1)" \
	&& cp "$jar" /app/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/app.jar /app/app.jar

USER app

EXPOSE 8080

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
