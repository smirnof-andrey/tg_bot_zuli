# Используем официальный образ Maven с Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline
# Копируем исходный код
COPY src ./src
# Собираем JAR
RUN mvn clean package -DskipTests

# Финальный образ с Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV BOT_TOKEN= $(cat /run/secrets/tg_zuli_token)
ENV BOT_USERNAME=$(cat /run/secrets/tg_zuli_username)
COPY target/*.jar app.jar
EXPOSE 8081
# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
