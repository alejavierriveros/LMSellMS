# Stage 1: Builder, compilación
# Imagen base con el kit jdk 21 para compilar el código
FROM eclipse-temurin:21-jdk AS builder

# Establece directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia primero el pom al directorio actual (/app) para aprovechar la caché de capas.
# De esta forma, si el pom no cambia, Docker reutiliza las dependencias ya descargadas.
COPY pom.xml .

# Copia el Maven Wrapper (mvnw). Es un script que descarga y ejecuta Maven sin instalarlo en el sistema.
COPY mvnw .

# Configuraciones de maven wrapper para que mvnw sepa qué versión de maven descargar
COPY .mvn .mvn

# Da permiso de ejecución al script mvnw y pre-descarga las dependencias en segundo plano (-q para modo silencioso)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia el código fuente (se pone al final porque es lo que cambia más seguido)
COPY src ./src

# Compila y empaqueta la aplicación a un archivo JAR saltando los Tests
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
# Imagen base limpia solo con el JRE para ejecutar la aplicación de forma liviana
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia desde el stage anterior ("builder") el JAR generado en la carpeta target
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto 0 ya que estamos trabajando con Eureka y asignará puertos dinámicos aleatorios
EXPOSE 0

# Comando de arranque del microservicio de Ventas
ENTRYPOINT ["java", "-jar", "app.jar"]