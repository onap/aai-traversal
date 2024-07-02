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
PROJECT_HOME=/opt/app/aai-traversal
OUTFILE=$PROJECT_HOME/logs/misc/${PROGNAME}.log.$(date +\%Y-\%m-\%d)

if [ "$1" = "--debug" ]; then
    set -x;
fi;

TS=$(date "+%Y-%m-%d %H:%M:%S")

CHECK_USER="aaiadmin"
userid=$( id | cut -f2 -d"(" | cut -f1 -d")" )
if [ "${userid}" != $CHECK_USER ]; then
    echo "You must be  $CHECK_USER to run $0. The id used $userid."
    exit 1
fi

error_exit () {
	echo "${PROGNAME}: failed for ${1:-"Unknown error"} on cmd $2" 1>&2
	echo "${PROGNAME}: failed for ${1:-"Unknown error"} on cmd $2" >> $OUTFILE
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
  bash -x $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for widget $filename" | tee -a $OUTFILE
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/named-query-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for named-query $filename" | tee -a $OUTFILE
vers=`grep named-query-uuid $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/named-queries/named-query/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for named-query $filename" | tee -a $OUTFILE
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/resource-model-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for resource model $filename" | tee -a $OUTFILE
vers=`grep model-invariant-id $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/models/model/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for resource model $filename" | tee -a $OUTFILE
done

j=0
for filepath in `ls $PROJECT_HOME/resources/etc/scriptdata/service-model-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin putTool for service model $filename" | tee -a $OUTFILE
vers=`grep model-invariant-id $filepath|cut -d':' -f2|cut -d'"' -f2`
# last parameter will skip put if it exists
resource=service-design-and-creation/models/model/$vers
if [ "$1" = "--debug" ]; then
  bash -x $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
else
  $PROJECT_HOME/scripts/putTool.sh $resource $filepath 412 >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
fi;
echo "End putTool for service model $filename" | tee -a $OUTFILE
done

echo "$PROGNAME completed ${TS}" | tee -a $OUTFILE
echo "See output and error file: $OUTFILE"

exit 0
