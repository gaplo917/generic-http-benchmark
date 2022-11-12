#!/bin/bash
SUITE="sb"
HOST="http://localhost:8081"
THREAD='-t4'
CONCURRENCY='-c2000'
DURATION='-d30s'
TIMEOUT='30s'
COOL_DOWN_SECOND=5

for TEST in 'blocking' 'blocking-coroutine' 'blocking-virtual-thread' 'non-blocking' 'non-blocking-coroutine' 'blocking-vt-future' 'blocking-vt-coroutine'
do
  for IODelay in 0 50 100 500
  do
      ENDPOINT="$HOST/$TEST/$IODelay"
      RESULT="$SUITE-$TEST.txt"

      # Add date
      echo "Benchmark $ENDPOINT at $(date -u)" | tee -a $RESULT;

      wrk $THREAD $CONCURRENCY $DURATION --timeout $TIMEOUT --latency $ENDPOINT | tee -a $RESULT

      printf "\n" | tee -a $RESULT

      printf "Waiting to cool down...\n";

      sleep $COOL_DOWN_SECOND;
  done
done
