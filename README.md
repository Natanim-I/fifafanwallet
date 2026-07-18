# EtetePay

A Spring Boot REST API for managing multi-currency digital wallets with EtetePay. Users can register with email verification, reset their password, submit KYC identity documents, hold wallets in multiple currencies, transfer funds, exchange between wallets using live rates, set category-based budgets, make merchant payments, and view filtered transaction history — all secured with JWT authentication.

## Features

- **User accounts** — Registration, profile details, and JWT-based auth with refresh tokens
- **Email verification** — New accounts receive a verification link; unverified users cannot log in
- **Password reset** — Request a reset link by email, set a new password with a time-limited token, and receive a confirmation email after a successful reset
- **KYC identity verification** — Submit personal details and ID images (front, back, selfie); documents are uploaded to AWS S3, the profile moves to `PENDING` for review, and status can be checked at any time
- **Multi-currency wallets** — Create wallets in supported currencies (USD, EUR, GBP, JPY, and more)
- **Transactions** — Deposit, withdraw, peer-to-peer transfer, and cross-currency exchange
- **Transaction search** — Filter history by type, currency, date range, and amount
- **Live exchange rates** — Fetched from the [Frankfurter API](https://api.frankfurter.dev)
- **Category-based budgets** — Create budgets by spending category and period, with overlap protection and period validation
- **Merchant payments** — Pay from a wallet, record the transaction, and automatically update the matching active budget (with cross-currency conversion when needed)
- **Structured logging** — Request/response timing via servlet filter, AOP-based service audit logs, and separate rolling log files for app, endpoint, and error output
- **CORS** — Configurable allowed browser origins for frontend apps (local dev and deployed Amplify URLs)

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT (jjwt 0.13) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| Logging | Logback + Spring AOP (AspectJ) |
| File storage | AWS S3 (AWS SDK v2) |
| Build | Maven |
| Containerization | Docker + Docker Compose |

## Prerequisites

- **Java 25** (JDK)
- **Maven** (or use the included `./mvnw` wrapper)
- **PostgreSQL 16** (for local development without Docker)
- **Docker & Docker Compose** (optional, for containerized setup)
- **AWS credentials** with S3 write access (required for KYC document uploads)

## Getting Started

### Option 1: Docker Compose

1. Create a `.env` file in the project root:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
DB_URL=jdbc:postgresql://postgres:5432/etete-pay
JWT_SECRET=your_base64_encoded_secret_key
JWT_REFRESH_EXPIRATION=604800000
MAIL_USERNAME=your_smtp_username
MAIL_PASSWORD=your_smtp_password
AWS_REGION=us-east-1
AWS_S3_BUCKET_NAME=your-kyc-bucket
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
FRONTEND_URL=http://localhost:5173
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> `JWT_SECRET` must be a Base64-encoded key suitable for HMAC-SHA256 signing.
>
> To enable email verification, password reset, KYC uploads, and CORS in Docker, also add the remaining variables to the `app` service `environment` block in `docker-compose.yml`:
>
> ```yaml
> SPRING_MAIL_HOST: ${SPRING_MAIL_HOST}
> SPRING_MAIL_PORT: ${SPRING_MAIL_PORT}
> FRONTEND_URL: ${FRONTEND_URL}
> AWS_REGION: ${AWS_REGION}
> AWS_S3_BUCKET_NAME: ${AWS_S3_BUCKET_NAME}
> AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
> AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
> CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
> ```
>
> The S3 client uses the default AWS credential chain. In Docker, pass credentials via `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`, or configure an IAM role when running on AWS.

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

2. Edit `application.properties` with your database credentials, JWT secret, and SMTP settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/etete_pay
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_base64_encoded_secret_key
jwt.refresh-expiration=2592000000

# Email (SMTP) Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Frontend URL (for verification and password-reset email links)
frontend.url=http://localhost:5173

# Comma-separated browser origins allowed to call the API
cors.allowed-origins=http://localhost:5173

# AWS S3 (KYC document uploads)
aws.region=us-east-1
aws.s3.bucket-name=your-kyc-bucket
```

For local development, also configure AWS credentials using the standard environment variables:

```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
```

3. Create the PostgreSQL database:

```sql
CREATE DATABASE etete_pay;
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
| `POST` | `/api/user/forgot-password` | Request a password reset email |
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
| `GET` | `/api/user/verify` | Verify user account via token query parameter |
| `POST` | `/api/user/resend-verification` | Resend verification email |
| `POST` | `/api/user/forgot-password` | Send a password reset email |
| `POST` | `/api/user/reset-password` | Reset password using a reset token |
| `GET` | `/api/user/details` | Get authenticated user profile |
| `GET` | `/api/user/kyc-status` | Get the authenticated user's KYC status |
| `POST` | `/api/user/submit-kyc` | Submit KYC details and identity images |

#### Registration Requirements
- **Password**: Must be at least 8 characters, containing at least one uppercase letter, one lowercase letter, one number, and one special character.
- **Country**: Must be one of the supported `Country` enum values (see [Supported Countries](#supported-countries)).

Example request:

```json
{
  "email": "user@example.com",
  "password": "SecurePass1!",
  "firstName": "John",
  "lastName": "Doe",
  "country": "ETHIOPIA"
}
```

#### Verification Details
- **Verify Account**: `GET /api/user/verify?verificationToken={token}`
  Validates the token. Expiry is 1 hour from generation. If the account is already verified, returns a success message without error.
- **Resend Verification**: `POST /api/user/resend-verification`
  If the account is already verified, returns a message indicating the user can sign in. Request body structure:
  ```json
  {
    "email": "user@example.com",
    "verificationToken": "existing-expired-token"
  }
  ```

#### Password Reset Details
- **Forgot Password**: `POST /api/user/forgot-password`
  Request body:
  ```json
  {
    "email": "user@example.com"
  }
  ```
  Sends a reset link to the user's email. The link expires 1 hour after generation.
- **Reset Password**: `POST /api/user/reset-password`
  Request body:
  ```json
  {
    "newPassword": "NewSecure1!",
    "token": "reset-token-from-email-link"
  }
  ```
  The new password must meet the same complexity rules as registration. The reset token is single-use and deleted after a successful reset. A confirmation email is sent after the password is updated.

#### KYC

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/user/kyc-status` | Get the authenticated user's current KYC status (authenticated) |
| `POST` | `/api/user/submit-kyc` | Submit KYC details and identity images (authenticated) |

A `KYCProfile` is created automatically when a user registers, with status `NOT_STARTED` and level `NONE`. The submit endpoint updates that profile, uploads three images to S3, sets the status to `PENDING`, and sets the level to `BASIC`.

**Content type:** `multipart/form-data`

| Part | Type | Description |
|------|------|-------------|
| `kycRequest` | JSON | Personal details (see below) |
| `idFrontImage` | File | Front of government ID |
| `idBackImage` | File | Back of government ID |
| `selfieImage` | File | Selfie photo |

**`kycRequest` fields:**

| Field | Rules |
|-------|-------|
| `faydaNumber` | Required; exactly 12 characters |
| `firstName` | Required; max 50 characters |
| `middleName` | Optional; max 50 characters |
| `lastName` | Required; max 50 characters |
| `dateOfBirth` | Required; must be in the past and user must be at least 18 |
| `phoneNumber` | Required; valid Ethiopia (`+251` / `251` / `0` + `9xxxxxxxx`) or US format |

**Image rules:**

- Allowed types: `image/jpeg`, `image/jpg`, `image/png`
- Maximum size: 5 MB per file
- Files are stored in S3 under `kyc/{userId}/{imageType}-{uuid}.{ext}`

Example response (submit or status):

```json
{
  "kycId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "submittedAt": "2026-07-16T18:30:00"
}
```

`submittedAt` is `null` until KYC documents have been submitted.

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

### Supported Countries

`ETHIOPIA`, `UNITED_STATES`, `UNITED_KINGDOM`, `CANADA`, `ITALY`

Used at registration and returned on the user profile.

### Supported Currencies

`USD`, `EUR`, `GBP`, `JPY`, `CHF`, `CAD`, `AUD`, `CNY`, `INR`, `BRL`, `MXN`, `RUB`, `ZAR`, `AED`, `SGD`, `ETB`

### Budget Categories

`FOOD`, `HOTEL`, `TICKET`, `TRANSPORT`, `MERCHANDISE`, `OTHER`

### Budget Periods

`TRIP`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`

### Transaction Types

`DEPOSIT`, `WITHDRAW`, `TRANSFER_IN`, `TRANSFER_OUT`, `EXCHANGE_IN`, `EXCHANGE_OUT`, `PAYMENT`

### Transaction Statuses

`PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`, `REVERSED`

### Payment Statuses

`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

### Wallet Statuses

`ACTIVE`, `DISABLED`

### KYC Statuses

`NOT_STARTED`, `PENDING`, `VERIFIED`, `REJECTED`

### KYC Levels

`NONE`, `BASIC`, `ENHANCED`

- `NONE` — Initial level assigned at registration
- `BASIC` — Assigned when KYC documents are submitted
- `ENHANCED` — Reserved for elevated verification tiers

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

All files roll daily and are retained for 30 days. Console output remains enabled for local development. The `logs/` directory is gitignored and created automatically at runtime.

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
| `403` | Access denied, or account not yet verified |
| `404` | User, wallet, budget, or KYC profile not found; invalid/expired verification or reset token |
| `409` | Duplicate user/wallet/budget, insufficient funds, disabled wallet |
| `500` | Email delivery failure or S3 upload failure |
| `503` | Exchange rate API unavailable |

## Project Structure

```
src/main/java/com/oasis/EtetePay/
├── annotation/      # Custom validation (phone number, age)
├── aop/             # AOP logging aspect for service-layer audit trails
├── config/          # Security, CORS, S3, REST client setup
├── controller/      # REST API endpoints
├── dto/             # Request/response records
├── enums/           # Country, currency, budget category, KYC status/level, transaction types, etc.
├── exception/       # Custom exceptions and global handler
├── filters/         # JWT authentication and request logging filters
├── model/           # JPA entities (User, Wallet, Transaction, Budget, Payment, KYCProfile, auth tokens)
├── repo/            # Spring Data repositories
└── service/         # Business logic (including KycService and S3Service)
```

## Running Tests

```bash
./mvnw test
```

## Building for Production

```bash
./mvnw clean package -DskipTests
```

The packaged JAR is written to `target/etete-pay.jar`.

## License

This project is provided as-is with no license specified.
