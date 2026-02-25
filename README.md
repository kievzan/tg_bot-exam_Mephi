# Chuser Bot - chat user extractor bot

Проект представляет собой Telegram-бота, предназначенного для получения списка участников Telegram-чата 
из истории сообщений - по отправляемому стандартному файлу выгрузки экспорта.

[[Требования]](docs/task_requirements.md)

## Документация

Подробная документация по проекту находится в файле [документации](docs/project_about.md).

Также возможно ознакомиться с [инструкцией](docs/user_instruction.md).

## Стек проекта

- Java 21.
- Maven.
  - TelegramBots (longpolling + client).
  - Jackson (core + datatype).
  - Apache POI (XLSX).
  - SLF4J.
  - JUnit 5.
  - Lombok.
- Docker.

## Запуск проекта

### Предварительные условия

Для запуска проекта необходимо:

1. Установленные Docker и Docker Compose
2. Java 21+
3. Maven

### Порядок запуска

Для запуска проекта необходимо:

1. Запустить Docker.
2. Получить токен бота у `@BotFather`.
3. Добавить полученный токен в переменные окружения. Для этого можно выбрать один из двух вариантов (далее `REAL_TOKEN` — токен, полученный от `@BotFather`):
   1. Добавить токен в переменные окружения (например, `export TELEGRAM_BOT_TOKEN=REAL_TOKEN` в `.bashrc`).
   2. Создать в корне репозитория `.env` файл с содержимым `TELEGRAM_BOT_TOKEN=REAL_TOKEN`.
4. Запустить проект `docker compose up --build` (в корне проекта).
