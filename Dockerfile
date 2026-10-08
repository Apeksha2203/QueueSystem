# VIVA GUIDE: Two-stage cloud build: Maven compiles the Java 17 WAR; Tomcat runs it as ROOT.war on HTTP 8080. Database configuration is supplied at runtime.
# Build stage contains compiler/tools; only the resulting WAR is copied into the runtime image.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
# Disable Tomcat's shutdown socket so Render HTTP probes do not mistake it for the web port.
RUN sed -i 's/<Server port="8005"/<Server port="-1"/' /usr/local/tomcat/conf/server.xml \
    && grep -q '<Server port="-1"' /usr/local/tomcat/conf/server.xml
COPY --from=build /build/target/queue-system.war /usr/local/tomcat/webapps/ROOT.war
ENV TZ=Asia/Kolkata
EXPOSE 8080
CMD ["catalina.sh", "run"]
