#!/bin/bash
RESULT_PATH="logs";
DIR="$RESULT_PATH/build/$(date +%Y%m%d%H%M%S)";

mkdir -p $DIR;

for env in ./config/* ; do
  ENV_FILE=$env;
  FILE_NAME="$(basename -s .env $env)";
  RESULT="$DIR/$FILE_NAME.txt";

  touch $RESULT;

  # Add date
  echo "Build $ENV_FILE at $(date -u)" | tee -a $RESULT;

  # TODO: optimize to only build distinct docker image
  (docker compose --env-file $ENV_FILE build >> $RESULT)  &
done

echo "Waiting all build ready..."
while [[ $(jobs -pr) ]]; do
  sleep 1;
done

echo "Done."
