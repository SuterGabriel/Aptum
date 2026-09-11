# Der Scheduling-Dienst: Java 21, Spring Boot, drei Maven-Module.
#
# Zwei Stufen. Die erste baut mit Maven und hat den ganzen Werkzeugkasten;
# die zweite hat nur eine JRE und das Jar. Was im laufenden Container nicht
# gebraucht wird, ist dort auch nicht drin - kein Maven, keine Quellen, kein
# Compiler. Das ist kleiner, und es ist weniger, was angegriffen werden kann.

# ---- Stufe 1: bauen ------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS bau
WORKDIR /bau

# Erst die POMs, dann die Abhängigkeiten: Diese Schicht ändert sich selten
# und bleibt im Cache, auch wenn sich der Code ändert.
COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
RUN mvn -B -q dependency:go-offline

COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
# Die Tests laufen in der CI gegen ein echtes Postgres; hier wird nur gebaut.
RUN mvn -B -q package -DskipTests

# ---- Stufe 2: laufen -----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Kein Root im Container. Die Anwendung braucht keine Rechte am System,
# also bekommt sie keine.
RUN addgroup -S aptum && adduser -S -G aptum aptum
USER aptum

COPY --from=bau /bau/infrastructure/target/scheduling-infrastructure-*.jar app.jar

EXPOSE 8080

# Der Orchestrator fragt hier nach, ob der Dienst lebt - dieselbe Antwort,
# die Spring Actuator ohnehin gibt.
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
