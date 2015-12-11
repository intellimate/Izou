#!/bin/bash
pid=`ps aux | grep izou | awk '{print $2}'`
kill -9 $pid