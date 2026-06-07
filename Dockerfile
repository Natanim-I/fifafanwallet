FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre

COPY --from=build /app/target/*.jar fifa-fan-wallet.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "fifa-fan-wallet.jar"]