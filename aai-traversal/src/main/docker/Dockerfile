FROM aaionap/aai-common:@aai.docker.version@-@aai.base.image@

RUN  mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-GQ

VOLUME /opt/aai/logroot/AAI-GQ
VOLUME /tmp
VOLUME /opt/tools

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD nc -z -v localhost 8446 || exit 1

# Add the proper files into the docker image from your build
WORKDIR /opt/app/aai-traversal
COPY /maven/aai-traversal/ .

ENV AAI_BUILD_VERSION @aai.docker.version
# Expose the ports for outside linux to use
# 8446 is the important one to be used
EXPOSE 8446
ENTRYPOINT ["/bin/bash", "/opt/app/aai-traversal/docker-entrypoint.sh"]