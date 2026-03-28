# Real-Time Collaborative Dashboard System

A microservices-based real-time dashboard platform that enables teams to create, share, and collaborate on data visualization dashboards with instant updates across all connected users.

## 🎯 Project Overview

This application demonstrates a production-ready architecture for building real-time collaborative applications. It showcases:

- **Microservices Architecture** - Separate services for authentication, dashboard management, and real-time communication
- **Event-Driven Design** - Redis Streams for asynchronous event processing
- **Real-Time Updates** - WebSocket connections for instant collaboration
- **Secure Authentication** - JWT-based auth with refresh token mechanism
- **Scalable Infrastructure** - Docker containerization with PostgreSQL and Redis

## 🏗️ Architecture

```
┌─────────────────┐
│   React Frontend │
│   (Port 5173)    │
└────────┬─────────┘
         │
         ├──── HTTP REST ────────┐
         │                       │
         ├──── WebSocket ────┐   │
         │                   │   │
┌────────▼────────┐  ┌───────▼───▼──────┐  ┌─────────────────┐
│  Auth Service   │  │ Dashboard Service │  │ Realtime Gateway│
│  (Port 8081)    │  │   (Port 8082)     │  │  (Port 8083)    │
└────────┬────────┘  └────────┬──────────┘  └────────┬────────┘
         │                    │                       │
         │                    │                       │
┌────────▼────────┐  ┌────────▼──────────┐  ┌────────▼────────┐
│  PostgreSQL     │  │   PostgreSQL      │  │  Redis Streams  │
│  (Auth DB)      │  │  (Dashboard DB)   │  │  (Events)       │
└─────────────────┘  └───────────────────┘  └─────────────────┘
```

## ✨ Features

### Current Features
- ✅ User authentication (signup/login) with JWT tokens
- ✅ Token refresh mechanism for seamless sessions
- ✅ Create and manage multiple dashboards
- ✅ Add/delete widgets (metric, chart, table, text types)
- ✅ Real-time widget updates across all connected users
- ✅ WebSocket-based live collaboration
- ✅ Event-driven architecture with Redis Streams
- ✅ Secure ownership-based access control
- ✅ Visual widget rendering with mock data

### Widget Types
1. **Metric Widget** - Display key performance indicators with live updates
2. **Chart Widget** - Bar chart visualization with animated data
3. **Table Widget** - Tabular data display with status badges
4. **Text Widget** - Rich text content for notes and alerts

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)
- Java 21+ (for backend development)

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/Gowtham33-3/realdash.git
cd realdash
```

2. **Start backend services**
```bash
cd backend
docker-compose up -d
```

This starts:
- Auth Service (http://localhost:8081)
- Dashboard Service (http://localhost:8082)
- Realtime Gateway (http://localhost:8083)
- PostgreSQL databases
- Redis

3. **Start frontend**
```bash
cd frontend/realtime-dashboard
npm install
npm run dev
```

Frontend runs at http://localhost:5173

4. **Access the application**
- Open http://localhost:5173
- Sign up for a new account
- Create a dashboard
- Add widgets and see real-time updates!

## 📁 Project Structure

```
realdash/
├── backend/
│   ├── auth-service/          # JWT authentication & user management
│   ├── dashboard-service/     # Dashboard & widget CRUD operations
│   ├── realtime-gateway/      # WebSocket server & event streaming
│   └── docker-compose.yml     # Container orchestration
│
└── frontend/
    └── realtime-dashboard/    # React + Redux frontend
        ├── src/
        │   ├── api/           # HTTP & WebSocket clients
        │   ├── components/    # React components
        │   ├── pages/         # Page components
        │   ├── store/         # Redux state management
        │   ├── types/         # TypeScript types
        │   └── websocket/     # WebSocket connection logic
        └── package.json
```

## 🔧 Technology Stack

### Backend
- **Spring Boot 3.5** - Java framework
- **PostgreSQL 16** - Relational database
- **Redis 7** - Event streaming & caching
- **JWT (jjwt)** - Authentication tokens
- **Hibernate/JPA** - ORM
- **WebSocket** - Real-time communication
- **Docker** - Containerization

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Redux Toolkit** - State management
- **Axios** - HTTP client
- **Vite** - Build tool
- **WebSocket API** - Real-time updates

## 🔐 Security

- **JWT Authentication** - Stateless token-based auth
- **Refresh Tokens** - Automatic token renewal
- **Ownership Validation** - Users can only modify their own dashboards
- **CORS Protection** - Configured for localhost:5173
- **Password Hashing** - BCrypt encryption
- **WebSocket Auth** - Token validation on connection

## 📊 Real-Time Flow

1. User performs action (add/update/delete widget)
2. Dashboard Service saves to PostgreSQL
3. Dashboard Service publishes event to Redis Streams
4. Realtime Gateway consumes event from Redis
5. Gateway broadcasts via WebSocket to all connected clients
6. All users see update instantly without page refresh

## 🎓 Use Cases

This architecture is suitable for:

- **Business Analytics Dashboards** - Real-time KPI monitoring
- **DevOps Monitoring** - Server health and metrics
- **Project Management** - Sprint progress tracking
- **Customer Support** - Live ticket queue monitoring
- **E-commerce Analytics** - Sales and inventory tracking
- **IoT Dashboards** - Sensor data visualization

## 🛠️ Development

### Backend Development

```bash
cd backend/auth-service  # or dashboard-service, realtime-gateway
./gradlew clean build
./gradlew bootRun
```

### Frontend Development

```bash
cd frontend/realtime-dashboard
npm run dev          # Development server
npm run build        # Production build
npm run lint         # Lint code
```

### Rebuild Docker Images

```bash
cd backend
docker-compose down
docker-compose up --build -d
```

## 📝 API Endpoints

### Auth Service (8081)
- `POST /auth/signup` - Create new user
- `POST /auth/login` - Login and get tokens
- `POST /auth/refresh` - Refresh access token

### Dashboard Service (8082)
- `GET /dashboards` - Get user's dashboards
- `POST /dashboards?name=<name>` - Create dashboard
- `POST /dashboards/{id}/widgets` - Add widget
- `PUT /dashboards/{id}/widgets/{widgetId}` - Update widget
- `DELETE /dashboards/{id}/widgets/{widgetId}` - Delete widget

### Realtime Gateway (8083)
- `WS /ws/dashboard/{dashboardId}?token=<jwt>` - WebSocket connection

## 🔮 Future Enhancements

- [ ] Real data source integration (APIs, databases)
- [ ] Chart library integration (Recharts, Chart.js)
- [ ] Widget configuration UI
- [ ] Dashboard sharing with other users
- [ ] Role-based access control
- [ ] Widget resize and drag-drop
- [ ] Export dashboard as PDF/image
- [ ] Dashboard templates
- [ ] Scheduled data refresh
- [ ] Alert notifications
- [ ] Dark mode theme

## 🤝 Contributing

This is a learning/portfolio project. Feel free to fork and experiment!

## 📄 License

MIT License - feel free to use this project for learning and portfolio purposes.

## 👤 Author

**Gowtham**
- GitHub: [@Gowtham33-3](https://github.com/Gowtham33-3)
- Project: [realdash](https://github.com/Gowtham33-3/realdash)

## 🙏 Acknowledgments

This project demonstrates modern microservices patterns inspired by:
- Netflix's microservices architecture
- Slack's real-time messaging system
- Grafana's dashboard design
- Spring Boot best practices
- React/Redux patterns

---

**Built with ❤️ using Spring Boot, React, Redis, and PostgreSQL**
