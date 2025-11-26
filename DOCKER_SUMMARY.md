# Resumen de Configuración Docker para Microservicios

## Archivos Creados/Modificados

### 1. Dockerfiles

#### `eureka_server/Dockerfile`
- **Multi-stage build**: Compilación Maven en una imagen builder, ejecución en imagen ligera
- **Puerto**: 8761
- **Health check**: Verifica disponibilidad de /eureka/apps
- **Tamaño optimizado**: Usa eclipse-temurin:21-jre-jammy (ligera)

#### `gateway/Dockerfile`
- **Multi-stage build**: Igual que Eureka
- **Puerto**: 8080
- **Health check**: Verifica /actuator/health
- **Dependencias**: Compila con soporte proto para protobuf

#### `user-service/DockerFile`
- **Multi-stage build**: Compilación Maven + ejecución
- **Puertos**: 8081 (HTTP) y 9090 (gRPC)
- **Health check**: Verifica /actuator/health
- **Compilación proto**: Incluye compilación de archivos .proto

### 2. Docker Compose (`docker-compose.yml`)

**Servicios Configurados:**

| Servicio | Contenedor | Puerto | BD | Función |
|----------|-----------|--------|----|---------| 
| eureka-server | eureka-server | 8761 | - | Service Registry |
| api-gateway | api-gateway | 8080 | - | API Gateway con service discovery |
| user-service | user-service | 8081/9090 | ✓ | Microservicio de usuarios (HTTP + gRPC) |
| user-service-db | user-service-db | 5432 (int) | PostgreSQL | BD para user-service |
| keycloak | keycloak | 8082 | ✓ | OAuth2/OIDC Server |
| keycloak-db | keycloak-db | (int) | PostgreSQL | BD para Keycloak |

**Red**: `microservices-net` (bridge) - Comunicación segura entre contenedores

**Volúmenes Persistentes:**
- `user_service_db_data` → `/var/lib/postgresql/data` (user-service DB)
- `keycloak_db_data` → `/var/lib/postgresql/data` (Keycloak DB)

**Health Checks**: Todos los servicios tienen health checks configurados con reintentos automáticos

**Dependencias**: 
- Gateway depende de Eureka y Keycloak
- User Service depende de su BD y Eureka

### 3. Inicialización de Base de Datos

#### `user-service/init-db.sql`
- Crea esquema `clinic`
- Tabla `users` con campos: id, nombre, apellido, correo, telefono, role
- Índices para optimización: correo y role
- Timestamps: created_at, updated_at

### 4. Configuración de Aplicaciones

#### `eureka_server/src/main/resources/application.yml`
- Sin cambios requeridos (usa valores por defecto)

