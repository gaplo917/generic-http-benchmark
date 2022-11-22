#!/bin/bash
RESULT_PATH="logs";
DIR="$RESULT_PATH/$(date +%Y%m%d%H%M%S)";

mkdir -p $DIR;

for env in ./config/* ; do
  ENV_FILE=$env;
  FILE_NAME="$(basename -s .env $env)";
  RESULT="$DIR/$FILE_NAME.txt";

  touch $RESULT;

  # Add date
  echo "Benchmark $ENV_FILE at $(date -u)" | tee -a $RESULT;

  docker compose --env-file $ENV_FILE up -d benchmark-target && \
  docker compose --env-file $ENV_FILE build gatling-runner && \
  docker compose --env-file $ENV_FILE up gatling-runner | tee -a $RESULT && \
  docker compose --env-file $ENV_FILE down;
done
