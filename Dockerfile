FROM openjdk:13-jdk-alpine as base 
WORKDIR /app
RUN addgroup -S waya && adduser -S waya -G waya
USER waya:waya
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=staging", "/app/app.jar"]