#### `gateway/src/main/resources/application.yml`
- **Actualizado**: Configuración de descubrimiento de servicios
- **Rutas**: /users/** → user-service
- **Eureka URL**: Variable de entorno `${EUREKA_URL}`
- **Keycloak**: Variable de entorno `${KEYCLOAK_ISSUER_URI}`
- **Service ID**: Convertido a minúsculas para routing

#### `user-service/src/main/resources/application.yml`
- **BD**: Usa variables de entorno con defaults
  - `JDBC_DATABASE_URL`: jdbc:postgresql://user-service-db:5432/clinic
  - `JDBC_DATABASE_USERNAME`: clinic_user
  - `JDBC_DATABASE_PASSWORD`: clinic_password
- **Eureka**: Configurado con hostname y instance-id dinámico
- **gRPC**: Puerto 9090 (configurable)
- **Keycloak**: URL del issuer configurable
- **JPA**: Hibernate DDL-auto = update, SQL formatting = true

### 5. Scripts de Gestión

#### `docker-manage.sh` (Linux/Mac)
Comandos disponibles:
- `./docker-manage.sh build` - Compilar imágenes
- `./docker-manage.sh build-start` - Compilar e iniciar
- `./docker-manage.sh start` - Iniciar servicios
- `./docker-manage.sh stop` - Detener servicios
- `./docker-manage.sh restart` - Reiniciar servicios
- `./docker-manage.sh status` - Ver estado
- `./docker-manage.sh logs [service]` - Ver logs
- `./docker-manage.sh health` - Verificar salud
- `./docker-manage.sh test` - Probar endpoints
- `./docker-manage.sh db` - Abrir shell de BD
- `./docker-manage.sh clean` - Limpiar todo

#### `docker-manage.ps1` (Windows PowerShell)
Mismos comandos que el script bash, adaptados para PowerShell

### 6. Documentación

#### `DOCKER_SETUP.md`
- Guía completa de instalación
- Diagrama de arquitectura
- Puertos y servicios
- Instrucciones paso a paso
- Verificación de salud
- Solución de problemas
- Variables de entorno
- Modo desarrollo
- Pipeline CI/CD recomendado

#### `.dockerignore`
- Excluye archivos innecesarios del build
- Reduce tamaño de contexto de build

## Variables de Entorno

### Eureka Server
- No requiere configuración especial

### API Gateway
```
EUREKA_URL=http://eureka-server:8761/eureka/
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/microservices
JAVA_OPTS=-Xmx512m -Xms256m
```

### User Service
```
EUREKA_URL=http://eureka-server:8761/eureka/
JDBC_DATABASE_URL=jdbc:postgresql://user-service-db:5432/clinic
JDBC_DATABASE_USERNAME=clinic_user
JDBC_DATABASE_PASSWORD=clinic_password
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/microservices
GRPC_SERVER_PORT=9090
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
JAVA_OPTS=-Xmx512m -Xms256m
```

## Flujo de Inicio

```
1. docker-compose build
   ├── Compila cada Dockerfile (multi-stage)
   └── Crea imágenes: eureka-server, gateway, user-service

2. docker-compose up -d
   ├── Inicia keycloak-db (PostgreSQL)
   ├── Inicia keycloak (espera a BD lista)
   ├── Inicia user-service-db (PostgreSQL + init-db.sql)
   ├── Inicia eureka-server
   ├── Inicia user-service (espera a BD lista)
   └── Inicia api-gateway (espera a Eureka)

3. Startup checks
   ├── Health checks de cada servicio
   ├── Registro de servicios en Eureka (30-60 seg)
   └── Disponibilidad de endpoints
```

## Testing

```bash
# Test 1: Eureka - Verificar servicios registrados
curl http://localhost:8761/eureka/apps

# Test 2: User Service - Health check
curl http://localhost:8081/actuator/health

# Test 3: Gateway - Health check
curl http://localhost:8080/actuator/health

# Test 4: Keycloak - Health check
curl http://localhost:8082/health/ready

# Test 5: User Service API
curl http://localhost:8081/swagger-ui.html

# Test 6: Base de datos
psql -h localhost -p 5432 -U clinic_user -d clinic
```

## Optimizaciones Aplicadas

1. **Multi-stage builds**: Reduce tamaño de imagen final
2. **Alpine images**: Bases de datos usan alpine para menor footprint
3. **Health checks**: Reintentos automáticos en caso de fallo
4. **Red dedicada**: microservices-net para comunicación segura
5. **Volúmenes persistentes**: Datos no se pierden en reinicios
6. **Environment variables**: Fácil configuración entre ambientes
7. **Lazy initialization**: Servicios esperan a dependencias

## Siguientes Pasos (Recomendados)

1. **Kubernetes**: Migrar de Docker Compose a K8s para producción
2. **Monitoring**: Agregar Prometheus + Grafana
3. **Logging**: ELK stack (Elasticsearch, Logstash, Kibana)
4. **API Docs**: OpenAPI/Swagger completamente configurado
5. **Tests**: Integration tests en CI/CD
6. **Security**: SSL/TLS, secret management, RBAC
7. **Backup**: Estrategia de backup de bases de datos
