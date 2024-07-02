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
# The script is called with a resource.
# It invokes a GET on the resource using curl
# Uses aaiconfig.properties for authorization type and url.

# remove leading slash when present
RESOURCE=`echo $1 | sed "s,^/,,"`
if [ -z $RESOURCE ]; then
        echo "resource parameter is missing"
        echo "usage: $0 resource file [expected-failure-codes]"
        exit 1
fi
echo `date` "   Starting $0 for resource $RESOURCE"

XFROMAPPID="AAI-TOOLS"
XTRANSID=`uuidgen`

userid=$( id | cut -f2 -d"(" | cut -f1 -d")" )
if [ "${userid}" != "aaiadmin" ]; then
    echo "You must be aaiadmin to run $0. The id used $userid."
    exit 1
fi


PROJECT_HOME=/opt/app/aai-traversal
prop_file=$PROJECT_HOME/resources/etc/appprops/aaiconfig.properties
log_dir=$PROJECT_HOME/logs/misc
today=$(date +\%Y-\%m-\%d)

MISSING_PROP=false
RESTURL=`grep ^aai.server.url= $prop_file |cut -d'=' -f2 |tr -d "\015"`
if [ -z $RESTURL ]; then
        echo "Property [aai.server.url] not found in file $prop_file"
        MISSING_PROP=true
fi
USEBASICAUTH=false
BASICENABLE=`grep ^aai.tools.enableBasicAuth $prop_file |cut -d'=' -f2 |tr -d "\015"`
if [ -z $BASICENABLE ]; then
        USEBASICAUTH=false
else
        USEBASICAUTH=true
        CURLUSER=`grep ^aai.tools.username $prop_file |cut -d'=' -f2 |tr -d "\015"`
        if [ -z $CURLUSER ]; then
                echo "Property [aai.tools.username] not found in file $prop_file"
                MISSING_PROP=true
        fi
        CURLPASSWORD=`grep ^aai.tools.password $prop_file |cut -d'=' -f2 |tr -d "\015"`
        if [ -z $CURLPASSWORD ]; then
                echo "Property [aai.tools.password] not found in file $prop_file"
                MISSING_PROP=true
        fi
fi

if [ $MISSING_PROP = false ]; then
        if [ $USEBASICAUTH = false ]; then
                AUTHSTRING="--cert $PROJECT_HOME/resources/etc/auth/aaiClientPublicCert.pem --key $PROJECT_HOME/resources/etc/auth/aaiClientPrivateKey.pem"
        else
                AUTHSTRING="-u $CURLUSER:$CURLPASSWORD"
        fi
        curl --request GET -sL -k $AUTHSTRING -H "X-FromAppId: $XFROMAPPID" -H "X-TransactionId: $XTRANSID" -H "Accept: application/json" $RESTURL$RESOURCE
        RC=$?;
else
        echo "usage: $0 resource"
        RC=-1
fi

echo `date` "   Done $0, returning $RC"
exit $RC
