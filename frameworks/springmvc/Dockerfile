FROM amazoncorretto:21.0.1 AS wrapper

WORKDIR /usr/src/app

COPY gradle gradle

COPY gradlew gradlew

COPY gradle.properties gradle.properties

COPY build.gradle.kts build.gradle.kts

COPY settings.gradle.kts settings.gradle.kts

# a hack to cache dependencies
RUN ./gradlew -i assemble || true

COPY src src

RUN ./gradlew -i bootJar

FROM amazoncorretto:21.0.1

WORKDIR /usr/src/app

COPY --from=wrapper /usr/src/app/build/libs/springmvc-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
