
```
library-app/
├── docker-compose.yml
├── init-scripts/
│   ├── 01-cleanup.sql
│   ├── 02-init.sql
│   ├── 03-seed.sql
│   └── 04-start-app.sh
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── library/
│   │               ├── Main.java
│   │               ├── config/
│   │               │   └── DatabaseConfig.java
│   │               ├── dao/
│   │               │   ├── BookDAO.java
│   │               │   ├── ReaderDAO.java
│   │               │   └── LoanDAO.java
│   │               ├── model/
│   │               │   ├── Book.java
│   │               │   ├── Reader.java
│   │               │   └── Loan.java
│   │               ├── service/
│   │               │   ├── BookService.java
│   │               │   ├── ReaderService.java
│   │               │   ├── LoanService.java
│   │               │   └── StatisticsService.java
│   │               └── ui/
│   │                   └── ConsoleUI.java
│   └── resources/
│       └── application.properties
└── pom.xml
```

instruction

# Остановить все контейнеры
sudo docker stop $(sudo docker ps -aq)
# Удалить все контейнеры
sudo docker rm $(sudo docker ps -aq)
# Проверить, свободен ли порт
sudo netstat -tlnp | grep 5432
# Проверить, запущен ли PostgreSQL локально
sudo systemctl status postgresql
# Остановить локальный PostgreSQL
sudo systemctl stop postgresql
# Отключить автозапуск (опционально)
sudo systemctl disable postgresql

sudo docker-compose down -v
sudo docker-compose build --no-cache
sudo docker-compose up -d

apt install maven


mvn clean -U compile dependency:copy-dependencies

java -cp "target/classes:target/dependency/*" com.library.Main