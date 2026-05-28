# Habit Tracker

Habit Tracker to pelna aplikacja webowa do zarzadzania nawykami. Projekt zawiera backend Spring Boot z REST API, JWT Security, PostgreSQL uruchamiany przez Docker Compose oraz prosty frontend Angular.

Uzytkownik moze zalozyc konto, zalogowac sie, dodawac wlasne nawyki, edytowac je, usuwac, oznaczac wykonanie danego dnia, cofac wykonanie, ogladac statystyki oraz historie aktywnosci. Kazdy uzytkownik widzi tylko swoje dane.

## 1. Jak odpalic projekt

### Wymagania

Na komputerze musza byc dostepne:

- Java JDK 26
- Docker Desktop dla Windows
- Node.js z npm

Mavena nie trzeba instalowac globalnie. Projekt ma Maven Wrapper, czyli pliki `mvnw` i `mvnw.cmd`.

Sprawdzenie narzedzi:

```powershell
java -version
docker version
docker compose version
node --version
npm --version
```

Jesli `docker version` pokazuje blad typu `failed to connect to the docker API`, uruchom aplikacje Docker Desktop i poczekaj, az silnik Dockera wystartuje.

### Krok 1: uruchom PostgreSQL

W katalogu glownym projektu:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI
docker compose up -d
```

Sprawdzenie, czy baza dziala:

```powershell
docker ps
```

Powinien byc widoczny kontener:

```text
habit_tracker_postgres
```

Konfiguracja bazy:

```text
Host: localhost
Port: 5432
Database: habit_tracker
User: habit_user
Password: habit_password
```

### Krok 2: uruchom backend

Otworz drugi terminal:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\backend
.\mvnw.cmd spring-boot:run
```

Backend dziala pod adresem:

```text
http://localhost:8080
```

### Krok 3: uruchom frontend

Otworz trzeci terminal:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\frontend
npm install
npm start
```

Frontend dziala pod adresem:

```text
http://localhost:4200
```

### Pelna kolejnosc komend

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

Terminal 3:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\frontend
npm start
```

Nastepnie wejdz w przegladarce na:

```text
http://localhost:4200
```

## 2. Jak obslugiwac aplikacje

### Rejestracja

1. Wejdz na `http://localhost:4200`.
2. Przejdz do widoku rejestracji.
3. Podaj username, email i haslo.
4. Po poprawnej rejestracji aplikacja zapisze token JWT w `localStorage` i przeniesie uzytkownika do dashboardu.

Walidacja rejestracji:

- username: wymagany, 3-40 znakow
- email: wymagany, poprawny format
- password: wymagane, minimum 6 znakow

### Logowanie

1. Wejdz do widoku logowania.
2. Podaj email i haslo.
3. Po zalogowaniu trafisz do dashboardu.

Token JWT jest automatycznie dolaczany do requestow przez Angular HTTP interceptor.

### Dashboard

Dashboard zawiera:

- gorny pasek z nazwa aplikacji i przyciskiem logout
- statystyki uzytkownika
- formularz dodawania albo edycji habitu
- liste kart habitow
- panel szczegolow z historia wykonania wybranego habitu

### Dodawanie habitu

W formularzu podaj:

- nazwe
- opis
- kategorie
- czestotliwosc
- target count
- status active

Kategorie:

- `HEALTH`
- `STUDY`
- `WORK`
- `FITNESS`
- `PERSONAL`
- `OTHER`

Czestotliwosci:

- `DAILY`
- `WEEKLY`

Po dodaniu habitu backend publikuje `HabitCreatedEvent`, a listener zapisuje wpis w `ActivityLog`.

### Edycja habitu

Na karcie habitu kliknij `Edit`. Formularz wypelni sie danymi wybranego habitu. Po zapisaniu backend publikuje `HabitUpdatedEvent`.

### Usuwanie habitu

Na karcie habitu kliknij `Delete`. Habit zostanie usuniety razem z wykonaniami. Backend publikuje `HabitDeletedEvent`.

### Oznaczanie wykonania

Na karcie habitu kliknij `Done today`. Aplikacja wysyla date dzisiejsza do backendu. Backend:

- zapisuje `HabitCompletion`
- blokuje drugi wpis dla tego samego habitu i dnia
- publikuje `HabitCompletedEvent`
- aktualizuje statystyki

Jesli sprobujesz oznaczyc ten sam habit drugi raz tego samego dnia, API zwroci blad `409 Conflict`.

### Cofanie wykonania

Na karcie habitu kliknij `Undo today`. Backend usuwa wykonanie dla dzisiejszej daty i publikuje `HabitUncompletedEvent`.

### Statystyki

Aplikacja pokazuje:

- liczbe aktywnych habitow
- liczbe wykonanych habitow w aktualnym miesiacu
- current streak dla habitu
- best streak dla habitu
- informacje, czy habit zostal wykonany dzisiaj

### Activity log

Widok `Activity` pokazuje ostatnie akcje uzytkownika:

- data
- typ eventu
- wiadomosc

Przykladowe eventy:

- `HABIT_CREATED`
- `HABIT_UPDATED`
- `HABIT_DELETED`
- `HABIT_COMPLETED`
- `HABIT_UNCOMPLETED`

### Wylogowanie

Kliknij `Logout`. Frontend usuwa JWT z `localStorage` i przenosi uzytkownika do logowania.

## 3. Jak wejsc do PostgreSQL

PostgreSQL nie otwiera sie w przegladarce przez `http://localhost:5432`. Port `5432` sluzy do polaczenia klienta bazodanowego.

