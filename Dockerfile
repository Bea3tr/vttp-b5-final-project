# Build Angular
FROM node:23 AS ng-build

WORKDIR /src

RUN npm i -g @angular/cli

COPY client/public public
COPY client/src src
COPY client/*.json .

RUN npm ci && ng build

# Build Spring Boot
FROM openjdk:23-jdk AS j-build

WORKDIR /src

COPY server/.mvn .mvn
COPY server/src src
COPY server/mvnw .
COPY server/pom.xml .

# Copy angular files over to static
COPY --from=ng-build /src/dist/client/browser src/main/resources/static

RUN chmod a+x mvnw && ./mvnw package -Dmaven.test.skip=true

# Copy the JAR file over to the final container
FROM openjdk:23-jdk

WORKDIR /app

COPY --from=j-build /src/target/server-0.0.1-SNAPSHOT.jar app.jar

ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.20.0/jmx_prometheus_javaagent-0.20.0.jar /app/jmx_exporter.jar
COPY server/prometheus-config.yaml /app/prometheus-config.yaml

ENV PORT=8080
ENV SPRING_DATA_REDIS_HOST=localhost SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_PASSWORD="" SPRING_DATA_REDIS_USERNAME=""
ENV SPRING_DATA_REDIS_DATABASE=0

ENV SPRING_DATA_MONGODB_URI=mongodb://localhost:27017 SPRING_DATA_MONGODB_DATABASE=myapp
ENV SPRING_DATASOURCE_URL='' SPRING_DATASOURCE_USERNAME=''
ENV SPRING_DATASOURCE_PASSWORD=''

ENV PETFINDER_ID="" PETFINDER_SECRET=""
ENV SPRING_MAIL_HOST=smtp.gmail.com SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME="" SPRING_MAIL_PASSWORD=""

ENV DOMAIN_NAME=""

EXPOSE ${PORT} 9404

SHELL ["/bin/sh", "-c"]
ENTRYPOINT SERVER_PORT=${PORT} java -javaagent:/app/jmx_exporter.jar=9404:/app/prometheus-config.yaml -jar app.jar