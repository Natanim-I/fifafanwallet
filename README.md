# FIFA Fan Wallet

A Spring Boot REST API for managing multi-currency digital wallets, built for FIFA fans planning trips and tracking match-day spending. Users can register, hold wallets in multiple currencies, transfer funds, exchange between wallets using live rates, set trip budgets, and view transaction history — all secured with JWT authentication.

## Features

- **User accounts** — Registration, profile details, and JWT-based auth with refresh tokens
- **Multi-currency wallets** — Create wallets in supported currencies (USD, EUR, GBP, JPY, and more)
- **Transactions** — Deposit, withdraw, peer-to-peer transfer, and cross-currency exchange
- **Live exchange rates** — Fetched from the [Frankfurter API](https://api.frankfurter.dev)
- **Budget tracking** — Create and manage budgets by period (trip, weekly, biweekly, monthly)
- **Payments** — Payment endpoint scaffolded (implementation in progress)

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT (jjwt 0.13) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Build | Maven |
| Containerization | Docker + Docker Compose |

## Prerequisites

- **Java 25** (JDK)
- **Maven** (or use the included `./mvnw` wrapper)
- **PostgreSQL 16** (for local development without Docker)
- **Docker & Docker Compose** (optional, for containerized setup)

## Getting Started

### Option 1: Docker Compose

1. Create a `.env` file in the project root:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
DB_URL=jdbc:postgresql://postgres:5432/fifa_fan_wallet
JWT_SECRET=your_base64_encoded_secret_key
JWT_REFRESH_EXPIRATION=604800000
```

> `JWT_SECRET` must be a Base64-encoded key suitable for HMAC-SHA256 signing.

2. Start the stack:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

### Option 2: Local Development

1. Copy the example configuration:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

2. Edit `application.properties` with your database credentials and JWT secret:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fifa_fan_wallet
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_base64_encoded_secret_key
```

3. Create the PostgreSQL database:

```sql
CREATE DATABASE fifa_fan_wallet;
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

## Authentication

Most endpoints require a Bearer token. Public routes:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/user/register` | Create a new account |
| `POST` | `/api/auth/login` | Obtain access and refresh tokens |
| `POST` | `/api/auth/refresh-token` | Refresh an expired access token |

Include the access token in subsequent requests:

```
Authorization: Bearer <access_token>
```

## API Reference

### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/user/register` | Register a new user |
| `GET` | `/api/user/details` | Get authenticated user profile |

### Wallets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/user/wallets` | List all wallets for the current user |
| `GET` | `/api/user/total-balance` | Get aggregated balance across wallets |
| `POST` | `/api/user/wallet/create` | Create a new wallet |
| `DELETE` | `/api/user/wallet/{walletId}/disable` | Disable a wallet |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/wallet/{walletId}/deposit` | Deposit funds into a wallet |
| `POST` | `/api/wallet/{walletId}/withdraw` | Withdraw funds from a wallet |
| `POST` | `/api/wallet/transfer/{senderId}/{receiverId}` | Transfer between two wallets |
| `POST` | `/api/wallet/exchange/{fromWalletId}/{toWalletId}` | Exchange currency between wallets |
| `GET` | `/api/user/transactions` | List transaction history |

### Budgets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/user/budget/all` | List all budgets |
| `GET` | `/api/user/budget/{budgetId}` | Get a single budget |
| `GET` | `/api/user/budget/{budgetId}/details` | Get budget spending details |
| `POST` | `/api/user/budget/create` | Create a new budget |
| `PUT` | `/api/user/budget/{budgetId}/update` | Update an existing budget |
| `DELETE` | `/api/user/budget/{budgetId}/delete` | Delete a budget |

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/user/make-payment` | Make a payment *(in progress)* |

## Supported Currencies

`USD`, `EUR`, `GBP`, `JPY`, `CHF`, `CAD`, `AUD`, `CNY`, `INR`, `BRL`, `MXN`, `RUB`, `ZAR`, `AED`, `SGD`, `ETH`

## Budget Categories

`FOOD`, `HOTEL`, `TICKET`, `TRANSPORT`, `MERCHANDISE`, `OTHER`

## Project Structure

```
src/main/java/com/oasis/FIFAFanWallet/
├── config/          # Security, JWT filter, REST client setup
├── controller/      # REST API endpoints
├── dto/             # Request/response records
├── enums/           # Currency, budget category, transaction types, etc.
├── exception/       # Custom exceptions and global handler
├── model/           # JPA entities (User, Wallet, Transaction, Budget, Payment)
├── repo/            # Spring Data repositories
└── service/         # Business logic
```

## Running Tests

```bash
./mvnw test
```

## Building for Production

```bash
./mvnw clean package -DskipTests
```

The packaged JAR is written to `target/fifa-fan-wallet.jar`.

## License

This project is provided as-is with no license specified.
