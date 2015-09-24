# phoenixbot
Various and sundry pivotal / github / hipchat integrations running on AWS Lambda

You'll need to update scripts/creds with the phoenixbot's AWS credentials (see scripts/creds.sample).

My workflow is to do some new code, and then commit it. Then run ./scripts/release.sh

This will make an uberjar and then sync the entire target dir with S3 (i knoow a waste, but works for now).

You can take that s3 location, and update the Lambda function through the UI Console.


There will probably be a number of different lambda clojure namespaces.

ElasticBeanstalk
Pivotal
Github
HipChat
etc.
