#!/bin/bash

###
# ============LICENSE_START=======================================================
# org.openecomp.aai
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
#       Script to initialize the chef-repo branch and.chef
#
##############################################################################

#echo "AAI_CHEF_ENV=${AAI_CHEF_ENV}" >> /etc/environment
#echo "AAI_CHEF_LOC=${AAI_CHEF_LOC}" >> /etc/environment
#touch /root/.bash_profile
chef-solo -c /var/chef/aai-data/chef-config/dev/.knife/solo.rb -j /var/chef/aai-config/cookbooks/runlist-app-server.json -E ${AAI_CHEF_ENV}

