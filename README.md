# Full-Stack Feature Flag & Movie Search Application

A comprehensive full-stack application demonstrating **real-time feature flag management** and **movie search functionality** with **Redis pub/sub communication** between microservices.

## Core Design Philosophy

### **Feature Flag System**

#### **RESTful API Design**
For usability and extensibility, we use feature names for logical operations and IDs for physical relationships. This approach facilitates future expansion with `flag_overrides` tables. For performance optimization, we implement Redis caching with intelligent cache invalidation strategies.

#### **Microservice Communication Architecture**
For feature flag propagation to other microservices, since strict ordering and persistence are not required, we chose Redis pub/sub for real-time messaging. This approach is simple and efficient, with message IDs added for tracking and performance optimization.

Other microservices (like movie search) implement startup and scheduled polling of feature REST APIs using Spring OpenFeign clients. This ensures bulk retrieval of relevant feature lists and provides fallback mechanisms. Future configurations can be optimized for connection settings and config center integration.

To prevent single points of failure, other microservices include default feature configurations for graceful degradation when the feature service is unavailable.

Since each microservice has a small set of frequently accessed features that can reside in memory, we use in-memory ConcurrentHashMap for optimal performance.

#### **Frontend Integration Strategy**
`maintenance_mode` doesn't require active polling - backend services use unified interceptors to return maintenance messages, and frontend interceptors handle these responses automatically to trigger maintenance mode.

`dark_mode` requires active retrieval. Since theme changes can tolerate some latency, we use timer-based polling for simplicity. For future real-time feature updates, we can implement Server-Sent Events (SSE) for unified real-time communication.

### **Movie Search System**

#### **External API Integration**
For optimal query performance, we use WebClient with reactive programming, which also facilitates connection parameter optimization. Failed retries throw unified `ExternalApiException` for frontend interceptor handling, providing user-friendly retry messaging. Future enhancements include fallback strategies for alternative movie data sources.

For performance and reliability, we implement Redis caching with different TTL strategies: movie data (24 hours) due to low change frequency, and search results (30 minutes) for moderate change frequency.

#### **Frontend Optimization Strategy**
We provide movie list and detail functionality without frontend Redux caching, as backend caching provides sufficient performance.

During testing, we discovered that OMDb API returns duplicate data (e.g., searching "batman"), so we implemented client-side deduplication for better UX. Backend pagination handling would be complex and could impact server performance.

#### **Additional Implementation Details**
- **Error Handling**: Comprehensive retry mechanisms with exponential backoff
- **Caching Strategy**: Multi-layer caching (Redis + in-memory) for optimal performance
- **API Rate Limiting**: Built-in throttling to respect external API limits
- **Health Monitoring**: Service health checks and comprehensive logging
- **Data Validation**: Input sanitization and response validation for security

## System Architecture

```text
┌─────────────────┐    ┌─────────────────┐
│   Feature Flag  │    │   Movie Search  │
│   Frontend      │    │   Frontend      │
│   (React)       │    │   (React)       │
│   Port: 3000    │    │   Port: 3001    │
└─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│   Feature Flag  │    │   Movie Search  │
│   Backend       │    │   Backend       │
│   (Spring Boot) │    │   (Spring Boot) │
│   Port: 8080    │    │   Port: 8081    │
└─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│     MySQL       │    │   OMDb API      │
│   (Database)    │    │   (External)    │
│   Port: 3306    │    │   (OMDb.com)    │
└─────────────────┘    └─────────────────┘
         │
         │
         ▼
┌─────────────────┐
│      Redis      │
│   (Pub/Sub)     │
│   Port: 6379    │
└─────────────────┘
         ▲
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────────────┐    ┌─────────────────┐
│   Feature Flag  │    │   Movie Search  │
│   Backend       │    │   Backend       │
│   (Publisher)   │    │   (Subscriber)  │
└─────────────────┘    └─────────────────┘
```

### Real-time Communication Flow

