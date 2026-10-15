FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN ./mvnw -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S samasignal && adduser -S -G samasignal samasignal
WORKDIR /app
RUN mkdir -p /app/uploads && chown samasignal:samasignal /app/uploads
COPY --from=build --chown=samasignal:samasignal /app/target/api-0.1.0-SNAPSHOT.jar app.jar
USER samasignal
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=12 CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
