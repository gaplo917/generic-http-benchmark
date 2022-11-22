#!/bin/bash
RESULT_PATH="logs";
UPLOAD_PATH="upload";
DATE_TIMESTAMP=$(date +%Y%m%d%H%M%S)
DIR="$RESULT_PATH/$DATE_TIMESTAMP";
UPLOAD_DIR="$UPLOAD_PATH/$DATE_TIMESTAMP";
mkdir -p $DIR;
mkdir -p $UPLOAD_DIR;

for env in "./config/ktor.env" ; do
  ENV_FILE=$env;
  FILE_NAME="$(basename -s .env $env)";
  RESULT="$DIR/$FILE_NAME.txt";

  touch $RESULT;

  # Add date
  echo "Benchmark $ENV_FILE at $(date -u)" | tee -a $RESULT;

  docker compose --env-file $ENV_FILE build && \
  docker compose --env-file $ENV_FILE up -d benchmark-target && \
  docker compose --env-file $ENV_FILE up gatling-runner | tee -a $RESULT && \
  docker compose --env-file $ENV_FILE down && \
  LINE=$(cat $RESULT | grep "Please open the following file: file:///usr/src/app/build/reports/gatling/") && \
  FIRST_PASS="${LINE/gatling-runner  | Please open the following file: file:\/\/\/usr\/src\/app\/build\/reports\/gatling\//}" && \
  FOLDER="${FIRST_PASS/\/index.html/}" && \
  rsync -av --exclude='*.log' "./reports/gatling/$FOLDER/" "./$UPLOAD_DIR/$FILE_NAME";

done
