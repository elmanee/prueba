# Etapa 1: Construcción (Build)
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . /app
# Ejecutamos el build quitando los tests para acelerar el proceso
RUN ./gradlew clean build -x test

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copiamos el archivo JAR compilado desde la etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar
# Exponemos el puerto 8080
EXPOSE 8080
# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
