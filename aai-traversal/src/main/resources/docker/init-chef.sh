#!/bin/bash
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

##############################################################################
#       Script to initialize the chef-repo branch and.chef
#
##############################################################################

cd /var/chef;

if [ ! -d "aai-config" ]; then

    git clone --depth 1 -b ${CHEF_BRANCH} --single-branch ${CHEF_CONFIG_GIT_URL}/${CHEF_CONFIG_REPO}.git aai-config || {
        echo "Error: Unable to clone the aai-config repo with url: ${CHEF_GIT_URL}/${CHEF_CONFIG_REPO}.git";
        exit;
    }

fi

if [ -d "aai-config/cookbooks/aai-traversal" ]; then

    (cd aai-config/cookbooks/aai-traversal/ && \
        for f in $(ls); do mv $f ../; done && \
        cd ../ && rmdir aai-traversal);

fi;

if [ ! -d "aai-data" ]; then

    git clone --depth 1 -b ${CHEF_BRANCH} --single-branch ${CHEF_DATA_GIT_URL}/aai-data.git aai-data || {
        echo "Error: Unable to clone the aai-data repo with url: ${CHEF_GIT_URL}";
        exit;
    }

fi

chef-solo \
   -c /var/chef/aai-data/chef-config/dev/.knife/solo.rb \
   -j /var/chef/aai-config/cookbooks/runlist-aai-traversal.json \
   -E ${AAI_CHEF_ENV};

