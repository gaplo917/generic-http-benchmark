FROM amazoncorretto:21.0.1 AS wrapper

WORKDIR /usr/src/app

COPY gradle gradle

COPY gradlew gradlew

COPY gradle.properties gradle.properties

RUN ./gradlew

COPY . .

RUN ./gradlew -i bootJar

FROM amazoncorretto:21.0.1

WORKDIR /usr/src/app

COPY --from=wrapper /usr/src/app/build/libs/springwebflux-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
