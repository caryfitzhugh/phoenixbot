source scripts/creds
aws s3 sync target s3://phoenixbot.cnds.io/jars --exclude "*" --include "phoenixbot*standalone.jar"
