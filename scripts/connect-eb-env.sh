source scripts/creds
echo "Connecting $1"
aws elasticbeanstalk update-environment \
        --environment-name $1 \
        --option-settings Namespace="aws:elasticbeanstalk:sns:topics",OptionName="Notification Topic ARN",Value="arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk"
