FROM aaionap/aai-common:1.1.0

RUN mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-RES /opt/app/aai-traversal/bin /opt/app/aai-traversal/extApps; 

# Add the proper files into the docker image from your build
ADD ./opt/app/aai-traversal /opt/app/aai-traversal
ADD ./commonLibs/           /opt/app/commonLibs/
ADD init-chef.sh 	    /opt/app/aai-traversal/init-chef.sh
ADD docker-entrypoint.sh    /opt/app/aai-traversal/docker-entrypoint.sh
ADD aai.sh 		    /etc/profile.d/aai.sh

# Change the permissions of aai shell script and all scripts in the microservice to be executable
RUN chmod 644 /etc/profile.d/aai.sh && \
	find /opt/app/aai-traversal -name "*.sh" -exec chmod 755 {} + 

# Set the log directory to be mountable so the logs will be persisted
VOLUME /opt/aai/logroot/AAI-GQ

# Expose the ports that needs to be available from the container
EXPOSE 8447

# During the startup of the container, this will be the starting directory
WORKDIR /opt/app/aai-traversal

# When the container is started this is the entrypoint script
# that docker will run. Make sure this script doesn't end abruptly
# Docker entrypoint will run exec command to make that main process
# Using the exec form here will ensure the java process is main

ENTRYPOINT ["/bin/bash", "/opt/app/aai-traversal/docker-entrypoint.sh"]

