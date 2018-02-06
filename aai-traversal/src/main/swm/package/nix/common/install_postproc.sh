#!/bin/sh

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

##############################################################################
# - SCLD GRM SERVICE
# - Copyright 2009 AT&T Intellectual Properties
##############################################################################


. `dirname $0`/install.env

cd ${ROOT_DIR};

TEMPLATE_YAML_FILE=${ROOT_DIR}/docker-compose.template.yaml
YAML_FILE=${ROOT_DIR}/docker-compose.yaml

sh ${UTILPATH}/findreplace.sh ${TEMPLATE_YAML_FILE} ${YAML_FILE} || exit 200
rm ${TEMPLATE_YAML_FILE}

exit 0
