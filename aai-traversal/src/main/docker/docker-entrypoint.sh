###
# ============LICENSE_START=======================================================
# org.onap.aai
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###

APP_HOME=$(pwd);
RESOURCES_HOME=${APP_HOME}/resources/;

export SERVER_PORT=${SERVER_PORT:-8446};

USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}

find /opt/app/ -name "*.sh" -exec chmod +x {} +

if [ -z ${DISABLE_UPDATE_QUERY} ]; then
    UPDATE_QUERY_RAN_FILE="updateQueryRan.txt";
    /opt/app/aai-traversal/bin/install/updateQueryData.sh
    touch ${UPDATE_QUERY_RAN_FILE};
fi

MIN_HEAP_SIZE=${MIN_HEAP_SIZE:-512m};
MAX_HEAP_SIZE=${MAX_HEAP_SIZE:-1024m};
MAX_METASPACE_SIZE=${MAX_METASPACE_SIZE:-512m};

JAVA_CMD="exec java";

JVM_OPTS="${PRE_JVM_ARGS}";
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=/opt/app/aai-traversal/logs/ajsc-jetty/heap-dump";
JVM_OPTS="${JVM_OPTS} -XX:MaxMetaspaceSize=${MAX_METASPACE_SIZE}";
JVM_OPTS="${JVM_OPTS} -XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE:-70}";

JVM_OPTS="${JVM_OPTS} -server";
JVM_OPTS="${JVM_OPTS} -XX:NewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:MaxNewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:SurvivorRatio=8";
JVM_OPTS="${JVM_OPTS} -XX:+DisableExplicitGC";
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC";
JVM_OPTS="${JVM_OPTS} -XX:ParallelGCThreads=4";
JVM_OPTS="${JVM_OPTS} -XX:LargePageSizeInBytes=128m";
JVM_OPTS="${JVM_OPTS} -Dsun.net.inetaddr.ttl=180";
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError";
JVM_OPTS="${JVM_OPTS} ${POST_JVM_ARGS}";

JAVA_OPTS="${PRE_JAVA_OPTS} -DAJSC_HOME=$APP_HOME";
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}";
JAVA_OPTS="${JAVA_OPTS} -DBUNDLECONFIG_DIR=./resources";
JAVA_OPTS="${JAVA_OPTS} -Dserver.local.startpath=${RESOURCES_HOME}";
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom";
JAVA_OPTS="${JAVA_OPTS} -Dlogback.configurationFile=./resources/logback.xml";
JAVA_OPTS="${JAVA_OPTS} -Dloader.path=$APP_HOME/resources";
JAVA_OPTS="${JAVA_OPTS} -Dgroovy.use.classvalue=true";
JAVA_OPTS="${JAVA_OPTS} ${POST_JAVA_OPTS}";

JAVA_MAIN_JAR=$(ls lib/aai-traversal*.jar);

${JAVA_CMD} ${JVM_OPTS} ${JAVA_OPTS} -jar ${JAVA_MAIN_JAR};
