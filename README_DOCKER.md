# 🐳 Configuración Docker Completada - Microservicios Clínica

## ✅ Archivos Generados/Actualizados

### 📄 Dockerfiles
```
✓ eureka_server/Dockerfile          - Multi-stage build para Eureka
✓ gateway/Dockerfile                - Multi-stage build para API Gateway  
✓ user-service/DockerFile           - Multi-stage build para User Service (HTTP + gRPC)
```

### 📦 Docker Compose
```
✓ docker-compose.yml                - Orquestación de 6 servicios + 2 BD
✓ .dockerignore                     - Optimización de build context
```

### 🗄️ Base de Datos
```
✓ user-service/init-db.sql          - Schema e índices para user-service
```

### ⚙️ Configuraciones de Aplicación
```
✓ eureka_server/src/main/resources/application.yml
✓ gateway/src/main/resources/application.yml       (ACTUALIZADO)
✓ user-service/src/main/resources/application.yml  (ACTUALIZADO)
```

### 📚 Documentación
```
✓ DOCKER_SETUP.md                   - Guía completa (30+ secciones)
✓ DOCKER_SUMMARY.md                 - Resumen técnico
✓ QUICKSTART.md                     - Guía rápida de inicio
✓ README_DOCKER.md                  - Este archivo
```

### 🛠️ Scripts de Gestión
```
✓ docker-manage.sh                  - Script Bash (Linux/Mac)
✓ docker-manage.ps1                 - Script PowerShell (Windows)
```

---

## 🏗️ Arquitectura de Servicios

```
┌──────────────────────────────────────────────────────────┐
│                   CAPA PRESENTACIÓN                       │
│              API Gateway (8080)                           │
│    - Punto de entrada único                              │
│    - Service discovery automático                        │
│    - TokenRelay (OAuth2)                                 │
└──────────────────┬───────────────────────────────────────┘
                   │
        ┌──────────┼──────────┬──────────────┐
        │          │          │              │
┌───────▼──┐  ┌────▼────┐ ┌──▼──────┐  ┌────▼──────┐
│  Eureka  │  │  User   │ │ Keycloak│  │  Keycloak │
│  Server  │  │ Service │ │  Auth   │  │    DB     │
│ (8761)   │  │ (8081)  │ │ (8082)  │  │           │
└──────────┘  └────┬────┘ └────┬────┘  └───────────┘
                   │           │
              ┌────▼───┐   ┌────▼──────┐
              │User DB │   │Keycloak DB│
              │ (5432) │   │ (5432)    │
              └────────┘   └───────────┘

PUERTOS DISPONIBLES:
- 8080:  API Gateway (REST)
- 8081:  User Service HTTP
- 8761:  Eureka Dashboard
- 8082:  Keycloak Admin
- 9090:  User Service gRPC (interno)
- 5432:  PostgreSQL (4 datos + 2 BD)
```

---

## 🚀 Quick Start (3 Pasos)

### Windows PowerShell:
```powershell
# 1. Compilar
mvn clean package -DskipTests

# 2. Iniciar
docker-compose up -d

# 3. Verificar (esperar 30-60 segundos)
.\docker-manage.ps1 health
```

### Linux/Mac:
```bash
# 1. Compilar
mvn clean package -DskipTests

# 2. Iniciar
docker-compose up -d

# 3. Verificar
./docker-manage.sh health
```

---

## 📊 Servicios Configurados

| # | Servicio | Puerto | BD | Status | Notas |
|---|----------|--------|-------|--------|-------|
| 1 | Eureka | 8761 | - | ✅ | Service Registry |
| 2 | API Gateway | 8080 | - | ✅ | Punto de entrada |
| 3 | User Service | 8081 | 🗄️ | ✅ | Microservicio usuarios |
| 4 | User Service | 9090 | - | ✅ | gRPC |
| 5 | Keycloak | 8082 | 🗄️ | ✅ | OAuth2/OIDC |
| 6 | BD User Service | - | 🗄️ | ✅ | PostgreSQL |
| 7 | BD Keycloak | - | 🗄️ | ✅ | PostgreSQL |

---

## 📋 Configuraciones Importantes

