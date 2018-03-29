FROM aaionap/aai-common:1.2.0


# Add the proper files into the docker image from your build
WORKDIR /opt/app/aai-traversal

# Expose the ports for outside linux to use
# 8446 is the important one to be used
EXPOSE 8446

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD nc -z -v localhost 8446 || exit 1

ENTRYPOINT ["/bin/bash", "/opt/app/aai-traversal/docker-entrypoint.sh"]

RUN  mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-GQ

VOLUME /opt/aai/logroot/AAI-GQ

COPY /maven/aai-traversal/ .

ENV AAI_BUILD_VERSION @aai.docker.version@
