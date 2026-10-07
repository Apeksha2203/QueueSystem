FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/queue-system.war /usr/local/tomcat/webapps/ROOT.war
ENV TZ=Asia/Kolkata
EXPOSE 8080
CMD ["catalina.sh", "run"]
