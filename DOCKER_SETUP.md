# Microservices Docker Setup

Esta guía describe cómo compilar, construir y ejecutar todos los microservicios usando Docker y Docker Compose.

## Prerequisitos

- Docker (v20.10+)
- Docker Compose (v2.0+)
- 4GB+ RAM disponible para los contenedores

## Estructura de Servicios

```
┌─────────────────────────────────────────────────────┐
│                   API Gateway (8080)                 │
│             (lb://eureka + service discovery)        │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┬──────────────┐
        │                     │              │
   ┌────▼────────┐  ┌────────▼─────┐  ┌────▼─────────┐
   │   Eureka     │  │  User Service │  │  Keycloak   │
   │  (8761)      │  │  (8081/9090)  │  │  (8082)     │
   └─────────────┘  └────────┬──────┘  └────┬────────┘
                             │              │
                       ┌─────▼────┐  ┌────▼──────┐
                       │user-db   │  │keycloak-db│
                       │(5432)    │  │ (5432)    │
                       └──────────┘  └───────────┘
```

## Puertos Expuestos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| API Gateway | 8080 | HTTP - Punto de entrada principal |
| Eureka Server | 8761 | HTTP - Service Registry |
| User Service | 8081 | HTTP - API REST |
| User Service | 9090 | gRPC - Communication protocol |
| Keycloak | 8082 | HTTP - OAuth2/OIDC Server |
| User Service DB | 5432 | PostgreSQL |
| Keycloak DB | 5432 (internal) | PostgreSQL (solo red interna) |

## Compilación e Inicio

### Opción 1: Compilación local + Docker Compose

```bash
# 1. Compilar todos los módulos Maven
cd /path/to/microservices/final
mvn clean package -DskipTests

# 2. Construir las imágenes Docker
docker-compose build

# 3. Iniciar todos los contenedores
docker-compose up -d

# 4. Ver logs
docker-compose logs -f
```

### Opción 2: Docker Compose Build (incluye compilación)

```bash
cd /path/to/microservices/final

# Compilar y construir en un paso
docker-compose up -d --build

# Ver logs
docker-compose logs -f
```

### Opción 3: Compilar servicio específico

```bash
# Compilar solo user-service
docker-compose build user-service

# Iniciar solo user-service y dependencias
docker-compose up -d user-service

# Ver logs
docker-compose logs -f user-service
```

## Verificación de Salud

```bash
# Verificar que todos los contenedores estén corriendo
docker-compose ps

# Probar Eureka Server
curl http://localhost:8761/eureka/apps

# Probar User Service
curl http://localhost:8081/actuator/health

# Probar API Gateway
curl http://localhost:8080/actuator/health

# Probar Keycloak
curl http://localhost:8082/health/ready
```

## Acceso a Servicios

### Eureka Dashboard
```
http://localhost:8761/
```

### User Service Swagger UI
```
http://localhost:8081/swagger-ui.html
```

### Keycloak Admin Console
```
http://localhost:8082/admin/
- Usuario: admin
- Contraseña: admin
```

### Conectarse a la Base de Datos

```bash
# User Service Database
psql -h localhost -p 5432 -U clinic_user -d clinic

# Keycloak Database (solo desde container)
docker exec -it keycloak-db psql -U keycloak -d keycloak
```

## Detener y Limpiar

```bash
# Detener todos los contenedores
docker-compose down

# Detener y eliminar volúmenes (cuidado: pierde datos)
docker-compose down -v

# Eliminar imágenes construidas
docker-compose down --rmi all
```

## Solución de Problemas

### Los servicios no se comunican

1. Verificar que la red `microservices-net` existe:
   ```bash
   docker network ls | grep microservices-net
   ```

2. Verificar que los contenedores estén en la red:
   ```bash
   docker network inspect microservices-net
   ```

### User Service no puede conectarse a BD

1. Verificar que la BD está sana:
   ```bash
   docker-compose ps user-service-db
   ```

2. Ver logs de la BD:
   ```bash
   docker-compose logs user-service-db
   ```

3. Probar conexión manual:
   ```bash
   docker exec -it user-service psql -h user-service-db -U clinic_user -d clinic
   ```

### Eureka no muestra los servicios

1. Esperar 30-60 segundos (tiempo de registro)
2. Ver logs de Eureka:
   ```bash
   docker-compose logs eureka-server
   ```

3. Verificar logs del servicio:
   ```bash
   docker-compose logs user-service
   ```

### Problemas de memoria/rendimiento

1. Aumentar límites de memoria en docker-compose.yml:
   ```yaml
   services:
     user-service:
       deploy:
         resources:
           limits:
             memory: 1G
           reservations:
             memory: 512M
   ```

2. Habilitar garbage collection más agresivo:
   ```bash
   JAVA_OPTS: "-Xmx512m -Xms256m -XX:+UseG1GC"
   ```

## Variables de Entorno Importantes

| Variable | Servicio | Descripción |
|----------|----------|-------------|
| EUREKA_URL | gateway, user-service | URL del servidor Eureka |
| JDBC_DATABASE_URL | user-service | URL de PostgreSQL |
| JDBC_DATABASE_USERNAME | user-service | Usuario PostgreSQL |
| JDBC_DATABASE_PASSWORD | user-service | Contraseña PostgreSQL |
| KEYCLOAK_ISSUER_URI | gateway, user-service | URL del issuer de Keycloak |
| GRPC_SERVER_PORT | user-service | Puerto del servidor gRPC |
| JAVA_OPTS | todos | Opciones de la JVM |

## Development Mode

Para desarrollo local sin Docker:

```bash
# Terminal 1 - PostgreSQL (usando Docker)
docker run -d \
  --name user-service-db \
  -e POSTGRES_DB=clinic \
  -e POSTGRES_USER=clinic_user \
  -e POSTGRES_PASSWORD=clinic_password \
  -p 5432:5432 \
  postgres:15-alpine

# Terminal 2 - User Service
cd user-service
mvn spring-boot:run

# Terminal 3 - Gateway
cd gateway
mvn spring-boot:run

# Terminal 4 - Eureka Server
cd eureka_server
mvn spring-boot:run

# Terminal 5 - Keycloak (usando docker-compose)
docker-compose up keycloak keycloak-db
```

## CI/CD Pipeline Recomendado

```yaml
# .github/workflows/build-and-deploy.yml
stages:
  - build: mvn clean package -DskipTests
  - docker: docker-compose build
  - test: docker-compose up -d && sleep 30 && ./run-tests.sh
  - push: docker push <registry>/microservices:latest
  - deploy: kubectl apply -f k8s/
```

## Notas Importantes

- **Base de Datos Persistente**: Los volúmenes `user_service_db_data` y `keycloak_db_data` persisten los datos entre reinicios
- **Network Aislada**: Todos los servicios están en la red `microservices-net` para comunicación segura
- **Health Checks**: Cada servicio tiene health checks configurados para reintentos automáticos
- **Logs**: Usar `docker-compose logs <service>` para ver logs en tiempo real
- **Desarrollo**: Los Dockerfiles usan multi-stage builds para reducir tamaño de imagen final
