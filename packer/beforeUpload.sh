#!/bin/bash
sudo yum install -y java-17-amazon-corretto.x86_64 -y
sudo yum install amazon-cloudwatch-agent -y
mkdir /home/ec2-user/webapp
touch /home/ec2-user/webapp/application.properties
{
  echo "publish.metrics=true"
  echo "metrics.server.hostname=localhost"
  echo "metrics.server.port=8215"
  echo "logging.file=/home/ec2-user/webapp/log/webapp.log"
} >>/home/ec2-user/webapp/application.properties
mkdir /home/ec2-user/webapp/log
touch /home/ec2-user/webapp/log/amazon-cloudwatch-agent.log
touch /home/ec2-user/webapp/log/webapp.log

#put this back to terraform after interactive test
#sudo echo "logging.file=${catalina.base}/logs/csye6225.log" >> /tmp/webapp/application.properties