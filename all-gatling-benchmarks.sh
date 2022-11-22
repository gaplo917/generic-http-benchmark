#!/bin/bash
LOG_PATH="logs";
UPLOAD_PATH="upload";
DATE_TIMESTAMP=$(date +%Y%m%d%H%M%S)
LOG_CURRENT_DIR="$LOG_PATH/$DATE_TIMESTAMP";
UPLOAD_CURRENT_DIR="$UPLOAD_PATH/$DATE_TIMESTAMP";

# Meta data for post-processing
META=($DATE_TIMESTAMP);

mkdir -p $LOG_CURRENT_DIR;
mkdir -p $UPLOAD_CURRENT_DIR;

# Run all benchmarks
for env in ./config/*.env ; do
  ENV_FILE=$env;
  FILE_NAME="$(basename -s .env $env)";
  RESULT="$LOG_CURRENT_DIR/$FILE_NAME.txt";

  # Add date
  echo "Benchmark $ENV_FILE at $(date -u)" | tee -a $RESULT;

  docker compose --env-file $ENV_FILE build && \
  docker compose --env-file $ENV_FILE up -d benchmark-target && \
  docker compose --env-file $ENV_FILE up gatling-runner | tee -a $RESULT && \
  docker compose --env-file $ENV_FILE down && \
  LINE=$(cat $RESULT | grep "Please open the following file: file:///usr/src/app/build/reports/gatling/") && \
  FIRST_PASS="${LINE/gatling-runner  | Please open the following file: file:\/\/\/usr\/src\/app\/build\/reports\/gatling\//}" && \
  FOLDER="${FIRST_PASS/\/index.html/}" && \
  rsync -av --exclude='*.log' "./reports/gatling/$FOLDER/" "./$UPLOAD_CURRENT_DIR/$FILE_NAME";

  META+=",$FILE_NAME";
done

# create a latest index file for post-processing
echo "$META" > "$UPLOAD_PATH/latest.txt";

# Upload the result in side the UPLOAD_PATH
docker compose -f docker-compose-cf-upload.yaml up;

