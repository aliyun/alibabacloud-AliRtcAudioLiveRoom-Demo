#!/bin/sh

lsof -i:8080 | grep java | awk '{print $2}' | xargs kill -9

#nohup java org.springframework.boot.loader.JarLauncher >/dev/null 2>&1 &
java org.springframework.boot.loader.JarLauncher