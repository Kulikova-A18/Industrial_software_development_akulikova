FROM node:18-alpine

# Создаем пользователя без указания конкретных ID (или используем другие ID)
RUN addgroup appgroup && \
    adduser -G appgroup -D appuser

WORKDIR /app

# Копируем и устанавливаем зависимости
COPY package.json .
RUN npm install

# Копируем исходный код
COPY server.js .

# Меняем владельца файлов
RUN chown -R appuser:appgroup /app

# Переключаемся на непривилегированного пользователя
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=20s --timeout=5s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

CMD ["node", "server.js"]
