# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 eventhub \
    && useradd --system --uid 10001 --gid eventhub --home-dir /app eventhub

COPY --from=build --chown=eventhub:eventhub /build/target/eventhub-backend-*.jar /app/app.jar
USER eventhub
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl --fail --silent http://127.0.0.1:8080/api/categories > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
