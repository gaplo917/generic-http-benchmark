echo "started upload...";

mc alias set cf $CF_ENDPOINT $CF_ACCESS_KEY_ID $CF_SECRET_ACCESS_KEY;

mc cp --recursive upload/ cf/$CF_BUCKET/;
