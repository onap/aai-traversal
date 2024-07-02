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
# The script is called with a resource, filepath, an optional argument to
# ignore HTTP failure codes which would otherwise indicate a failure,
# and an optional argument to display more data.
# It invokes a PUT on the resource with the file using curl
# Uses aaiconfig.properties for authorization type and url. The HTTP response
# code is checked. Responses between 200 and 299 are considered success.
# When the ignore failure code parameter is passed, responses outside of
# the 200 to 299 range but matching a sub-string of the parameter are
# considered success. For example, a parameter value of 412 will consider
# responses in the range of 200 to 299 and 412 successes.
#
# method checking parameter list for two strings, and determine if
# the second string is a sub-string of the first
contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "$string"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

display_usage() {
        cat <<EOF
        Usage: $0 [options]

        1. Usage: putTool.sh <resource-path> <json payload file> <optional -display>
        2. This script requires two arguments, a resource path and a file path to a json file containing the payload.
        3. Example: resource-path and payload for a particular customer is: business/customers/customer/JohnDoe customerpayload.json
        4. Adding the optional "-display" argument will display all data returned from the request.
EOF
}
if [ $# -eq 0 ]; then
        display_usage
        exit 1
fi

# remove leading slash when present
RESOURCE=`echo $1 | sed "s,^/,,"`
if [ -z $RESOURCE ]; then
        echo "resource parameter is missing"
        echo "usage: $0 resource file [expected-failure-codes]"
        exit 1
fi
JSONFILE=$2
if [ -z $JSONFILE ]; then
        echo "json file parameter is missing"
        echo "usage: $0 resource file [expected-failure-codes]"
        exit 1
fi
echo `date` "   Starting $0 for resource $RESOURCE"
ALLOWHTTPRESPONSES=$3

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

RETURNRESPONSE=false
if [ ${#} -ne 2 ]; then
    if [ "$3" = "-display" ]; then
        RETURNRESPONSE=true
    fi
fi
if [ ${#} -ne 3 ]; then
    if [ "$4" = "-display" ]; then
        RETURNRESPONSE=true
    fi
fi

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

fname=$JSONFILE
if [ -f /tmp/$(basename $JSONFILE) ]; then
	fname=/tmp/$(basename $JSONFILE)
elif [ ! -f $JSONFILE ]; then
	echo "The file $JSONFILE does not exist"
	exit -1
fi

if [ $MISSING_PROP = false ]; then
        if [ $USEBASICAUTH = false ]; then
                AUTHSTRING="--cert $PROJECT_HOME/resources/etc/auth/aaiClientPublicCert.pem --key $PROJECT_HOME/resources/etc/auth/aaiClientPrivateKey.pem"
        else
                AUTHSTRING="-u $CURLUSER:$CURLPASSWORD"
        fi
        if [ $RETURNRESPONSE = true ]; then
			curl --request PUT -sL -k $AUTHSTRING -H "X-FromAppId: $XFROMAPPID" -H "X-TransactionId: $XTRANSID" -H "Accept: application/json" -H "Content-Type: application/json" -T $fname $RESTURL$RESOURCE | jq '.'
			RC=$?
		else
        	result=`curl --request PUT -w "%{http_code}" -o /dev/null -k $AUTHSTRING -H "X-FromAppId: $XFROMAPPID" -H "X-TransactionId: $XTRANSID" -H "Accept: application/json" -H "Content-Type: application/json" -T $fname $RESTURL$RESOURCE`
        	#echo "result is $result."
        	RC=0;
        	if [ $? -eq 0 ]; then
                case $result in
                        +([0-9])?)
                                if [[ "$result" -ge 200 && $result -lt 300 ]]
                                then
                                        echo "PUT result is OK,  $result"
                                else
                                        if [ -z $ALLOWHTTPRESPONSES ]; then
                                                echo "PUT request failed, response code was  $result"
                                                RC=$result
                                        else
                                                contains $ALLOWHTTPRESPONSES $result
                                                if [ $? -ne 0 ]
                                                then
                                                        echo "PUT request failed, unexpected response code was  $result"
                                                        RC=$result
                                                else
                                                        echo "PUT result is expected,  $result"
                                                fi
                                        fi
                                fi
                                ;;
                        *)
                                echo "PUT request failed, response was $result"
                                RC=-1
                                ;;

                esac
        	else
                echo "FAILED to send request to $RESTURL"
                RC=-1
        	fi
        fi
else
        echo "usage: $0 resource file [expected-failure-codes]"
        RC=-1
fi

echo `date` "   Done $0, returning $RC"
exit $RC