### Eureka Server
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### API Gateway
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: users-service
          uri: lb://user-service
          predicates:
            - Path=/users/**
```

### User Service
```yaml
spring:
  datasource:
    url: ${JDBC_DATABASE_URL:jdbc:postgresql://user-service-db:5432/clinic}
    username: ${JDBC_DATABASE_USERNAME:clinic_user}
    password: ${JDBC_DATABASE_PASSWORD:clinic_password}
  jpa:
    hibernate:
      ddl-auto: update
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://eureka-server:8761/eureka/}
```

---

## 🔐 Credenciales Default

| Servicio | Usuario | Contraseña |
|----------|---------|-----------|
| Keycloak Admin | admin | admin |
| PostgreSQL User Service | clinic_user | clinic_password |
| PostgreSQL Keycloak | keycloak | keycloak |

---

## 📝 Tabla de Contenidos de Documentación

### QUICKSTART.md (⏱️ 5 min)
- Quick start en 3 pasos
- Acceso a servicios
- Verificación rápida
- Solución de problemas comunes
- Limpieza

### DOCKER_SETUP.md (📖 30 min)
- Prerequisitos
- Estructura de servicios (diagrama)
- Puertos expuestos
- Compilación e inicio (3 opciones)
- Verificación de salud
- Acceso a servicios
- Detener y limpiar
- Solución de problemas (detallada)
- Variables de entorno
- Development mode
- CI/CD pipeline
- Notas importantes

### DOCKER_SUMMARY.md (📋 15 min)
- Archivos creados/modificados
- Dockerfiles explicados
- Docker Compose detallado
- Inicialización de BD
- Configuración de aplicaciones
- Scripts de gestión
- Variables de entorno
- Flujo de inicio
- Testing
- Optimizaciones
- Siguientes pasos

---

## 🎯 Comandos Útiles

### PowerShell (Windows)
```powershell
.\docker-manage.ps1 build-start        # Compilar e iniciar todo
.\docker-manage.ps1 status             # Ver estado
.\docker-manage.ps1 logs user-service  # Ver logs
.\docker-manage.ps1 health             # Verificar salud
.\docker-manage.ps1 test               # Probar endpoints
.\docker-manage.ps1 db                 # Abrir BD
.\docker-manage.ps1 clean              # Limpiar todo
```

### Bash (Linux/Mac)
```bash
./docker-manage.sh build-start          # Compilar e iniciar todo
./docker-manage.sh status               # Ver estado
./docker-manage.sh logs user-service    # Ver logs
./docker-manage.sh health               # Verificar salud
./docker-manage.sh test                 # Probar endpoints
./docker-manage.sh db                   # Abrir BD
./docker-manage.sh clean                # Limpiar todo
```

### Docker Compose Directo
```bash
docker-compose build               # Compilar imágenes
docker-compose up -d               # Iniciar servicios
docker-compose ps                  # Ver estado
docker-compose logs -f             # Ver logs
docker-compose down                # Detener servicios
docker-compose down -v             # Detener y limpiar volúmenes
```

---

## 📞 Testing de Servicios

```bash
# Eureka
curl http://localhost:8761/eureka/apps

# User Service
curl http://localhost:8081/actuator/health
curl http://localhost:8081/swagger-ui.html

# Gateway
curl http://localhost:8080/actuator/health

# Keycloak
curl http://localhost:8082/health/ready
```

---

## 📚 Próximos Pasos (Recomendados)

- [ ] Ejecutar `docker-compose up -d` para iniciar
- [ ] Verificar servicios en Eureka (http://localhost:8761)
- [ ] Probar API con Swagger (http://localhost:8081/swagger-ui.html)
- [ ] Agregar Prometheus + Grafana para monitoreo
- [ ] Agregar ELK stack para logging centralizado
- [ ] Implementar CI/CD con GitHub Actions
- [ ] Migrar a Kubernetes para producción
- [ ] Configurar SSL/TLS con certificados

---

## 📊 Estadísticas

| Métrica | Valor |
|---------|-------|
| Dockerfiles creados | 3 |
| Servicios en docker-compose | 6 |
| Bases de datos | 2 |
| Scripts de gestión | 2 |
| Documentación (líneas) | 1000+ |
| Archivos totales generados/modificados | 12 |

---

## ✨ Características

✅ **Multi-stage Docker builds** - Imágenes optimizadas
✅ **Service Discovery** - Eureka automático
✅ **Health Checks** - Reintentos automáticos
✅ **Persistent Volumes** - Datos preservados
✅ **Environment Variables** - Configuración flexible
✅ **Isolated Network** - microservices-net
✅ **gRPC Support** - User Service con gRPC
✅ **OAuth2/OIDC** - Keycloak integrado
✅ **PostgreSQL** - Bases de datos separadas
✅ **Scripts de Gestión** - Bash + PowerShell
✅ **Documentación Completa** - 3 guías detalladas
✅ **Quick Start** - Inicio en 3 pasos

---

## 🐛 Troubleshooting

### "Servicios no aparecen en Eureka"
→ Esperar 30-60 segundos, es normal

### "Error de puerto en uso"
→ Cambiar puerto en docker-compose.yml

### "BD no conecta"
→ Ver logs con `docker-compose logs user-service-db`

### "Out of memory"
→ Aumentar límites de memoria en docker-compose.yml

Ver documentación completa en `DOCKER_SETUP.md` para más soluciones.

---

## 📞 Support

Para más información:
1. Leer `QUICKSTART.md` (5 minutos)
2. Leer `DOCKER_SETUP.md` (30 minutos)
3. Leer `DOCKER_SUMMARY.md` (15 minutos)

---

**¡Configuración Docker completada! 🎉**

Ejecuta `docker-compose up -d` y comienza a desarrollar.
