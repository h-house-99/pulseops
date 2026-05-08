# API

This document defines the first backend endpoints the React frontend will call.

## Health Check

Used to confirm that the frontend can talk to the backend.

```http
GET /api/health
```

Example response:

```json
{
  "status": "ok"
}
```

## Upload Statement

Uploads one CSV statement file.

```http
POST /api/statements/upload
Content-Type: multipart/form-data
```

Form field:

```text
file
```

Example response:

```json
{
  "statementId": 1,
  "transactionCount": 42
}
```

## List Transactions

Returns parsed transactions.

```http
GET /api/transactions
```

Example response:

```json
[
  {
    "id": 1,
    "transactionDate": "2026-01-15",
    "merchantName": "Netflix",
    "description": "NETFLIX.COM",
    "amount": 15.49,
    "category": "Streaming"
  }
]
```

## List Subscriptions

Returns detected recurring subscriptions.

```http
GET /api/subscriptions
```

Example response:

```json
[
  {
    "id": 1,
    "merchantName": "Netflix",
    "category": "Streaming",
    "estimatedMonthlyAmount": 15.49,
    "lastChargedDate": "2026-04-15",
    "frequency": "MONTHLY",
    "status": "ACTIVE"
  }
]
```

## Update Subscription

Updates user-editable subscription fields.

```http
PATCH /api/subscriptions/{id}
```

Example request:

```json
{
  "category": "Streaming",
  "status": "ACTIVE"
}
```
