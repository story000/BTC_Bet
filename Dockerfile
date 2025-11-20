# Build stage: compile and package the custom ROOT.war
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY server /workspace/server
RUN mvn -f server/pom.xml clean package

# Runtime stage: Tomcat serving the packaged WAR
FROM tomcat:10.1.0-M5-jdk16-openjdk-slim-bullseye
COPY --from=builder /workspace/server/target/ROOT.war /usr/local/tomcat/webapps/
