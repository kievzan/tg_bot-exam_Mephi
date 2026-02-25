# --- СТАДИЯ СБОРКИ ---
# Образ для сборки
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# рабочую директорию.
WORKDIR /app

# pom.xml, чтобы кешировались зависимости
COPY pom.xml ./
# Скачиваем все нужные зависимости в локальный Maven-кэш
RUN mvn -q -B dependency:go-offline

# Далее копируем исходники.
COPY src ./src

# Собираем jar и копируем зависимости в target/dependency
RUN mvn -q -B -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

# --- СТАДИЯ ЗАПУСКА ---
# Образ для запуска
FROM eclipse-temurin:21-jre-alpine

# Определяем рабочую директорию.
WORKDIR /app

# Создаем пользователя (чзапускать не из под root)
RUN adduser -D appuser
USER appuser

# Копируем приложение и зависимости
COPY --from=build /app/target/chuserbot-1.0.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/dependency ./lib

# точку входа приложения.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app.jar:lib/* ru.kievsan.chuserbot.ChuserBotApplication"]
