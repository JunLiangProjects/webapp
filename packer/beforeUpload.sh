#!/bin/bash
# Install JDK
sudo yum install -y java-17-amazon-corretto.x86_64 -y

## Install MySQL Community Version
#sudo amazon-linux-extras install epel -y
#sudo yum install https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm -y
#sudo yum install mysql-community-server -y
#
## Start MySQL service
#sudo systemctl start mysqld
#
## Get temporary root password
#MYSQL_TEMP_PASSWORD=$(sudo grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}')
#
## Set custom root username and password
#MYSQL_ROOT_USERNAME="Jun_Liang"
#MYSQL_ROOT_PASSWORD="Me_262A1a"
##mysqladmin -uroot -proot password "$MYSQL_TEMP_PASSWORD"
#mysql --user=root --password="$MYSQL_TEMP_PASSWORD" --connect-expired-password -e \
#  "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD'; \
#     CREATE USER '$MYSQL_ROOT_USERNAME'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD'; \
#     GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_ROOT_USERNAME'@'localhost' WITH GRANT OPTION; \
#     FLUSH PRIVILEGES;"
#
## Create "cloud_computing" database
#mysql --user="$MYSQL_ROOT_USERNAME" --password="$MYSQL_ROOT_PASSWORD" -e \
#  "CREATE DATABASE cloud_computing;"
#
## Restart MySQL service
#sudo systemctl restart mysqld

# Grant permission for uploading
sudo chmod 777 /etc/systemd/system/