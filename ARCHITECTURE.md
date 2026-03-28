# System Architecture Documentation

## Overview

This document provides an in-depth explanation of the Real-Time Collaborative Dashboard System architecture, design decisions, and data flows.

## Table of Contents
1. [High-Level Architecture](#high-level-architecture)
2. [Service Breakdown](#service-breakdown)
3. [Data Flow Diagrams](#data-flow-diagrams)
4. [Database Schema](#database-schema)
5. [Event Streaming](#event-streaming)
6. [Security Model](#security-model)
7. [Scalability Considerations](#scalability-considerations)

---

## High-Level Architecture

### Microservices Pattern

The system follows a microservices architecture with three independent services:

```
┌──────────────────────────────────────────────────────────────┐
│                        Frontend Layer                         │
│                    React + Redux + WebSocket                  │
└───────────────────────┬──────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌───────────────┐ ┌─────────────┐ ┌──────────────┐
│ Auth Service  │ │  Dashboard  │ │   Realtime   │
│               │ │   Service   │ │   Gateway    │
│ - Signup      │ │ - CRUD      │ │ - WebSocket  │
│ - Login       │ │ - Ownership │ │ - Broadcast  │
│ - JWT         │ │ - Events    │ │ - Consumer   │
└───────┬───────┘ └──────┬──────┘ └──────┬───────┘
        │                │                │
        ▼                ▼                ▼
┌───────────────┐ ┌─────────────┐ ┌──────────────┐
│  PostgreSQL   │ │ PostgreSQL  │ │    Redis     │
│   (Users)     │ │ (Dashboards)│ │  (Streams)   │
└───────────────┘ └─────────────┘ └──────────────┘
```

### Why Microservices?

1. **Separation of Concerns** - Each service has a single responsibility
2. **Independent Scaling** - Scale services based on load
3. **Technology Flexibility** - Can use different tech stacks per service
4. **Fault Isolation** - One service failure doesn't crash the system
5. **Team Autonomy** - Different teams can own different services

---

## Service Breakdown

### 1. Auth Service (Port 8081)

**Responsibility**: User authentication and authorization

**Technology Stack**:
- Spring Boot 3.5
- Spring Security
- PostgreSQL
- JWT (jjwt library)
- BCrypt password hashing

**Key Components**:
```
AuthController
    ├── signup()      → AuthService → UserRepository → PostgreSQL
    ├── login()       → AuthService → JwtService → Generate Tokens
    └── refresh()     → RefreshTokenService → Validate & Renew
```

**Database Tables**:
- `users` - User credentials and profile
- `refresh_tokens` - Active refresh tokens

**Security Features**:
- Password hashing with BCrypt
- JWT access tokens (15 min expiry)
- Refresh tokens (7 day expiry)
- Token blacklisting on logout

---

### 2. Dashboard Service (Port 8082)

**Responsibility**: Dashboard and widget management

**Technology Stack**:
- Spring Boot 3.5
- Spring Data JPA
- PostgreSQL
- Redis (for event publishing)
- Jackson (JSON processing)

**Key Components**:
```
DashboardController
    ├── getDashboards()    → DashboardService → DashboardRepository
    ├── createDashboard()  → Save to DB
    ├── addWidget()        → Save + Publish Event
    ├── updateWidget()     → Update + Publish Event
    └── deleteWidget()     → Delete + Publish Event

JwtAuthenticationFilter
    └── Validates JWT → Extracts userId → Sets SecurityContext

DashboardEventPublisher
    └── Publishes events to Redis Streams
```

**Database Tables**:
- `dashboards` - Dashboard metadata (id, name, owner_id)
- `widgets` - Widget data (id, type, config JSONB, dashboard_id)

**Event Publishing**:
Every mutation (add/update/delete) publishes an event to Redis:
```json
{
  "eventId": "uuid",
  "eventType": "WIDGET_ADDED",
  "aggregateType": "DASHBOARD",
  "aggregateId": "dashboard-id",
  "timestamp": "1234567890",
  "payload": "{...}"
}
```

---

### 3. Realtime Gateway (Port 8083)

**Responsibility**: WebSocket connections and event broadcasting

**Technology Stack**:
- Spring Boot 3.5
- Spring WebSocket
- Redis Streams
- No database (stateless)

**Key Components**:
```
WebSocketConfig
    └── Configures /ws/dashboard/* endpoint

WebSocketAuthInterceptor
    └── Validates JWT from query param

DashboardWebSocketHandler
    ├── onOpen()     → Store connection
    ├── onClose()    → Remove connection
    └── broadcast()  → Send to all dashboard subscribers

DashboardStreamConsumer
    └── Reads from Redis → Broadcasts to WebSocket clients
```

**Connection Management**:
```java
Map<String, Set<WebSocketSession>> dashboardSessions
// Key: dashboardId
// Value: Set of active WebSocket connections
```

**Consumer Loop**:
```
1. Read events from Redis Stream (XREADGROUP)
2. Parse event payload
3. Find all WebSocket sessions for that dashboard
4. Broadcast event to all sessions
5. Acknowledge message (XACK)
6. Repeat
```

---

## Data Flow Diagrams

### 1. User Authentication Flow

```
┌──────┐                ┌──────────┐              ┌──────────┐
│Client│                │  Auth    │              │PostgreSQL│
└──┬───┘                │ Service  │              └────┬─────┘
   │                    └────┬─────┘                   │
   │ POST /auth/signup       │                         │
   ├────────────────────────>│                         │
   │                         │ Hash password           │
   │                         │ Save user               │
   │                         ├────────────────────────>│
   │                         │<────────────────────────┤
   │ 201 Created             │                         │
   │<────────────────────────┤                         │
   │                         │                         │
   │ POST /auth/login        │                         │
   ├────────────────────────>│                         │
   │                         │ Validate credentials    │
   │                         ├────────────────────────>│
   │                         │<────────────────────────┤
   │                         │ Generate JWT            │
   │                         │ Create refresh token    │
   │                         ├────────────────────────>│
   │ {accessToken, refresh}  │                         │
   │<────────────────────────┤                         │
```

### 2. Add Widget Flow (Real-Time)

```
┌──────┐  ┌──────────┐  ┌──────────┐  ┌─────┐  ┌──────────┐  ┌──────┐
│User A│  │Dashboard │  │PostgreSQL│  │Redis│  │ Gateway  │  │User B│
└──┬───┘  │ Service  │  └────┬─────┘  └──┬──┘  └────┬─────┘  └──┬───┘
   │       └────┬─────┘       │           │          │           │
   │ Add Widget │              │           │          │           │
   ├───────────>│              │           │          │           │
   │            │ Validate JWT │           │          │           │
   │            │ Check owner  │           │          │           │
   │            │ Save widget  │           │          │           │
   │            ├─────────────>│           │          │           │
   │            │<─────────────┤           │          │           │
   │            │ Publish event│           │          │           │
   │            ├──────────────┼──────────>│          │           │
   │ 200 OK     │              │           │          │           │
   │<───────────┤              │           │          │           │
   │            │              │           │ Consumer │           │
   │            │              │           │ reads    │           │
   │            │              │           ├─────────>│           │
   │            │              │           │          │ Broadcast │
   │            │              │           │          ├──────────>│
   │            │              │           │          │           │
   │ WebSocket  │              │           │          │ WebSocket │
   │ update     │              │           │          │ update    │
   │<───────────┼──────────────┼───────────┼──────────┤           │
   │            │              │           │          │           │
```

### 3. WebSocket Connection Flow

```
┌──────┐              ┌──────────┐              ┌─────┐
│Client│              │ Gateway  │              │Redis│
└──┬───┘              └────┬─────┘              └──┬──┘
   │                       │                       │
   │ WS Connect            │                       │
   │ ?token=<jwt>          │                       │
   ├──────────────────────>│                       │
   │                       │ Validate JWT          │
   │                       │ Extract userId        │
   │                       │ Store session         │
   │ Connection OK         │                       │
   │<──────────────────────┤                       │
   │                       │                       │
   │                       │ Consumer reads        │
   │                       │<──────────────────────┤
   │                       │ Find sessions         │
   │                       │ for dashboard         │
   │ Event broadcast       │                       │
   │<──────────────────────┤                       │
   │                       │                       │
   │ Close connection      │                       │
   ├──────────────────────>│                       │
   │                       │ Remove session        │
```

---

## Database Schema

### Auth Service Database

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id UUID REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_user_email ON users(email);
```

### Dashboard Service Database

```sql
CREATE TABLE dashboards (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE widgets (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    dashboard_id UUID REFERENCES dashboards(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dashboard_owner ON dashboards(owner_id);
CREATE INDEX idx_widget_dashboard ON widgets(dashboard_id);
```

**JSONB Config Examples**:
```json
// Metric widget
{"title": "Active Users", "value": 1234, "trend": "+5%"}

// Chart widget
{"title": "Sales", "data": [{"x": "Mon", "y": 100}, ...]}

// Table widget
{"title": "Products", "columns": [...], "rows": [...]}
```

---

## Event Streaming

### Redis Streams Architecture

**Stream Key**: `dashboard:events`

**Consumer Group**: `realtime-gateway-group`

**Consumer Name**: `gateway-consumer-1`

### Event Structure

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "WIDGET_ADDED | WIDGET_UPDATED | WIDGET_REMOVED",
  "aggregateType": "DASHBOARD",
  "aggregateId": "dashboard-uuid",
  "version": "1",
  "timestamp": "1234567890123",
  "payload": "{\"widgetId\":\"...\",\"type\":\"...\",\"config\":{...}}"
}
```

### Why Redis Streams?

1. **Persistence** - Events are stored, not lost if consumer is down
2. **Consumer Groups** - Multiple consumers can process events
3. **Acknowledgment** - Ensures events are processed
4. **Ordering** - Events are processed in order
5. **Scalability** - Can add more consumers for load balancing

### Event Processing Guarantees

- **At-least-once delivery** - Events may be reprocessed on failure
- **Idempotency** - Frontend handles duplicate events gracefully
- **Ordering** - Events for same dashboard are ordered

---

## Security Model

### Authentication Flow

1. **Signup**: Password → BCrypt hash → Store in DB
2. **Login**: Validate password → Generate JWT + Refresh Token
3. **API Calls**: Include `Authorization: Bearer <JWT>` header
4. **Token Refresh**: Use refresh token to get new access token

### JWT Structure

```json
{
  "sub": "user-uuid",
  "roles": ["USER"],
  "iat": 1234567890,
  "exp": 1234568790
}
```

### Authorization

**Dashboard Service**:
```java
// Extract userId from JWT
UUID userId = UUID.fromString(auth.getName());

// Check ownership
if (!dashboard.getOwnerId().equals(userId)) {
    throw new AccessDeniedException("Forbidden");
}
```

**Realtime Gateway**:
```java
// Validate JWT from query param
Claims claims = jwtService.validate(token);
String userId = claims.getSubject();
```

### Security Best Practices

- ✅ Passwords hashed with BCrypt
- ✅ JWT signed with secret key
- ✅ Short-lived access tokens (15 min)
- ✅ Refresh tokens stored in database
- ✅ CORS configured for specific origin
- ✅ Ownership validation on all mutations
- ✅ WebSocket authentication required

---

## Scalability Considerations

### Horizontal Scaling

**Auth Service**:
- Stateless (can run multiple instances)
- Load balance with nginx/HAProxy
- Share PostgreSQL database

**Dashboard Service**:
- Stateless (can run multiple instances)
- Load balance with nginx/HAProxy
- Share PostgreSQL and Redis

**Realtime Gateway**:
- Stateful (maintains WebSocket connections)
- Use sticky sessions for load balancing
- Redis Streams allows multiple consumers

### Vertical Scaling

**Database**:
- PostgreSQL read replicas for queries
- Connection pooling (HikariCP)
- Indexes on frequently queried columns

**Redis**:
- Redis Cluster for high availability
- Separate Redis instances per service

### Performance Optimizations

1. **Database**:
   - Eager fetch widgets with `JOIN FETCH`
   - Connection pooling
   - Query optimization with indexes

2. **Caching**:
   - Cache dashboard metadata in Redis
   - Cache user sessions

3. **WebSocket**:
   - Compress messages
   - Batch events if high frequency
   - Heartbeat to detect dead connections

4. **Event Processing**:
   - Batch acknowledgments
   - Parallel processing with multiple consumers
   - Dead letter queue for failed events

---

## Monitoring & Observability

### Metrics to Track

- Request latency (p50, p95, p99)
- Error rates per endpoint
- Active WebSocket connections
- Redis Stream lag
- Database connection pool usage
- JWT validation failures

### Logging Strategy

- Structured logging (JSON format)
- Log levels: DEBUG, INFO, WARN, ERROR
- Correlation IDs for request tracing
- Centralized logging (ELK stack)

### Health Checks

```
GET /actuator/health
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

---

## Deployment Architecture

### Docker Compose (Development)

```yaml
services:
  auth-service:
    image: backend-auth-service
    ports: ["8081:8081"]
    depends_on: [postgres-auth]
  
  dashboard-service:
    image: backend-dashboard-service
    ports: ["8082:8082"]
    depends_on: [postgres-dashboard, redis]
  
  realtime-gateway:
    image: backend-realtime-gateway
    ports: ["8083:8083"]
    depends_on: [redis]
```

### Production (Kubernetes)

```
┌─────────────────────────────────────────┐
│           Load Balancer (Ingress)       │
└────────────┬────────────────────────────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌────────┐ ┌────────┐ ┌────────┐
│ Auth   │ │Dashboard│ │Gateway │
│ Pod x3 │ │ Pod x3  │ │ Pod x2 │
└────┬───┘ └────┬────┘ └────┬───┘
     │          │            │
     ▼          ▼            ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│PostgreSQL│ │PostgreSQL│ │  Redis  │
│ StatefulSet│ StatefulSet│ StatefulSet│
└─────────┘ └─────────┘ └─────────┘
```

---

## Design Patterns Used

1. **Microservices** - Service decomposition
2. **Event Sourcing** - Event-driven architecture
3. **CQRS** - Separate read/write models
4. **Repository Pattern** - Data access abstraction
5. **DTO Pattern** - Data transfer objects
6. **Factory Pattern** - Object creation
7. **Observer Pattern** - WebSocket subscriptions
8. **Singleton Pattern** - Service beans

---

## Trade-offs & Decisions

### Why PostgreSQL over MongoDB?
- Relational data (dashboards → widgets)
- ACID transactions
- Strong consistency
- JSONB for flexible widget config

### Why Redis Streams over Kafka?
- Simpler setup for small-medium scale
- Built-in persistence
- Lower operational overhead
- Sufficient for this use case

### Why JWT over Session Cookies?
- Stateless authentication
- Microservices friendly
- Mobile app support
- Scalability

### Why WebSocket over Server-Sent Events?
- Bidirectional communication
- Lower latency
- Better browser support
- Industry standard for real-time

---

**This architecture is production-ready and can handle thousands of concurrent users with proper infrastructure!**
