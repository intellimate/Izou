#!/bin/bash

mkdir /home/izou
mv izou.jar /home/izou/
mv izou-start.sh /usr/local/bin/
mv izou-stop.sh /usr/local/bin/
mv izou /etc/init.d

cd /etc/init.d
chmod u+x izou
update-rc.d izou defaults