1. **Feature Flag Update**: Admin updates flag via Feature Flag Frontend
2. **Database Persistence**: Change saved to MySQL database by Feature Flag Backend
3. **Redis Pub/Sub**: Feature Flag Backend publishes event to Redis channel
4. **Real-time Propagation**: Movie Search Backend receives update via Redis subscriber
5. **API Response**: Movie Search Backend provides updated flag status via API
6. **Frontend Polling**: Movie Search Frontend polls Movie Search Backend for updates (30s interval)
7. **UI Update**: Theme/features update in real-time

### Data Flow

- **Movie Search Frontend** → **Movie Search Backend** → **OMDb API**
- **Feature Flag Frontend** → **Feature Flag Backend** → **MySQL Database**
- **Feature Flag Backend** → **Redis Pub/Sub** → **Movie Search Backend**

## Technical Implementation

### **Backend Architecture**

#### Feature Flag Backend (Spring Boot + MyBatis)
- RESTful API with comprehensive CRUD operations
- MySQL database persistence with MyBatis ORM
- Redis pub/sub publisher for real-time updates
- Swagger/OpenAPI documentation

#### Movie Search Backend (Spring Boot + WebFlux)
- Reactive programming with Spring WebFlux
- OMDb API integration with gzip compression support
- Redis pub/sub subscriber for feature flag updates
- Redis caching for performance optimization

### **Frontend Architecture**
- Modern React 18 with hooks and functional components
- TypeScript for type safety and better development experience
- Redux Toolkit for predictable state management
- Tailwind CSS for responsive design with dark mode support

### **Real-time Communication**
- Event-driven architecture with JSON message format
- Message deduplication using unique message IDs
- Fault tolerance with graceful degradation
- Health monitoring and comprehensive logging

## Quick Start & Demo

### **Application URLs**

| Service | URL | Description |
|---------|-----|-------------|
| **Feature Flag Frontend** | <http://localhost:3000> | Feature flag management UI |
| **Feature Flag Backend** | <http://localhost:8080> | Feature flag API |
| **Movie Search Frontend** | <http://localhost:3001> | Movie search UI |
| **Movie Search Backend** | <http://localhost:8081> | Movie search API |
| **MySQL** | localhost:3306 | Database |
| **Redis** | localhost:6379 | Cache & Pub/Sub |

### **API Documentation**
- **Feature Flag API**: <http://localhost:8080/feature/swagger-ui.html>
- **Movie Search API**: <http://localhost:8081/movie/swagger-ui.html>

### **REST API Endpoints**

#### Feature Flag Backend (Port 8080)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/feature/flags` | Get all feature flags | - | List of feature flags |
| GET | `/feature/flags/{name}` | Get specific feature flag | - | Feature flag details |
| POST | `/feature/flags` | Create new feature flag | `{"name": "string", "enabled": boolean, "description": "string"}` | Created feature flag |
| PUT | `/feature/flags/{name}` | Update feature flag | `{"enabled": boolean, "description": "string"}` | Updated feature flag |
| DELETE | `/feature/flags/{name}` | Delete feature flag | - | Success message |
| GET | `/feature/actuator/health` | Health check | - | Service health status |

#### Movie Search Backend (Port 8081)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/movie/movies/search` | Search movies | `?search={query}&page={page}` | Movie search results |
| GET | `/movie/movies/{imdbId}` | Get movie details | - | Movie details |
| GET | `/movie/movies/flags/{flagName}` | Get feature flag status | - | Feature flag status |
| GET | `/movie/actuator/health` | Health check | - | Service health status |

### **Live Demo Scenarios**

1. **Real-time Dark Mode Toggle**
   - Update feature flag in Feature Flag Frontend
   - Watch Movie Search Frontend theme change within 30 seconds
   - Monitor Redis pub/sub messages in real-time

2. **Maintenance Mode Testing**
   - Enable maintenance mode flag
   - Observe service blocking with maintenance message
   - Disable flag to restore normal operation

