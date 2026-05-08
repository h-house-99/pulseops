# Data Model

This document defines the first database tables for the Spring Boot backend.

## Statement

Represents one uploaded statement file.

Fields:

- `id`
- `file_name`
- `uploaded_at`
- `transaction_count`

## Transaction

Represents one row parsed from a statement.

Fields:

- `id`
- `statement_id`
- `transaction_date`
- `description`
- `merchant_name`
- `amount`
- `category`

Notes:

- `description` stores the raw text from the statement.
- `merchant_name` stores the cleaned merchant name used by the app.
- `amount` should be positive for charges in version 1.

## Subscription

Represents a recurring charge detected from transactions.

Fields:

- `id`
- `merchant_name`
- `category`
- `estimated_monthly_amount`
- `last_charged_date`
- `frequency`
- `status`

Example `frequency` values:

- `MONTHLY`
- `ANNUAL`
- `UNKNOWN`

Example `status` values:

- `ACTIVE`
- `IGNORED`
- `CANCELLED`

## Relationships

```text
Statement 1 -> many Transactions
Subscription is detected from many Transactions
```

For version 1, subscriptions can be stored separately from transactions. Later, we can add a join table if we want to track exactly which transactions caused each subscription detection.
