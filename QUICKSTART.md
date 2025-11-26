# Quick Start - Ejecutar Microservicios con Docker

## Opción 1: Windows (PowerShell) - Recomendado

```powershell
# 1. Navegar al directorio del proyecto
cd C:\Users\alexv\Documents\microservices\final

# 2. Compilar todos los módulos Maven
mvn clean package -DskipTests

# 3. Compilar imágenes Docker
docker-compose build

# 4. Iniciar todos los servicios
docker-compose up -d

# 5. Esperar 30-60 segundos para que se registren en Eureka
Start-Sleep -Seconds 60

# 6. Verificar estado
.\docker-manage.ps1 health

# 7. Ver logs en tiempo real
docker-compose logs -f

# 8. Detener cuando termines
docker-compose down
```

## Opción 2: Windows (PowerShell) - Usando Script

```powershell
# 1. Navegar al directorio
cd C:\Users\alexv\Documents\microservices\final

# 2. Compilar y compilar imágenes
mvn clean package -DskipTests
.\docker-manage.ps1 build

# 3. Compilar e iniciar todo (todo en uno)
.\docker-manage.ps1 build-start

# 4. Ver estado
.\docker-manage.ps1 health

# 5. Ver logs de un servicio específico
.\docker-manage.ps1 logs user-service

# 6. Probar endpoints
.\docker-manage.ps1 test

# 7. Limpiar todo
.\docker-manage.ps1 clean
```

## Opción 3: Linux/Mac - Usando Script

```bash
# 1. Hacer script ejecutable
chmod +x docker-manage.sh

# 2. Compilar y ejecutar (con Maven)
mvn clean package -DskipTests

# 3. Compilar e iniciar todo
./docker-manage.sh build-start

# 4. Ver estado
./docker-manage.sh health

# 5. Ver logs
./docker-manage.sh logs user-service

# 6. Limpiar
./docker-manage.sh clean
```

## Acceso a Servicios

Una vez iniciados, accede a:

| Servicio | URL |
|----------|-----|
| 🔷 Eureka Dashboard | http://localhost:8761 |
| 🟢 User Service Swagger | http://localhost:8081/swagger-ui.html |
| 🟡 API Gateway | http://localhost:8080 |
| 🔴 Keycloak Admin | http://localhost:8082/admin (admin/admin) |

## Verificación Rápida

```bash
# Comprobar que los contenedores están corriendo
docker-compose ps

# Comprobar Eureka tiene servicios registrados
curl http://localhost:8761/eureka/apps

# Comprobar que User Service está sano
curl http://localhost:8081/actuator/health

# Conectarse a la BD de user-service
psql -h localhost -p 5432 -U clinic_user -d clinic
# Contraseña: clinic_password
```

## Solución de Problemas Comunes

### "Services no aparecen en Eureka"
```bash
# Esperar 30-60 segundos después del inicio
# Luego verificar:
docker-compose logs eureka-server
docker-compose logs user-service
```

### "Error conectando a BD"
```bash
# Verificar que el contenedor está corriendo
docker-compose ps user-service-db

# Ver logs de la BD
docker-compose logs user-service-db

# Probar conexión manual
psql -h localhost -p 5432 -U clinic_user -d clinic
```

### "Puerto 8080 ya en uso"
```bash
# Cambiar puerto en docker-compose.yml
# Línea con: - "8080:8080"
# Cambiar a: - "8090:8080"

# Luego reiniciar
docker-compose up -d
```

### "Out of memory"
```bash
# Aumentar memoria en docker-compose.yml
# Agregar bajo cada servicio:
deploy:
  resources:
    limits:
      memory: 1G
```

## Detener y Limpiar

```bash
# Solo detener (datos persisten)
docker-compose down

# Detener y eliminar todo (incluyendo BD)
docker-compose down -v

# Eliminar imágenes también
docker-compose down -v --rmi all
```

## Desarrollo Local sin Docker

Si prefieres ejecutar localmente:

```bash
# Terminal 1 - PostgreSQL
docker run -d \
  --name user-service-db \
  -e POSTGRES_DB=clinic \
  -e POSTGRES_USER=clinic_user \
  -e POSTGRES_PASSWORD=clinic_password \
  -p 5432:5432 \
  postgres:15-alpine

# Terminal 2 - Eureka Server
cd eureka_server
mvn spring-boot:run

# Terminal 3 - User Service
cd user-service
mvn spring-boot:run

# Terminal 4 - Gateway
cd gateway
mvn spring-boot:run

# Terminal 5 - Keycloak (Docker)
docker run -p 8082:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0 \
  start-dev
```

## Monitoreo en Tiempo Real

```bash
# Ver todos los logs
docker-compose logs -f

# Ver logs de un servicio
docker-compose logs -f user-service

# Ver logs de múltiples servicios
docker-compose logs -f user-service eureka-server

# Ver últimas 50 líneas
docker-compose logs --tail=50 user-service

# Ver logs desde hace 5 minutos
docker-compose logs --since=5m
```

## Administración de Base de Datos

```bash
# Conectarse a user-service DB
docker exec -it user-service-db psql -U clinic_user -d clinic

# Ver tablas
\dt

# Ver usuarios
SELECT * FROM users;

# Hacer backup
docker exec user-service-db pg_dump -U clinic_user clinic > backup.sql

# Restaurar
docker exec -i user-service-db psql -U clinic_user clinic < backup.sql
```

## Variables de Entorno Personalizadas

Crear archivo `.env`:
```
EUREKA_URL=http://eureka-server:8761/eureka/
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/microservices
JDBC_DATABASE_URL=jdbc:postgresql://user-service-db:5432/clinic
JDBC_DATABASE_USERNAME=clinic_user
JDBC_DATABASE_PASSWORD=clinic_password
JAVA_OPTS=-Xmx1024m -Xms512m
```

Luego:
```bash
docker-compose --env-file .env up -d
```

## Estadísticas de Recursos

```bash
# Ver uso de recursos de cada contenedor
docker stats

# Ver tamaño de imágenes
docker images

# Limpiar recursos no usados
docker system prune -a

# Ver espacio en disco usado por Docker
docker system df
```

## Más Información

- 📖 Ver `DOCKER_SETUP.md` para documentación completa
- 📋 Ver `DOCKER_SUMMARY.md` para resumen técnico
- 🐳 Docker Compose docs: https://docs.docker.com/compose/
- 📦 Docker Hub: https://hub.docker.com/
