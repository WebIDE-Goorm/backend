FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
	&& apt-get install -y docker.io \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
