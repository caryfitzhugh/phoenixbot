aws lambda update-function-code --profile=cnds-me --function-name "PhoenixBot_PivotalTracker"  --s3-bucket phoenixbot.cnds.io --s3-key jars/$1 --publish
aws lambda update-function-code --profile=cnds-me --function-name "PhoenixBot_ElasticBeanstalk"  --s3-bucket phoenixbot.cnds.io --s3-key jars/$1 --publish
