FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache tzdata

ENV TZ=Asia/Tokyo

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Tokyo"

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]