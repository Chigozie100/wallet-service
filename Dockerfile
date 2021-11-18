FROM openjdk:11-jre-slim
EXPOSE 9009
<<<<<<< HEAD
ADD target/*.jar temporal-wallet.jar
=======
ADD target/temporal-wallet-0.0.1-SNAPSHOT.jar temporal-wallet.jar
>>>>>>> master
ENTRYPOINT ["java","-Dspring.profiles.active=test", "-jar", "/temporal-wallet.jar"]

