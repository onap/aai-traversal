#!/bin/bash

. /etc/profile.d/aai.sh
PROJECT_HOME=/opt/app/aai-traversal

docker-compose -f ${PROJECT_HOME}/docker-compose.yaml stop || exit 200
