# Docker Setup Guide

This project uses Docker Compose to manage Kafka, Zookeeper, and PostgreSQL.

## Prerequisites

- Install [Docker](https://docs.docker.com/get-docker/)
- Install [Docker Compose](https://docs.docker.com/compose/install/)

## Quick Start

### 1. Start all services

```bash
docker-compose up -d
```

The `-d` flag runs services in detached mode (background).

### 2. Verify services are running

```bash
docker-compose ps
```

You should see:
- ✅ zookeeper (healthy)
- ✅ kafka (healthy)
- ✅ postgres (healthy)

### 3. Start your Spring Boot application

```bash
./mvnw spring-boot:run
```

Or from your IDE, run `FinancialDashboardApplication.java`

## Service Details

| Service | Host | Port | Purpose |
|---------|------|------|---------|
| **Kafka** | localhost | 9092 | Message broker for events |
| **Zookeeper** | localhost | 2181 | Kafka coordination |
| **PostgreSQL** | localhost | 5433 | Database |

## Useful Commands

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f kafka
docker-compose logs -f postgres
```

### Stop services
```bash
docker-compose stop
```

### Stop and remove everything
```bash
docker-compose down
```

### Remove volumes (delete data)
```bash
docker-compose down -v
```

### Restart a service
```bash
docker-compose restart kafka
```

## Testing Kafka Connection

### Access Kafka broker
```bash
docker exec -it kafka bash
```

### List topics
```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Create a test topic
```bash
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test-topic --partitions 1 --replication-factor 1
```

### Consume messages
```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic category-events --from-beginning
```

## Testing PostgreSQL Connection

### Access PostgreSQL
```bash
docker exec -it postgres psql -U postgres -d financial_db
```

### List tables
```sql
\dt
```

### Exit
```sql
\q
```

## Troubleshooting

### Kafka won't start
- Ensure Zookeeper started first (check logs: `docker-compose logs zookeeper`)
- Wait 10 seconds before starting the app

### Port conflicts
If ports 9092, 2181, or 5433 are already in use:
1. Edit `docker-compose.yml`
2. Change port mappings (e.g., `9093:9092`)
3. Update `application.yml` bootstrap-servers

### Data persistence
PostgreSQL data is stored in a Docker volume (`postgres_data`) and persists between restarts. To reset:
```bash
docker-compose down -v
```

## Your Application Configuration

Your app is already configured to connect to these services:

```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
  datasource:
    url: jdbc:postgresql://localhost:5433/financial_db
```

No changes needed! Just start the containers and run your app.
