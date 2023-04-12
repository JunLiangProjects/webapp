#!/bin/bash
sudo yum install -y java-17-amazon-corretto.x86_64 -y
sudo yum install amazon-cloudwatch-agent -y
sudo mkdir /home/ec2-user/webapp
sudo touch /home/ec2-user/webapp/application.properties
{
  sudo echo "publish.metrics=true"
  sudo echo "metrics.server.hostname=localhost"
  sudo echo "metrics.server.port=8215"
  sudo echo "logging.file=/home/ec2-user/webapp/log/webapp.log"
} >>/home/ec2-user/webapp/application.properties
sudo mkdir /home/ec2-user/webapp/log
sudo touch /home/ec2-user/webapp/log/amazon-cloudwatch-agent.log
sudo touch /home/ec2-user/webapp/log/webapp.log

#put this back to terraform after interactive test
#sudo echo "logging.file=${catalina.base}/logs/csye6225.log" >> /tmp/webapp/application.properties