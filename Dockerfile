#FROM openjdk:11-jre-slim
#EXPOSE 9009
#ADD target/*.jar temporal-wallet.jar
#ADD target/temporal-wallet-0.0.1-SNAPSHOT.jar temporal-wallet.jar
#ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/temporal-wallet.jar"]
FROM openjdk:13-jdk-alpine as base 
WORKDIR /app
RUN addgroup -S waya && adduser -S waya -G waya
USER waya:waya
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

