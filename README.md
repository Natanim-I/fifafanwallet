# FIFA Fan Wallet

A Spring Boot REST API for managing multi-currency digital wallets, built for FIFA fans planning trips and tracking match-day spending. Users can register, hold wallets in multiple currencies, transfer funds, exchange between wallets using live rates, set category-based trip budgets, make merchant payments, and view filtered transaction history — all secured with JWT authentication.

## Features

- **User accounts** — Registration, profile details, and JWT-based auth with refresh tokens
- **Multi-currency wallets** — Create wallets in supported currencies (USD, EUR, GBP, JPY, and more)
- **Transactions** — Deposit, withdraw, peer-to-peer transfer, and cross-currency exchange
- **Transaction search** — Filter history by type, currency, date range, and amount
- **Live exchange rates** — Fetched from the [Frankfurter API](https://api.frankfurter.dev)
- **Category-based budgets** — Create budgets by spending category and period, with overlap protection and period validation
- **Merchant payments** — Pay from a wallet, record the transaction, and automatically update the matching active budget (with cross-currency conversion when needed)
- **Structured logging** — Request/response timing via servlet filter, AOP-based service audit logs, and separate rolling log files for app, endpoint, and error output

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT (jjwt 0.13) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Logging | Logback + Spring AOP (AspectJ) |
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
| `GET` | `/api/user/total-balance` | Get total balance across all wallets, converted to USD via live exchange rates |
| `POST` | `/api/user/wallet/create` | Create a new wallet |
| `DELETE` | `/api/user/wallet/{walletId}/disable` | Disable a wallet |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/wallet/{walletId}/deposit` | Deposit funds into a wallet |
| `POST` | `/api/wallet/{walletId}/withdraw` | Withdraw funds from a wallet |
| `POST` | `/api/wallet/transfer/{senderId}/{receiverId}` | Transfer between two wallets (same currency) |
| `POST` | `/api/wallet/exchange/{fromWalletId}/{toWalletId}` | Exchange currency between the user's own wallets |
| `GET` | `/api/user/transactions` | List transaction history with optional filters |

#### Transaction query parameters

All parameters on `GET /api/user/transactions` are optional:

| Parameter | Type | Description |
|-----------|------|-------------|
| `type` | `TransactionType` | Filter by transaction type |
| `currency` | `Currency` | Filter by wallet currency |
| `startDate` | `LocalDateTime` | Include transactions on or after this date |
| `endDate` | `LocalDateTime` | Include transactions on or before this date |
| `minAmount` | `BigDecimal` | Minimum transaction amount |
| `maxAmount` | `BigDecimal` | Maximum transaction amount |
| `amount` | `BigDecimal` | Exact transaction amount |

Example:

```
GET /api/user/transactions?type=PAYMENT&currency=USD&minAmount=10&maxAmount=500
```

### Budgets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/user/budget/all` | List all budgets |
| `GET` | `/api/user/budget/{budgetId}` | Get a single budget |
| `GET` | `/api/user/budget/{budgetId}/details` | Get budget spending details |
| `POST` | `/api/user/budget/create` | Create a new budget |
| `PUT` | `/api/user/budget/{budgetId}/update` | Update an existing budget |
| `DELETE` | `/api/user/budget/{budgetId}/delete` | Delete a budget |

#### Budget rules

- Each budget has a **currency**, **category**, **period type**, **limit**, and **date range**.
- Overlapping budgets with the same category and period type are not allowed.
- Period date ranges are validated by type:
  - `WEEKLY` — exactly 7 days
  - `BIWEEKLY` — exactly 14 days
  - `MONTHLY` — exactly 1 month
  - `TRIP` — flexible date range (start must be before end)

Example create request:

```json
{
  "currency": "USD",
  "limitAmount": 500.00,
  "type": "TRIP",
  "category": "FOOD",
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-06-30T23:59:59"
}
```

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/user/make-payment` | Make a merchant payment from a wallet |

When a payment is made, the API:

1. Validates the wallet belongs to the authenticated user and has sufficient funds
2. Deducts the amount from the wallet
3. Records a `PAYMENT` transaction tagged with the budget category
4. Finds the user's active budget matching the category and current date
5. Updates the budget's spent amount (converting via live exchange rates if the wallet and budget currencies differ)

Example request:

```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 45.50,
  "budgetCategory": "FOOD",
  "merchantName": "Stadium Cafe",
  "description": "Pre-match lunch"
}
```

Example response (`201 Created`):

```json
{
  "paymentId": "660e8400-e29b-41d4-a716-446655440001",
  "userId": "770e8400-e29b-41d4-a716-446655440002",
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "transactionId": "880e8400-e29b-41d4-a716-446655440003",
  "amount": 45.50,
  "budgetCategory": "FOOD",
  "merchantName": "Stadium Cafe",
  "description": "Pre-match lunch",
  "status": "COMPLETED",
  "createdAt": "2026-06-16T12:30:00"
}
```

## Enums

### Supported Currencies

`USD`, `EUR`, `GBP`, `JPY`, `CHF`, `CAD`, `AUD`, `CNY`, `INR`, `BRL`, `MXN`, `RUB`, `ZAR`, `AED`, `SGD`, `ETH`

### Budget Categories

`FOOD`, `HOTEL`, `TICKET`, `TRANSPORT`, `MERCHANDISE`, `OTHER`

### Budget Periods

`TRIP`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`

