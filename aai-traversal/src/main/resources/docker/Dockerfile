FROM ubuntu:14.04

ENV DEBIAN_FRONTEND noninteractive

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTP_PROXY}
ENV https_proxy ${HTTP_PROXY}
ENV http_proxy  ${HTTP_PROXY}

# Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# Install all the application requirements such as curl ksh and git
# Also install the chef and then remove it in one RUN command
# Because the docker files work like git commits and each command is committed
# So removing the chef in a different command will still have its in its build image
# Its good to be optimizing and removing any files that are not needed for docker images
# for the best possible performance out of your image

RUN if [ ! -z ${HTTP_PROXY} ]; then echo "Acquire::http::proxy  \"${HTTP_PROXY}\";" >> /etc/apt/apt.conf; fi && \
    if [ ! -z ${HTTP_PROXY} ]; then echo "Acquire::https::proxy \"${HTTP_PROXY}\";" >> /etc/apt/apt.conf; fi && \
    apt-get update && \
    apt-get install -y software-properties-common && \
    apt-get install --reinstall ca-certificates && \
    sudo -E add-apt-repository ppa:openjdk-r/ppa && \
    apt-get update && \
    apt-get -qq install -y openjdk-8-jre-headless git curl ksh && \
    curl -k -LO  https://packages.chef.io/stable/ubuntu/14.04/chefdk_0.17.17-1_amd64.deb || \
    curl --tlsv1 -LO https://packages.chef.io/stable/ubuntu/14.04/chefdk_0.17.17-1_amd64.deb && \
    dpkg -i chefdk_0.17.17-1_amd64.deb && \
    rm chefdk_0.17.17-1_amd64.deb && \
    rm -rf /var/lib/apt/lists/*

# Add the proper files into the docker image from your build
ADD ./opt/app /opt/app
ADD ./commonLibs/ /opt/app/commonLibs/
ADD init-chef.sh /init-chef.sh
ADD docker-entrypoint.sh /docker-entrypoint.sh
ADD aai.sh /etc/profile.d/aai.sh

# Expose the ports for outside linux to use
# 8446 is the important one to be used

EXPOSE 8446

# Create the /var/chef if it doesn't exist
WORKDIR /var/chef

# Create the directory structure of aai application resembling the development server
# hard-coding path to match ajsc version

RUN chmod 755 /init-chef.sh /docker-entrypoint.sh && chmod 644 /etc/profile.d/aai.sh && \
    mkdir /opt/aaihome && \
    useradd -r -ms /bin/bash -d /opt/aaihome/aaiadmin aaiadmin && \
    mkdir -p /opt/app/${project.artifactId}  && \
    chown aaiadmin:aaiadmin /opt/app/${project.artifactId} && \
    chown -R aaiadmin:aaiadmin /opt/app/${project.artifactId} && \
    mkdir -p /opt/aai/logroot && \
    chown -R aaiadmin:aaiadmin /opt/aai/logroot && \
    ln -s /opt/app/${project.artifactId}/bin scripts && \
    mkdir -p /opt/app/${project.artifactId}/extApps && chown -R aaiadmin:aaiadmin /opt/app/${project.artifactId}/extApps && \
    find /opt/app/${project.artifactId}/bin -name "*.sh" -exec chmod 755 {} + && \
    chown aaiadmin:aaiadmin /docker-entrypoint.sh && \
    chown -R aaiadmin:aaiadmin /var/chef && \
    mkdir -p /opt/aai/logroot/AAI-GQ && \
    chown aaiadmin:aaiadmin /opt/aai/logroot/AAI-GQ && \
    ln -s /opt/aai/logroot/AAI-GQ /opt/app/${project.artifactId}/logs && \
    chown -R aaiadmin:aaiadmin /opt/app/${project.artifactId}/logs

VOLUME /opt/aai/logroot/AAI-GQ

WORKDIR /

USER aaiadmin

# When the container is started this is the entrypoint script
# that docker will run. Make sure this script doesn't end abruptly
# If you want the container running even if the main application stops
# You can run a ever lasting process like tail -f /dev/null
# Or something like that at the end of the docker-entrypoint script
# So if the main application you are planning on running fails
# the docker container keeps on running forever

ENTRYPOINT ./docker-entrypoint.sh

