FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY demo/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]