### Transaction Types

`DEPOSIT`, `WITHDRAW`, `TRANSFER_IN`, `TRANSFER_OUT`, `EXCHANGE_IN`, `EXCHANGE_OUT`, `PAYMENT`

### Payment Statuses

`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

## Logging & Observability

The application uses a two-layer logging setup configured in `src/main/resources/logback.xml`.

### Request logging (`RequestLoggingFilter`)

Every HTTP request is logged on entry and completion, including method, URI, response status, and elapsed time. Output goes to the `ENDPOINT_LOGGER` logger.

### Service logging (`LoggingAspect`)

Spring AOP advice records key business events and failures across the service layer:

| Event | Logged details |
|-------|----------------|
| User registration | User ID |
| Wallet create / disable | Wallet ID |
| Deposit / withdraw | Transaction ID |
| Transfer / exchange | Transaction ID |
| Budget create / update / delete | Budget ID |
| Payment | Payment ID |
| Service errors | Method name and exception |

### Log files

Logs are written to the `logs/` directory at the project root (created automatically at runtime):

| File | Logger | Contents |
|------|--------|----------|
| `logs/app.log` | `APP_LOGGER` | Service-layer audit events |
| `logs/endpoint.log` | `ENDPOINT_LOGGER` | HTTP request/response timing |
| `logs/error.log` | `ERROR_LOGGER` | Service-layer exceptions |

All files roll daily and are retained for 30 days. Console output remains enabled for local development.

## Error Handling

API errors are returned as a consistent JSON shape via `GeneralExceptionHandler`:

```json
{
  "status": 404,
  "message": "Budget not found."
}
```

| HTTP Status | Typical causes |
|-------------|----------------|
| `400` | Invalid arguments (e.g. same-wallet transfer, invalid budget dates) |
| `401` | Invalid credentials or refresh token |
| `403` | Access denied |
| `404` | User, wallet, or budget not found |
| `409` | Duplicate user/wallet/budget, insufficient funds, disabled wallet |
| `503` | Exchange rate API unavailable |

## Project Structure

```
src/main/java/com/oasis/FIFAFanWallet/
├── aop/             # AOP logging aspect for service-layer audit trails
├── config/          # Security, REST client setup
├── controller/      # REST API endpoints
├── dto/             # Request/response records
├── enums/           # Currency, budget category, transaction types, payment status, etc.
├── exception/       # Custom exceptions and global handler
├── filters/         # JWT authentication and request logging filters
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
