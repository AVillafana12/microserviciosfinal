FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
# copia dependencias primero para cachear el build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests dependency:go-offline

# copia el resto del código y construye
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jdk
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} /app/app.jar

# puerto que expones en docker-compose
EXPOSE 5000

# volumen para almacenar imágenes (mapeado por docker-compose)
VOLUME ["/data/images"]

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]