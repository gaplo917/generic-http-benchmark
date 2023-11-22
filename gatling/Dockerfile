FROM gradle:7.5.1-jdk17

WORKDIR /usr/src/app

ENV GRADLE_USER_HOME=".gradle"

COPY . .

CMD ["gradle", "-i", "gatlingRun"]
