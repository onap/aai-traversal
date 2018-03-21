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

export CHEF_CONFIG_REPO=${CHEF_CONFIG_REPO:-aai-config};
export CHEF_GIT_URL=${CHEF_GIT_URL:-http://gerrit.onap.org/r/aai};
export CHEF_CONFIG_GIT_URL=${CHEF_CONFIG_GIT_URL:-$CHEF_GIT_URL};
export CHEF_DATA_GIT_URL=${CHEF_DATA_GIT_URL:-$CHEF_GIT_URL};

export SERVER_PORT=${SERVER_PORT:-8446};

export RESOURCES_HOSTNAME=${RESOURCES_HOSTNAME:-aai-resources.api.simpledemo.onap.org};
export RESOURCES_PORT=${RESOURCES_PORT:-8447};

USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}

if [ $(cat /etc/passwd | grep aaiadmin | wc -l) -eq 0 ]; then
	groupadd aaiadmin -g ${GROUP_ID} || {
		echo "Unable to create the group id for ${GROUP_ID}";
		exit 1;
	}
	useradd --shell=/bin/bash -u ${USER_ID} -g ${GROUP_ID} -o -c "" -m aaiadmin || {
		echo "Unable to create the user id for ${USER_ID}";
		exit 1;
	}
fi;

chown -R aaiadmin:aaiadmin /opt/app /opt/aai/logroot /var/chef
find /opt/app/ -name "*.sh" -exec chmod +x {} +

if [ -f ${APP_HOME}/aai.sh ]; then

    gosu aaiadmin ln -s bin scripts
    gosu aaiadmin ln -s /opt/aai/logroot/AAI-GQ logs

    mv ${APP_HOME}/aai.sh /etc/profile.d/aai.sh

    chmod 755 /etc/profile.d/aai.sh
fi;

if [ -z ${DISABLE_UPDATE_QUERY} ]; then
    UPDATE_QUERY_RAN_FILE="updateQueryRan.txt";
    gosu aaiadmin /opt/app/aai-traversal/bin/install/updateQueryData.sh
    gosu aaiadmin touch ${UPDATE_QUERY_RAN_FILE};
fi

JAVA_CMD="exec gosu aaiadmin java";

JVM_OPTS="${PRE_JVM_OPTS} -XX:+UnlockDiagnosticVMOptions";
JVM_OPTS="${JVM_OPTS} -XX:+UnsyncloadClass";
JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSParallelRemarkEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+UseCMSInitiatingOccupancyOnly";
JVM_OPTS="${JVM_OPTS} -XX:CMSInitiatingOccupancyFraction=70";
JVM_OPTS="${JVM_OPTS} -XX:+ScavengeBeforeFullGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSScavengeBeforeRemark";
JVM_OPTS="${JVM_OPTS} -XX:-HeapDumpOnOutOfMemoryError";
JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC";
JVM_OPTS="${JVM_OPTS} -verbose:gc";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCDetails";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCTimeStamps";
JVM_OPTS="${JVM_OPTS} -XX:MaxPermSize=512M";
JVM_OPTS="${JVM_OPTS} -XX:PermSize=512M";
JVM_OPTS="${JVM_OPTS} -server";
JVM_OPTS="${JVM_OPTS} -XX:NewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:MaxNewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:SurvivorRatio=8";
JVM_OPTS="${JVM_OPTS} -XX:+DisableExplicitGC";
JVM_OPTS="${JVM_OPTS} -verbose:gc";
JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSParallelRemarkEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+CMSClassUnloadingEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC";
JVM_OPTS="${JVM_OPTS} -XX:-UseBiasedLocking";
JVM_OPTS="${JVM_OPTS} -XX:ParallelGCThreads=4";
JVM_OPTS="${JVM_OPTS} -XX:LargePageSizeInBytes=128m";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCDetails";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCTimeStamps";
JVM_OPTS="${JVM_OPTS} -Xloggc:/opt/app/aai-traversal/logs/ajsc-jetty/gc/aai_gc.log";
JVM_OPTS="${JVM_OPTS} -Dsun.net.inetaddr.ttl=180";
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError";
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=/opt/app/aai-traversal/logs/ajsc-jetty/heap-dump";
JVM_OPTS="${JVM_OPTS} ${POST_JVM_OPTS}";

JAVA_OPTS="${PRE_JAVA_OPTS} -DAJSC_HOME=$APP_HOME";
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}";
JAVA_OPTS="${JAVA_OPTS} -DBUNDLECONFIG_DIR=./resources";
JAVA_OPTS="${JAVA_OPTS} -Dserver.local.startpath=${RESOURCES_HOME}";
JAVA_OPTS="${JAVA_OPTS} -DAAI_CHEF_ENV=${AAI_CHEF_ENV}";
JAVA_OPTS="${JAVA_OPTS} -DSCLD_ENV=${SCLD_ENV}";
JAVA_OPTS="${JAVA_OPTS} -DAFT_ENVIRONMENT=${AFT_ENVIRONMENT}";
JAVA_OPTS="${JAVA_OPTS} -DlrmName=com.att.ajsc.traversal";
JAVA_OPTS="${JAVA_OPTS} -DAAI_BUILD_NUMBER=${AAI_BUILD_NUMBER}";
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom";
JAVA_OPTS="${JAVA_OPTS} -Dloader.path=$APP_HOME/resources";
JAVA_OPTS="${JAVA_OPTS} ${POST_JAVA_OPTS}";

JAVA_MAIN_JAR=$(ls lib/aai-traversal*.jar);

${JAVA_CMD} ${JVM_OPTS} ${JAVA_OPTS} -jar ${JAVA_MAIN_JAR};
