# Habit Tracker

## O czym jest projekt

Habit Tracker to aplikacja webowa do sledzenia nawykow. Uzytkownik moze zalozyc konto, zalogowac sie i zarzadzac swoimi habitami.

Glowne funkcje:

- rejestracja i logowanie
<img width="800" height="450" alt="ezgif-144db777b4d79c56" src="https://github.com/user-attachments/assets/457f7a63-3285-4450-9c2a-86dfbab4ed05" />

- JWT i dostep tylko do wlasnych danych
- dodawanie, edycja, usuwanie i przegladanie habitow
<img width="800" height="450" alt="ezgif-1adab7da29752ce6" src="https://github.com/user-attachments/assets/3335f6f4-4347-4f0e-9bdd-6ac95f3593e5" />

- oznaczanie habitu jako wykonanego dzisiaj
<img width="800" height="450" alt="ezgif-1c2ecc6307350291" src="https://github.com/user-attachments/assets/2d26966b-1a3c-479b-9c16-9f1d1facd4b6" />

- cofanie wykonania
- current streak i best streak
<img width="800" height="450" alt="ezgif-1c5b974fa35ee642" src="https://github.com/user-attachments/assets/e2d82cc4-41f3-4bf2-98ba-6b3f4802285d" />

- statystyki dashboardu
- historia aktywnosci zapisywana przez Spring Events

Backend nie zwraca encji JPA bezposrednio. Kontrolery pracuja na DTO. Bledy API sa obslugiwane globalnie przez `@ControllerAdvice`.

## Jak odpalac

Potrzebne:

- Java JDK 26
- Docker Desktop
- Node.js z npm

Maven nie musi byc zainstalowany globalnie, bo backend ma Maven Wrapper.

### IntelliJ IDEA

W projekcie sa gotowe run configurations w katalogu `.run`.

Odpalaj w tej kolejnosci:

1. `1 PostgreSQL Docker`
2. `2 Backend API`
3. `3 Frontend Angular`

Dodatkowo:

- `2 Backend API with Seeder` uruchamia backend i jednorazowo dodaje konto demo
- `Backend Tests` uruchamia testy backendu
- `Frontend Build` buduje frontend

Po starcie aplikacja jest pod:

```text
http://localhost:4200
```

API backendu:

```text
http://localhost:8080
```

Konto demo z historia habitow:

```text
Email: demo@example.com
Password: password123
```

### Konsola

Terminal 1:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI
docker compose up -d
```

Terminal 2:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\backend
.\mvnw.cmd spring-boot:run
```

Backend z seederem:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=seed"
```

Terminal 3:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\frontend
npm install
npm start
```

Testy backendu:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\backend
.\mvnw.cmd test
```

Build frontendu:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\frontend
npm run build
```

PostgreSQL:

```text
Host: localhost
Port: 5432
Database: habit_tracker
User: habit_user
Password: habit_password
```

Wejscie do bazy przez kontener:

```powershell
docker exec -it habit_tracker_postgres psql -U habit_user -d habit_tracker
```

Przydatne komendy w `psql`:

```sql
\dt
SELECT * FROM users;
SELECT * FROM habits;
SELECT * FROM habit_completions;
SELECT * FROM activity_logs;
```

## Technologie

Backend:

- Java 26
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (`jjwt`)
- Bean Validation
- PostgreSQL
- Maven Wrapper
- JUnit 5
- Mockito

Frontend:

- Angular
- TypeScript
- Angular Router
- Reactive Forms
- HTTP Client
- JWT interceptor
- route guard
- localStorage

Infrastruktura:

- Docker Compose
- PostgreSQL 16 Alpine

Struktura backendu:

```text
controller
service
repository
entity
dto
mapper
exception
security
event
config
```

Najwazniejsze endpointy:

```http
POST /api/auth/register
POST /api/auth/login

GET /api/habits
POST /api/habits
PUT /api/habits/{id}
DELETE /api/habits/{id}

POST /api/habits/{id}/completions
DELETE /api/habits/{id}/completions/{date}

GET /api/habits/statistics
GET /api/habits/{id}/statistics
GET /api/activity
```
