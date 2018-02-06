#!/bin/bash

export WORKING_DIR="$( cd "$(dirname "$0")" ; pwd -P )/"

DOCKER_COMPOSE_CMD="docker-compose -f ${WORKING_DIR}/docker-compose.yaml";

ARG=$1;

if [ -z "$ARG" ]; then
        echo "Error: You need to at least provide one argument which is the script to execute";
        exit 1;
fi;

if [ "${ARG}" = "--debug" ]; then

    SCRIPT_NAME=$2;

    if [ -z "$SCRIPT_NAME" ]; then
        echo "Error: You need to provide the tool name after specifying the --debug flag";
        exit 1;
    fi;

    shift 2;

else
    SCRIPT_NAME=$1;
    shift 1;
fi;

CONTAINER_NAME=$(${DOCKER_COMPOSE_CMD} ps -q aai-traversal);

if [ $? -ne 0 ]; then
    echo "Error: seems like the container is not running, please run the commands to start aai-traversal";
    exit 1;
fi;

if [ ${SCRIPT_NAME} = "putTool.sh" ]; then

    PAYLOAD_FILE=$2;

    if [ ! -z "${PAYLOAD_FILE}" ] && [ -f "${PAYLOAD_FILE}" ]; then
        docker cp ${PAYLOAD_FILE} ${CONTAINER_NAME}:/tmp/$(basename ${PAYLOAD_FILE})
    fi;
fi;

${DOCKER_COMPOSE_CMD} exec --user aaiadmin aai-traversal ls /opt/app/aai-traversal/scripts/${SCRIPT_NAME} && {

    if [ "${ARG}" = "--debug" ]; then
        ${DOCKER_COMPOSE_CMD} exec --user aaiadmin aai-traversal bash -x /opt/app/aai-traversal/scripts/${SCRIPT_NAME} "$@"
    else
        ${DOCKER_COMPOSE_CMD} exec --user aaiadmin aai-traversal /opt/app/aai-traversal/scripts/${SCRIPT_NAME} "$@"
    fi;

    exit 0;
} || {
    echo "Unable to find the tool in the /opt/app/aai-traversal/scripts";
    exit 1;
}
