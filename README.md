# Chuser Bot - chat user extractor bot

Проект представляет Telegram-бота, предназначенного для получения списка участников Telegram-чата 
из истории сообщений - по стандартному файлу выгрузки экспорта.

[[Требования]](docs/task_requirements.md)

## Документация

[Документация](docs/project_about.md).

[Инструкция](docs/user_instruction.md).

## Стек

- Java 21
- Maven
    - Lombok
    - Jackson (core + datatype)
    - SLF4J
    - JUnit 5
    - TelegramBots (longpolling + client)
    - Apache POI (XLSX)
- Docker.

## Запуск проекта

Для запуска проекта необходимо:

1. Установленные Docker и Docker Compose
2. Java 21+
3. Maven

### Порядок запуска

1. Запустить Docker.
2. Получить токен бота у `@BotFather`.
3. Добавить полученный токен в переменные окружения одним из способов (используя `REAL_TOKEN` — токен, полученный от `@BotFather`):
   - Создать в корне репозитория `.env` файл с содержимым `TELEGRAM_BOT_TOKEN=REAL_TOKEN`.
   - Добавить токен в переменные окружения (например, `export TELEGRAM_BOT_TOKEN=REAL_TOKEN` в `.bashrc`).
4. Запустить проект `docker compose up --build` (в корне проекта).
