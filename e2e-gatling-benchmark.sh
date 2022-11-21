#!/bin/bash
for env in ./config/* ; do
  ENV_FILE=$env
  FILE_NAME="$(basename -s .env $env)"
  RESULT="e2e-result/$FILE_NAME.txt"

  touch $RESULT;
  # Add date
  echo "Benchmark $ENV_FILE at $(date -u)" | tee -a $RESULT;

  docker compose --env-file $ENV_FILE up -d benchmark-target prometheus grafana;
  docker compose --env-file $ENV_FILE up gatling-runner | tee -a $RESULT;
  docker compose --env-file $ENV_FILE down;
done
