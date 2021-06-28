FROM openjdk:11-jre-slim
EXPOSE 9009
ADD target/temporal-wallet-0.0.1-SNAPSHOT.jar temporal-wallet.jar
ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/temporal-wallet.jar"]

