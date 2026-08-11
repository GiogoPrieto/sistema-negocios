FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -Dmaven.resources.skip=false -Dfile.encoding=UTF-8
EXPOSE 8080
CMD ["java", "-jar", "target/SistemaNegocios-0.0.1-SNAPSHOT.jar"]