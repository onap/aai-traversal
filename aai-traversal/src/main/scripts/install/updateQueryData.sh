#!/bin/sh
#
# ============LICENSE_START=======================================================
# org.onap.aai
# ================================================================================
# Copyright © 2017 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
#
# ECOMP is a trademark and service mark of AT&T Intellectual Property.
#

PROGNAME=$(basename $0)

: ${PROJECT_HOME:=/opt/app/aai-traversal}

if [ "$1" = "--debug" ]; then
    set -x;
fi;

TS=$(date "+%Y-%m-%d %H:%M:%S")

error_exit () {
	echo "${PROGNAME}: failed for ${1:-"Unknown error"} on cmd $2" 1>&2
#	exit ${2:-"1"}
}

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/widget-model-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for widget $filename" | tee -a $OUTFILE
vers=`grep model-invariant-id $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/models/model/$vers
if [ "$1" = "--debug" ]; then
  sh -x $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for widget $filename"
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/named-query-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for named-query $filename"
vers=`grep named-query-uuid $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/named-queries/named-query/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for named-query $filename"
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/resource-model-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for resource model $filename"
vers=`grep model-invariant-id $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/models/model/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for resource model $filename"
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/service-model-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for service model $filename"
vers=`grep model-invariant-id $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/models/model/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/bin/putTool.sh $resource $filepath 412 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for service model $filename"
done

echo "$PROGNAME completed ${TS}"

exit 0
