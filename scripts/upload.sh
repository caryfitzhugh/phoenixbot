source scripts/creds
aws s3 sync target s3://phoenixbot.cnds.io/jars --exclude "*" --include "phoenixbot*standalone.jar"

echo "Update with this:"
echo https://s3.amazonaws.com/phoenixbot.cnds.io/jars/`cd target && ls *-standalone.jar`
