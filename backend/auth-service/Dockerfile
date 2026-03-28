FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*[^plain].jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
