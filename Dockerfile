FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home app
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN mkdir uploads && chown -R app:app /app
USER app
# Ajustado al plan gratuito de Render (512 MB de RAM y menos de 1 CPU)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
