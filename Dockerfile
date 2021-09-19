FROM openjdk:11-jre-slim
EXPOSE 9009
ADD target/*.jar temporal-wallet.jar
ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/temporal-wallet.jar"]