### Opcja 1: psql w kontenerze

```powershell
docker exec -it habit_tracker_postgres psql -U habit_user -d habit_tracker
```

Przydatne komendy:

```sql
\dt
SELECT * FROM users;
SELECT * FROM habits;
SELECT * FROM habit_completions;
SELECT * FROM activity_logs;
\q
```

### Opcja 2: DBeaver albo pgAdmin

Dane polaczenia:

```text
Host: localhost
Port: 5432
Database: habit_tracker
User: habit_user
Password: habit_password
```

## 4. Testy

Backend:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\backend
.\mvnw.cmd test
```

Oczekiwany wynik:

```text
Tests run: 11, Failures: 0, Errors: 0
BUILD SUCCESS
```

Frontend:

```powershell
cd C:\Users\jkbpo\Documents\ZTPAI\frontend
npm run build
```

## 5. Technologie

### Backend

- Java 26
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security
- JWT przez biblioteke `jjwt`
- Bean Validation
- PostgreSQL
- Maven Wrapper
- JUnit 5
- Mockito

Backend ma strukture pakietow:

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

### Frontend

- Angular
- TypeScript
- Angular Router
- Reactive Forms
- HTTP Client
- HTTP interceptor dla JWT
- Route guard dla chronionych widokow
- localStorage do przechowywania tokenu JWT

### Baza danych

- PostgreSQL 16 Alpine
- Docker Compose
- Hibernate `ddl-auto: update` dla developmentu

### Security

Publiczne endpointy:

```http
POST /api/auth/register
POST /api/auth/login
```

Pozostale endpointy wymagaja naglowka:

```http
Authorization: Bearer <token>
```

Hasla sa hashowane przez BCrypt. Backend sprawdza wlasciciela zasobu, wiec uzytkownik nie moze odczytac, edytowac ani usunac habitow innego uzytkownika.

### DTO

Kontrolery nie zwracaja encji JPA bezposrednio. Dane przechodza przez DTO:

- `RegisterRequest`
- `LoginRequest`
- `AuthResponse`
- `HabitCreateRequest`
- `HabitUpdateRequest`
- `HabitResponse`
- `HabitCompletionRequest`
- `HabitCompletionResponse`
- `HabitStatisticsResponse`
- `DashboardStatisticsResponse`
- `ActivityLogResponse`

### Globalna obsluga bledow

Backend ma `@ControllerAdvice`, ktory zwraca bledy w jednym formacie:

```json
{
  "timestamp": "2026-05-26T12:00:00",
  "status": 400,
  "error": "Validation failed",
  "message": "Invalid request data",
  "path": "/api/habits",
  "details": {
    "name": "Habit name is required"
  }
}
```

Obslugiwane przypadki:

- bledy walidacji
- zasob nie istnieje
- bledne dane logowania
- brak autoryzacji
- brak dostepu do zasobu
- duplikat wykonania habitu w tym samym dniu
- duplikat emaila przy rejestracji

### Spring Events

Po akcjach biznesowych `HabitService` publikuje eventy:

- `HabitCreatedEvent`
- `HabitUpdatedEvent`
- `HabitDeletedEvent`
- `HabitCompletedEvent`
- `HabitUncompletedEvent`

`ActivityLogListener` odbiera eventy i zapisuje wpisy do tabeli `activity_logs`.

Przyklad:

```text
eventType: HABIT_CREATED
message: Created habit: Read 20 pages
```

## 6. REST API

### Auth

```http
POST /api/auth/register
POST /api/auth/login
```

### Habits

```http
GET /api/habits
GET /api/habits/{id}
POST /api/habits
PUT /api/habits/{id}
DELETE /api/habits/{id}
```

### Completions

```http
POST /api/habits/{id}/completions
DELETE /api/habits/{id}/completions/{date}
GET /api/habits/{id}/completions
GET /api/habits/{id}/completions/month?year=2026&month=5
```

### Statistics

```http
GET /api/habits/statistics
GET /api/habits/{id}/statistics
```

### Activity

```http
GET /api/activity
```

## 7. Najczestsze problemy

### `mvn` nie jest rozpoznawany

Nie uzywaj `mvn`. Uzyj Maven Wrapper:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

### Docker nie laczy sie z API

Blad:

```text
failed to connect to the docker API
```

Rozwiazanie:

1. Uruchom Docker Desktop.
2. Poczekaj, az Docker bedzie gotowy.
3. Sprawdz:

```powershell
docker version
docker compose up -d
```

### `localhost:5432` nie dziala w przegladarce

To normalne. PostgreSQL nie jest strona HTTP. Uzyj `psql`, DBeaver albo pgAdmin.

### Port 8080 albo 4200 jest zajety

Sprawdz proces:

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :4200
```

Zatrzymaj proces albo zmien port w konfiguracji.

## 8. Checklist pod ocene 5.0

- [x] Rejestracja i logowanie
- [x] JWT Security
- [x] BCrypt dla hasel
- [x] REST API dla habitow
- [x] Completion i undo completion
- [x] Statystyki dashboardu
- [x] Current streak i best streak
- [x] Activity log
- [x] Spring Events
- [x] DTO zamiast encji w kontrolerach
- [x] Bean Validation
- [x] Globalna obsluga bledow
- [x] PostgreSQL przez Docker Compose
- [x] Frontend Angular
- [x] Interceptor JWT i route guard
- [x] Unit testy JUnit 5 + Mockito
