#!/bin/bash

# Setting up libsbml, needed for the SBML2SBML converters
LD_LIBRARY_PATH=/nfs/production/biomodels/sw/libSBML-5.11.6-Linux/usr/lib64


# Setting up for Java properties
PROPERTIES=""

# Setting up antimony converters if used. 
# This can be done by either specifying a Java system property
#PROPERTIES="$PROPERTIES -DSBTRANSLATE_PATH=/path/to/sbtranslate"
# or an environment variable
#export SBTRANSLATE_PATH=/path/to/sbtranslate

RESOLVE_LINK=`readlink -f $0`
SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`
LIB_PATH=${SBF_CONVERTER_HOME}/lib

export CLASSPATH=

for jarFile in $LIB_PATH/*.jar
do
    export CLASSPATH=$CLASSPATH:$jarFile
done
export CLASSPATH=$CLASSPATH:$LIB_PATH/paxtools-4.2/paxtools-4.2.0-no-jena.jar

java -Dmiriam.xml.export=${SBF_CONVERTER_HOME}/miriam.xml org.sbfc.converter.ConverterGUI 