3. **API Integration Testing**
   - Test feature flag API endpoints
   - Verify Redis pub/sub message propagation
   - Monitor cross-service communication

### **Prerequisites**
- Java 17+, Node.js 18+, Docker & Docker Compose

### **One-Command Setup**
```bash
# Clone and start all services
git clone <repository-url>
cd fullstack_sample
cp env.example .env
docker-compose up -d --build
```

### **Verify Services**
```bash
# Check health endpoints
curl http://localhost:8080/feature/actuator/health
curl http://localhost:8081/movie/actuator/health

# Test APIs
curl http://localhost:8080/feature/flags
curl "http://localhost:8081/movie/movies/search?search=batman"
```

## Technology Stack

### Backend
- **Spring Boot 3.3.6** - Main framework
- **MyBatis** - Database ORM (Feature Flag Backend)
- **Spring Data Redis** - Redis integration and pub/sub
- **Spring WebFlux** - Reactive programming (Movie Search Backend)
- **Spring Cloud OpenFeign** - HTTP client for external APIs
- **MySQL 8.0** - Primary database
- **Redis 7.0** - Cache and Pub/Sub messaging
- **OpenAPI/Swagger** - API documentation
- **Docker** - Containerization

### Frontend
- **React 19.1.1** - UI framework
- **TypeScript 5.9.3** - Type safety
- **Redux Toolkit** - State management
- **Tailwind CSS** - Styling with dark mode support
- **Vite** - Build tool
- **Axios** - HTTP client with interceptors
- **React Router** - Navigation

### Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing
- **JaCoCo** - Code coverage
- **React Testing Library** - Frontend testing (Planning)

## Project Structure

```text
fullstack_sample/
├── feature-flag-backend/          # Feature Flag API
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── Dockerfile
│   └── pom.xml
├── feature-flag-frontend/        # Feature Flag UI
│   ├── src/
│   ├── public/
│   ├── Dockerfile
│   └── package.json
├── movie-search-backend/          # Movie Search API
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── Dockerfile
│   └── pom.xml
├── movie-search-frontend/         # Movie Search UI
│   ├── src/
│   ├── public/
│   ├── Dockerfile
│   └── package.json
├── database/                      # Database migrations
│   └── migrations/
├── docker-compose.yml             # Docker orchestration
├── .env.example                   # Environment template
└── README.md                      # This file
```

## Testing

### Run Tests

```bash
# Feature Flag Backend
cd feature-flag-backend && ./mvnw test

# Movie Search Backend  
cd movie-search-backend && ./mvnw test

# Run all tests with coverage
./mvnw verify
```

### Test Coverage
- **Backend Coverage**: >80% instruction coverage
- **Unit Tests**: Service and controller layer
- **Integration Tests**: Database and external service integration
- **End-to-End Tests**: Complete workflow testing

## Deployment

```bash
# Development
docker-compose up --build

# Production
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Configuration

### Environment Variables

Refer to `env.example` file for environment variable configuration. Copy to `.env` and modify as needed.

### Application Profiles

- **dev** - Development configuration
- **docker** - Docker environment

## Troubleshooting

### Quick Health Checks

```bash
# Check all services
curl http://localhost:8080/feature/actuator/health
curl http://localhost:8081/movie/actuator/health
docker exec -it redis redis-cli ping
docker exec -it mysql mysql -u root -p -e "SELECT 1"
```

### Common Issues

**Redis Pub/Sub not working:**
```bash
docker exec -it redis redis-cli monitor
docker-compose logs movie-search-backend | grep "PUB/SUB"
```

**Feature flags not propagating:**
```bash
curl -X PUT -H "Content-Type: application/json" -d '{"enabled": true}' http://localhost:8080/feature/flags/dark_mode
curl http://localhost:8081/movie/movies/flags/dark_mode
```

**Port conflicts:**
```bash
netstat -tulpn | grep :8080
sudo kill -9 <PID>
```



## License

This project is licensed under the MIT License.



