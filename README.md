# Subscription Analyzer

A demo full-stack web app that helps users upload transaction statements and identify recurring subscriptions or bills.

The first version will use a React frontend, a Spring Boot backend, and PostgreSQL. The long-term goal is to keep the React UI stable while rebuilding the backend in other frameworks like Go and Python.

## Project Goal

Build a resume-ready app that demonstrates:

- REST API design
- file upload handling
- CSV transaction parsing
- PostgreSQL persistence
- recurring charge detection
- React dashboard UI
- clean separation between frontend and backend

## First Demo Story

A user uploads a CSV statement. The app parses the transactions, stores them, detects recurring merchants, and shows a simple dashboard of subscriptions and estimated monthly spend.

## Tech Stack

- Frontend: React with Vite
- Backend: Java Spring Boot
- Database: PostgreSQL
- AI: added later after the core app works

## Project Structure

```text
subscription-analyzer/
  backend/
  frontend/
  docs/
```

## Learning Rule

Build one small vertical slice at a time:

1. React talks to Spring Boot.
2. React uploads a file to Spring Boot.
3. Spring Boot parses a CSV file.
4. Spring Boot saves transactions to PostgreSQL.
5. React displays transactions.
6. The backend detects recurring subscriptions.
7. React displays the subscription dashboard.
