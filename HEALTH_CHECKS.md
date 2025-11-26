# 🏥 Health Check Endpoints

## Endpoints de Salud de Servicios

### Eureka Server
```
GET http://localhost:8761/eureka/apps
Descripción: Lista todos los servicios registrados
Respuesta: XML con aplicaciones registradas

GET http://localhost:8761/eureka/apps/<app-name>
Descripción: Obtiene detalles de una aplicación específica
Ejemplo: http://localhost:8761/eureka/apps/user-service
```

### API Gateway
```
GET http://localhost:8080/actuator/health
Descripción: Spring Boot Actuator health check
Respuesta: {"status":"UP","components":{...}}

GET http://localhost:8080/actuator/health/liveness
Descripción: Liveness probe (¿está vivo?)

GET http://localhost:8080/actuator/health/readiness
Descripción: Readiness probe (¿listo para tráfico?)
```

### User Service
```
GET http://localhost:8081/actuator/health
Descripción: Spring Boot Actuator health check

GET http://localhost:8081/actuator/health/db
Descripción: Verificar conexión con base de datos

GET http://localhost:8081/swagger-ui.html
Descripción: Documentación interactiva de API

POST http://localhost:8081/users
Descripción: Crear usuario (API REST)
Body: {
  "nombre": "Juan",
  "apellido": "Pérez",
  "correo": "juan@example.com",
  "telefono": "123456789",
  "role": "PACIENTE"
}

GET http://localhost:8081/users
Descripción: Listar todos los usuarios

GET http://localhost:8081/users/{id}
Descripción: Obtener usuario por ID
```

### Keycloak
```
GET http://localhost:8082/health/ready
Descripción: Keycloak readiness probe

GET http://localhost:8082/admin/
Descripción: Consola de administración
Credenciales: admin / admin

GET http://localhost:8082/realms/<realm-name>
Descripción: Obtener configuración del realm
Ejemplo: http://localhost:8082/realms/microservices

GET http://localhost:8082/realms/<realm-name>/protocol/openid-connect/certs
Descripción: Certificados públicos para validar JWT
```

### PostgreSQL (User Service DB)
```
Host: localhost
Puerto: 5432
Base de datos: clinic
Usuario: clinic_user
Contraseña: clinic_password

Comandos de prueba:
psql -h localhost -p 5432 -U clinic_user -d clinic

\dt                    # Listar tablas
\d users               # Estructura de tabla users
SELECT * FROM users;   # Ver todos los usuarios
```

---

## 🧪 Scripts de Testing

### Bash/PowerShell - Verificación Completa

```bash
#!/bin/bash
echo "Testing Eureka..."
curl -s http://localhost:8761/eureka/apps | head -20

echo "Testing User Service..."
curl -s http://localhost:8081/actuator/health | jq '.'

echo "Testing Gateway..."
curl -s http://localhost:8080/actuator/health | jq '.'

echo "Testing Keycloak..."
curl -s http://localhost:8082/health/ready | jq '.'
```

### Crear Usuario de Prueba

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "correo": "juan@example.com",
    "telefono": "+34 912 345 678",
    "role": "PACIENTE"
  }'
```

### Listar Usuarios

```bash
curl -X GET http://localhost:8081/users \
  -H "Content-Type: application/json"
