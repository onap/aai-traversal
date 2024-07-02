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

#
# This script deletes the named query in resources/etc/scriptdata/named-query-json directory
#

PROGNAME=$(basename $0)
OUTFILE=/opt/app/aai-traversal/logs/misc/${PROGNAME}.log.$(date +\%Y-\%m-\%d)
#OUTFILE=/c/temp/${PROGNAME}.log.$(date +\%Y-\%m-\%d)

TS=$(date "+%Y-%m-%d %H:%M:%S")

error_exit () {
	echo "${PROGNAME}: failed for ${1:-"Unknown error"} on cmd $2" 1>&2
	echo "${PROGNAME}: failed for ${1:-"Unknown error"} on cmd $2" >> $OUTFILE
#	exit ${2:-"1"}
}

j=0
for filepath in `ls /opt/app/aai-traversal/resources/etc/scriptdata/named-query-json/*.json|sort -f`
#for filepath in `ls /c/sources/aai/aaigitnew/resources-local/etc/scriptdata/named-query-json/*.json|sort -f`
do
j=$(expr "$j" + 1)
filename=$(basename $filepath)
echo "Begin deleteTool for named-query $filename" | tee -a $OUTFILE
vers=`grep named-query-uuid $filepath|cut -d':' -f2|cut -d'"' -f2`
resource=service-design-and-creation/named-queries/named-query/$vers
echo "y" | /opt/app/aai-traversal/scripts/deleteTool.sh $resource >> $OUTFILE 2>&1 || error_exit "$resource $filepath" $j
echo "End deleteTool for named-query $filename" | tee -a $OUTFILE
done

echo "$PROGNAME completed ${TS}" | tee -a $OUTFILE
echo "See output and error file: $OUTFILE"

exit 0
