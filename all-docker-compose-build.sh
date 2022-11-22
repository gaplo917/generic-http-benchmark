#!/bin/bash
RESULT_PATH="logs";
DIR="$RESULT_PATH/build/$(date +%Y%m%d%H%M%S)";

mkdir -p $DIR;

IMAGES=();

for env in ./config/*.env ; do
  ENV_FILE=$env;
  FILE_NAME="$(basename -s .env $env)";
  RESULT="$DIR/$FILE_NAME.txt";

  IMAGE=$(cat $ENV_FILE | grep "BENCHMARK_TARGET_DOCKER_IMAGE=");

  if [[ ! "${IMAGES[@]}" =~ "$IMAGE" ]]; then
      echo "Found a unique image: $IMAGE";
      IMAGES=(${IMAGES[@]} $IMAGE);

      touch $RESULT;

      # Add date
      echo "Build $ENV_FILE at $(date -u), check the log via $RESULT\n" | tee -a $RESULT;

      (docker compose --env-file $ENV_FILE build >> $RESULT)  &
  fi
done

echo "Waiting all build ready...";

while [[ $(jobs -pr) ]]; do
  sleep 1;
done

echo "Done.";
