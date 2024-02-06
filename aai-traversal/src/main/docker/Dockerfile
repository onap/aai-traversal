FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

USER root

RUN groupadd aaiadmin -g 1000 \
  && adduser -u 1000 -h /opt/aaihome/aaiadmin -S -D -G aaiadmin -s /bin/bash aaiadmin \
  && mkdir -p /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-GQ \
      /opt/app/aai-traversal \
  && chown -R aaiadmin:aaiadmin /opt/aaihome/aaiadmin /opt/aai/logroot/AAI-GQ \
      /opt/app/aai-traversal /etc/profile.d /opt/app /opt/aai/logroot \
      /etc/profile.d \
      /opt/app

VOLUME /tmp
VOLUME /opt/tools

HEALTHCHECK --interval=40s --timeout=10s --retries=3 CMD nc -z -v localhost 8446 || exit 1

# Add the proper files into the docker image from your build
WORKDIR /opt/app/aai-traversal

COPY --chown=aaiadmin:aaiadmin /maven/aai-traversal/ .

USER aaiadmin

ENV AAI_BUILD_VERSION @aai.docker.version@
# Expose the ports for outside linux to use
# 8446 is the important one to be used
EXPOSE 8446
ENTRYPOINT ["/bin/bash", "/opt/app/aai-traversal/docker-entrypoint.sh"]