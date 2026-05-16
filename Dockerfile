# UsingJava 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR file
COPY build/libs/*-SNAPSHOT.jar app.jar
# PORT
EXPOSE 8080

# Command to run
ENTRYPOINT ["java", "-jar", "app.jar"]