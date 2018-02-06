#!/bin/ksh

# Common functions that can be used throughout multiple scripts
# In order to call these functions, this file needs to be sourced

# Checks if the user that is currently running is aaiadmin
check_user(){

    userid=$( id | cut -f2 -d"(" | cut -f1 -d")" )

    if [ "${userid}" != "aaiadmin" ]; then
        echo "You must be aaiadmin to run $0. The id used $userid."
        exit 1
    fi
}

# Sources the profile and sets the project home
source_profile(){
    . /etc/profile.d/aai.sh
    PROJECT_HOME=/opt/app/aai-traversal
}

# Runs the spring boot jar based on which main class
# to execute and which logback file to use for that class
execute_spring_jar(){

    className=$1;
    logbackFile=$2;

    shift 2;

    EXECUTABLE_JAR=$(ls ${PROJECT_HOME}/lib/aai-traversal-*SNAPSHOT.jar);

    JAVA_OPTS="${JAVA_PRE_OPTS}";
    JAVA_OPTS="-DAJSC_HOME=$PROJECT_HOME";
    JAVA_OPTS="$JAVA_OPTS -DBUNDLECONFIG_DIR=resources";
    JAVA_OPTS="$JAVA_OPTS -Daai.home=$PROJECT_HOME ";
    JAVA_OPTS="$JAVA_OPTS -Dhttps.protocols=TLSv1.1,TLSv1.2";
    JAVA_OPTS="$JAVA_OPTS -Dloader.main=${className}";
    JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=${logbackFile}";
    JAVA_OPTS="${JAVA_OPTS} ${JAVA_POST_OPTS}";

    ${JAVA_HOME}/bin/java ${JVM_OPTS} ${JAVA_OPTS} -jar ${EXECUTABLE_JAR} "$@"
}

# Prints the start date and the script that the user called
start_date(){
    echo
    echo `date` "   Starting $0"
}

# Prints the end date and the script that the user called
end_date(){
    echo
    echo `date` "   Done $0"
}
