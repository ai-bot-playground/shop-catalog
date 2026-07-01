# shop-catalog

Serwis katalogu produktów — *read-heavy*, bez zarządzania stanem magazynowym (to robi `shop-inventory`).

**Stack:** Java 25 · Spring Boot 4.0.7 · Spring Data JPA · Flyway · Caffeine cache · PostgreSQL

## API

| Metoda | Ścieżka              | Opis                        |
|--------|----------------------|-----------------------------|
| GET    | `/products`          | lista z paginacją           |
| GET    | `/products/{id}`     | szczegóły produktu (cached) |
| GET    | `/products/search?q` | wyszukiwanie po nazwie      |

`GET /products/{id}` jest cache'owany przez Caffeine (`maximumSize=1000, expireAfterWrite=60s`).

Zdrowie serwisu: `GET /actuator/health`.

## Baza danych

Schemat zakłada Flyway przy starcie (`V1__init.sql`):

```
categories(id, name)
products(id, name, description, price, image_url, category_id → categories)
```

## Uruchomienie lokalne

```bash
./gradlew bootRun
```

Wymagana baza PostgreSQL. Przykładowe dane (Electronics, Books) ładuje Flyway automatycznie.

## Docker

```bash
./gradlew bootJar
docker build -t shop-catalog:0.0.1 .
docker run -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/catalog_db \
           -p 8080:8080 shop-catalog:0.0.1
```

## Zmienne środowiskowe

| Zmienna                  | Domyślnie | Opis                                      |
|--------------------------|-----------|-------------------------------------------|
| `SPRING_DATASOURCE_URL`  | —         | JDBC URL do PostgreSQL                    |
| `SPRING_DATASOURCE_USERNAME` | —     | użytkownik bazy                           |
| `SPRING_DATASOURCE_PASSWORD` | —     | hasło bazy                                |
| `SHOP_TEST_SUPPORT_ENABLED` | `false` | włącza endpointy testowe (POST/DELETE /products) |

## Testy

Testy akceptacyjne (Cucumber + Testcontainers) — stawiają własną bazę PostgreSQL:

```bash
./gradlew test
```

## CI / K8s

PR do dowolnego brancha uruchamia preprod gate (`.github/workflows/pr-to-main.yml`) — buduje obraz, deployuje na klaster `kind-preprod` i uruchamia cross-service acceptance suite.

Manifest K8s: `k8s/shop-catalog.yaml` (namespace `shop`, `ClusterIP:8080`).
