# Full-Stack Feature Flag & Movie Search Application

A comprehensive full-stack application demonstrating **real-time feature flag management** and **movie search functionality** with **Redis pub/sub communication** between microservices.

## 🎯 Core Technical Highlights

### **Real-time Microservices Communication**

- **Redis Pub/Sub**: Event-driven architecture with instant feature flag propagation
- **Message Deduplication**: Prevents duplicate processing with unique message IDs
- **Fault Tolerance**: Graceful degradation when services are unavailable

### **Modern Full-Stack Architecture**

- **Backend**: Spring Boot 3.3.6 with WebFlux reactive programming
- **Frontend**: React 18 with TypeScript and Redux Toolkit
- **Database**: MySQL 8.0 with MyBatis ORM
- **Cache**: Redis 7.0 for both caching and pub/sub messaging

### **External API Integration**

- **OMDb API**: Movie search with gzip compression support
- **Error Handling**: Comprehensive retry mechanisms and fallback strategies
- **Caching**: Redis-based response caching for performance optimization

## 🏗️ System Architecture

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

## 🛠️ Technical Implementation

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

#### React 18 + TypeScript + Redux Toolkit

- Modern React with hooks and functional components
- TypeScript for type safety and better development experience
- Redux Toolkit for predictable state management
- Tailwind CSS for responsive design with dark mode support

### **Real-time Communication**

#### Redis Pub/Sub Messaging

- Event-driven architecture with JSON message format
- Message deduplication using unique message IDs
- Fault tolerance with graceful degradation
- Health monitoring and comprehensive logging

## 🚀 Quick Demo

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

## 📱 Application URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Feature Flag Frontend** | <http://localhost:3000> | Feature flag management UI |
| **Feature Flag Backend** | <http://localhost:8080> | Feature flag API |
| **Movie Search Frontend** | <http://localhost:3001> | Movie search UI |
| **Movie Search Backend** | <http://localhost:8081> | Movie search API |
| **MySQL** | localhost:3306 | Database |
| **Redis** | localhost:6379 | Cache & Pub/Sub |

### API Documentation

- **Feature Flag API**: <http://localhost:8080/feature/swagger-ui.html>
- **Movie Search API**: <http://localhost:8081/movie/swagger-ui.html>


## 🚀 Quick Start

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

## 🏗️ Technology Stack

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

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Redux Toolkit** - State management
- **Tailwind CSS** - Styling with dark mode support
- **Vite** - Build tool
- **Axios** - HTTP client with interceptors
- **React Router** - Navigation

### Real-time Communication

- **Redis Pub/Sub** - Event-driven messaging
- **JSON Messages** - Structured event format
- **Message Deduplication** - Prevents duplicate processing
- **Health Checks** - Service monitoring

### Testing

- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing
- **JaCoCo** - Code coverage
- **React Testing Library** - Frontend testing (Planning)

## 📊 Project Structure

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
├── docs/                          # Documentation
├── docker-compose.yml             # Docker orchestration
├── .env.example                   # Environment template
└── README.md                      # This file
```

## 🧪 Testing

### Backend Testing

```bash
# Feature Flag Backend
cd feature-flag-backend
./mvnw test
./mvnw verify

# Movie Search Backend
cd movie-search-backend
./mvnw test
./mvnw verify
```

### Test Coverage

- **Backend Coverage**: >80% instruction coverage
- **Unit Tests**: Service and controller layer
- **Integration Tests**: Database and external service integration
- **End-to-End Tests**: Complete workflow testing

## 🚀 Deployment

### Development Environment

```bash
# Start all services
docker-compose up --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Production Environment

```bash
# Build production images
docker-compose build

# Deploy to production
docker-compose up -d
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `password` |
| `MYSQL_DATABASE` | Database name | `feature_flags` |
| `REDIS_PORT` | Redis port | `6379` |
| `OMDB_API_KEY` | OMDb API key | Required |

### Application Profiles

- **dev** - Development configuration
- **docker** - Docker environment

## 🐛 Troubleshooting

### Common Issues

#### 1. Redis Pub/Sub Not Working

```bash
# Check Redis connection
docker exec -it redis redis-cli ping

# Monitor Redis pub/sub messages
docker exec -it redis redis-cli monitor

# Check movie-search-backend logs for Redis messages
docker-compose logs movie-search-backend | grep "PUB/SUB"
```

#### 2. Feature Flag Updates Not Propagating

```bash
# Check if Redis pub/sub is working
curl -X PUT -H "Content-Type: application/json" \
  -d '{"enabled": true}' \
  http://localhost:8080/feature/flags/dark_mode

# Wait 5 seconds, then check if movie-search-backend received it
curl http://localhost:8081/movie/movies/flags/dark_mode
```

#### 3. Movie Search API Issues

```bash
# Check if OMDb API is accessible
curl "http://www.omdbapi.com/?apikey=64171ee0&s=batman"

# Check movie-search-backend logs for errors
docker-compose logs movie-search-backend --tail=50
```

#### 4. Frontend Dark Mode Not Updating

```bash
# Check if feature flag is properly set
curl http://localhost:8081/movie/movies/flags/dark_mode

# Frontend polls every 30 seconds, wait for next poll cycle
# Or check browser console for polling logs
```

#### 5. Database Connection Issues

```bash
# Check MySQL status
docker-compose logs mysql

# Reset database
docker-compose down -v
docker-compose up -d mysql
```

#### 6. Port Conflicts

```bash
# Check port usage
netstat -tulpn | grep :8080
netstat -tulpn | grep :3000

# Kill conflicting processes
sudo kill -9 <PID>
```

### Health Checks

```bash
# Feature Flag Backend
curl http://localhost:8080/feature/actuator/health

# Movie Search Backend
curl http://localhost:8081/movie/actuator/health

# Database
docker exec -it mysql mysql -u root -p -e "SELECT 1"

# Redis
docker exec -it redis redis-cli ping

# Test Redis Pub/Sub
docker exec -it redis redis redis-cli publish "feature-flag-updates" '{"test": "message"}'
```



## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.



