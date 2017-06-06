#!/bin/bash

RESOLVE_LINK=`readlink -f $0`
SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`
LIB_PATH=${SBF_CONVERTER_HOME}/lib

export LD_LIBRARY_PATH=/usr/lib64/

export CLASSPATH=
for jarFile in $LIB_PATH/*.jar
do
    export CLASSPATH=$CLASSPATH:$jarFile
done
export CLASSPATH=$CLASSPATH:$LIB_PATH/paxtools-4.2/paxtools-4.2.0-no-jena.jar

COMMAND="java "


eval $COMMAND org.util.classlist.ConverterSearcher
