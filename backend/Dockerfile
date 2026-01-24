FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV TZ=Asia/Tokyo

VOLUME /tmp

WORKDIR /app

COPY target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]