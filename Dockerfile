# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
# JVM tuning: footprint minimo + rilascio memoria all'OS dopo i picchi.
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=60 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=25 -XX:-ShrinkHeapInSteps -XX:TieredStopAtLevel=1 -Xss512k -XX:MaxMetaspaceSize=128m -XX:+UseStringDeduplication"
COPY --from=build /app/target/aeroport-web-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
