# Trainer Workload Service

Tracks each trainer's monthly training workload for the Gym CRM ecosystem. The main `gym-crm` service publishes an event
whenever a training session is added or canceled; this service consumes those events asynchronously via ActiveMQ,
maintains a running monthly summary per trainer, and exposes it for querying over REST.

## Tech Stack

- Java 25 / Spring Boot 4
- Spring Data MongoDB
- Spring JMS + ActiveMQ (async event consumption from gym-crm)
- Bearer-token auth for the query endpoints

## Data model

Each trainer is a single document in the `trainer_workloads` collection — years and months are embedded, not separate
collections, since they're always read and written together as one aggregate:

```
TrainerWorkload (document, _id = username)
├── firstName, lastName, active
└── years: List<WorkloadYear>
├── year
└── months: List<WorkloadMonth>
├── month
└── trainingSummaryDuration
```

A compound index on `firstName`+`lastName` (`TrainerWorkloadRepository#findByFirstNameAndLastName`) supports name-based
lookups.

## Messaging

| Queue                         | Direction | Purpose                                                 |
|-------------------------------|-----------|---------------------------------------------------------|
| `trainer-workload.events`     | consumes  | Workload events (ADD/DELETE) published by gym-crm       |
| `trainer-workload.events.dlq` | produces  | Events rejected as malformed or missing required fields |

`TrainerWorkloadEventListener` consumes `trainer-workload.events`. Each message is a JSON body matching
`WorkloadEventRequest`:

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

Before applying an event, the listener validates it against the constraints above (`@NotBlank`/`@NotNull`/`@Positive` on
`WorkloadEventRequest`) plus basic JSON well-formedness. Messages that fail either check - and are therefore missing
required information - are wrapped (raw payload + rejection reasons + timestamp) and published to
`trainer-workload.events.dlq` instead of being applied or endlessly redelivered.
`DeadLetterEventListener` consumes that queue and logs rejected events at `ERROR`, as a hook for future
alerting/persistence.

If gym-crm forwards a `transactionId` JMS property on the original event, the listener puts it into the logging MDC for
the duration of processing, so log lines for a given training operation can be correlated across both services.

## API

| Method | Path                                                               | Description                                              |
|--------|--------------------------------------------------------------------|----------------------------------------------------------|
| GET    | `/api/v1/trainer-workloads/{username}`                             | Get a trainer's full workload summary (all years/months) |
| GET    | `/api/v1/trainer-workloads/{username}/years/{year}/months/{month}` | Get a trainer's training total for one specific month    |

Workload updates no longer happen over REST - see **Messaging** above. Both GET endpoints are still protected the same
way as before (see **Security**); note that gym-crm no longer calls them directly, so today they're intended for other
consumers (e.g. an admin/reporting tool) presenting their own valid service token.

## Security

All endpoints (except `/actuator/health/**`) require a `Authorization: Bearer <token>` header carrying a JWT signed with
the shared secret configured via `SERVICE_JWT_SECRET`, containing a `type=service` claim. This service never issues
tokens itself - it only validates ones issued by trusted callers.

## Observability

Requests and events are traced using a shared `transactionId`:

* **Request logging:** `RequestLoggingInterceptor` logs request start/completion (REST endpoints only).
* **Operation logging:** `TrainerWorkloadServiceImpl` logs each business step - creation of a new trainer/year/month
  record at `DEBUG`, the resulting duration update at `INFO`, and reads at `DEBUG`.
* **Transaction ID (REST):** `TransactionIdFilter` reuses the inbound `X-Transaction-Id` or generates a new one, then
  includes it in the response and error responses.
* **Transaction ID (messaging):** `TrainerWorkloadEventListener` reads the `transactionId` JMS property (if present)
  from the incoming event and puts it into the MDC for the duration of processing.
* **Dead letters:** rejected events are logged at `ERROR` by `DeadLetterEventListener`, with the raw payload and
  rejection reasons.

## Getting Started

```bash
./gradlew bootRun
```

Requires MongoDB and ActiveMQ - `docker compose up -d` (via `compose.yaml`) starts MongoDB on port `27017` and ActiveMQ
on `61616` (web console on `8161`).

## Testing

```bash
./gradlew test
```