```

### Obtener Usuario por ID

```bash
curl -X GET http://localhost:8081/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json"
```

---

## 📊 Monitoreo en Tiempo Real

### Ver todos los logs
```bash
docker-compose logs -f
```

### Ver logs de un servicio específico
```bash
docker-compose logs -f user-service
docker-compose logs -f eureka-server
docker-compose logs -f gateway
```

### Ver últimas 50 líneas
```bash
docker-compose logs --tail=50
```

### Ver logs desde hace X tiempo
```bash
docker-compose logs --since=5m
```

### Ver estadísticas de recursos
```bash
docker stats
```

---

## ✅ Checklist de Verificación

Después de iniciar los servicios, verificar:

- [ ] Eureka Dashboard accesible: http://localhost:8761
- [ ] User Service Swagger accesible: http://localhost:8081/swagger-ui.html
- [ ] Gateway responde: http://localhost:8080/actuator/health
- [ ] Keycloak admin accesible: http://localhost:8082/admin
- [ ] Usuario en Eureka: http://localhost:8761/eureka/apps/user-service
- [ ] Base de datos conectada:
  ```bash
  psql -h localhost -p 5432 -U clinic_user -d clinic -c "SELECT COUNT(*) FROM users;"
  ```
- [ ] Logs sin errores:
  ```bash
  docker-compose logs | grep ERROR
  ```

---

## 🔍 Debugging

### Ver configuración de un servicio
```bash
curl -s http://localhost:8081/actuator/configprops | jq '.'
```

### Ver beans de Spring
```bash
curl -s http://localhost:8081/actuator/beans | jq '.'
```

### Ver properties activas
```bash
curl -s http://localhost:8081/actuator/env | jq '.'
```

### Ver registros en Eureka
```bash
curl -s http://localhost:8761/eureka/apps | jq '.'
```

### Conectarse a BD para debugging
```bash
docker exec -it user-service-db psql -U clinic_user -d clinic

# Dentro de psql:
\dt                           # Listar tablas
SELECT * FROM users LIMIT 5;  # Ver primeros 5 usuarios
\l                            # Listar bases de datos
```

---

## 🚨 Casos de Fallo Comunes

### "Connection refused"
**Problema**: Servicio no está corriendo
**Solución**:
```bash
docker-compose ps                    # Ver estado
docker-compose logs <servicio>       # Ver logs
docker-compose restart <servicio>    # Reiniciar
```

### "timeout"
**Problema**: Servicio tarda en iniciar
**Solución**: Esperar 30-60 segundos, health checks están configurados

### "Database connection error"
**Problema**: BD no está lista
**Solución**:
```bash
docker-compose logs user-service-db  # Ver logs
docker-compose ps user-service-db    # Verificar estado
# Esperar a que muestre "ready" en la salida
```

### "Service not registered in Eureka"
**Problema**: Tardanza en registro
**Solución**: Es normal, esperar 30-60 segundos

### "No active profile set"
**Problema**: Spring no carga configuración
**Solución**: Verificar variables de entorno en docker-compose.yml

---

## 📈 Métricas Disponibles

### Metrics Actuator
```
GET http://localhost:8081/actuator/metrics
Descripción: Lista todas las métricas disponibles

GET http://localhost:8081/actuator/metrics/process.uptime
Descripción: Tiempo de ejecución del proceso

GET http://localhost:8081/actuator/metrics/jvm.memory.used
Descripción: Memoria JVM usada

GET http://localhost:8081/actuator/metrics/http.server.requests
Descripción: Estadísticas de requests HTTP
```

---

## 🔐 Verificar Keycloak

### Obtener realm
```bash
curl -s http://localhost:8082/realms/microservices | jq '.'
```

### Obtener certificados públicos
```bash
curl -s http://localhost:8082/realms/microservices/protocol/openid-connect/certs | jq '.'
```

### Token introspection
```bash
curl -X POST http://localhost:8082/realms/microservices/protocol/openid-connect/token/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=<your-token>"
```

---

## 📝 Logs Esperados

### Eureka startup
```
o.s.c.n.e.server.EurekaServerInitializerConfiguration : Started EurekaServerInitializerConfiguration in 3.2 seconds
```

### User Service startup
```
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8081 (http)
c.c.user.UserServiceApplication         : Started UserServiceApplication in 5.1 seconds
```

### Gateway startup
```
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
```

### BD initialization
```
PostgreSQL is initialized and ready for client connections
```

---

## 🎯 Performance Baselines

Tiempos esperados después de `docker-compose up -d`:
- Keycloak DB: 5-10 segundos
- Keycloak: 15-20 segundos
- User Service DB: 3-5 segundos
- Eureka Server: 5-10 segundos
- User Service: 10-15 segundos
- API Gateway: 5-10 segundos

**Total esperado**: 40-60 segundos para que todos estén listos

---

## 📞 Obtener Help

Ver documentación completa en:
- `QUICKSTART.md` - Inicio rápido
- `DOCKER_SETUP.md` - Guía detallada
- `DOCKER_SUMMARY.md` - Resumen técnico
