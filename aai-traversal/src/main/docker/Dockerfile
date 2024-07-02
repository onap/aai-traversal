FROM eclipse-temurin:8-jre-alpine
# curl is used in the putTool script
# (PUT's can't be done using the busybox wget)
RUN apk --no-cache add curl

USER nobody
ENV SERVER_PORT=8446
EXPOSE ${SERVER_PORT}

WORKDIR /opt/app/aai-traversal

COPY --chown=nobody:nobody /maven/aai-traversal/ .

ENTRYPOINT ["/bin/sh", "/opt/app/aai-traversal/docker-entrypoint.sh"]
