# Trainer Workload Service

Tracks each trainer's monthly training workload for the Gym CRM ecosystem. The main `gym-crm`service notifies this
service whenever a training session is added or canceled; this service maintains a running monthly summary per trainer
and exposes it for querying.

## Tech Stack

- Java 25 / Spring Boot 4
- Spring Data JPA + PostgreSQL
- Liquibase
- Eureka client + Bearer-token auth

## Data model

```
TrainerWorkload (username, firstName, lastName, active)
    WorkloadYear (year)
    WorkloadMonth (month, trainingSummaryDuration)
```

## API

| Method | Path                                                               | Description                                                  |
|--------|--------------------------------------------------------------------|--------------------------------------------------------------|
| POST   | `/api/v1/trainer-workloads`                                        | Apply a workload event (ADD/DELETE) for a trainer's training |
| GET    | `/api/v1/trainer-workloads/{username}`                             | Get a trainer's full workload summary (all years/months)     |
| GET    | `/api/v1/trainer-workloads/{username}/years/{year}/months/{month}` | Get a trainer's training total for one specific month        |

### `POST /api/v1/trainer-workloads` request body

```json
{
  "trainerUsername": "Jane.Smith",
  "trainerFirstName": "Jane",
  "trainerLastName": "Smith",
  "active": true,
  "trainingDate": "2024-06-10",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

`actionType` is `ADD` when a training session is scheduled, `DELETE` when it is canceled. The monthly total is adjusted
accordingly and never drops below zero. Trainer name/active status are refreshed on every event.

## Security

All endpoints (except `/actuator/health/**`) require a `Authorization: Bearer <token>` header carrying a JWT signed with
the shared secret configured via `SERVICE_JWT_SECRET`, containing a `type=service` claim. This service never issues
tokens itself — it only validates ones issued by trusted callers (e.g. `gym-crm`). Requests without a valid service
token get `401 Unauthorized`.

## Observability

Requests are traced using a shared `transactionId`:

* **Request logging:** `RequestLoggingInterceptor` logs request start/completion.
* **Operation logging:** `applyWorkloadEvent` logs changes at `INFO`; read operations log at `DEBUG`.
* **Transaction ID:** `TransactionIdFilter` reuses the inbound `X-Transaction-Id` or generates a new one, then includes
  it in the response and error responses.
* **Cross-service tracing:** propagated IDs allow training operations in `gym-crm` and workload sync calls to be
  correlated.

## Getting Started

```bash
./gradlew bootRun
```

Requires PostgreSQL — `docker compose up -d` (via `compose.yaml`) starts one on port `5433`.

## Testing

```bash
./gradlew test
```
