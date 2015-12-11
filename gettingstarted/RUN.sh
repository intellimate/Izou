#!/bin/bash

mkdir /home/izou
mv izou.jar /home/izou/
mv izou-start.sh /usr/local/bin/
mv izou-stop.sh /usr/local/bin/
mv izou /etc/init.d

update-rc.d izou defaults