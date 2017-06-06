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


# Setting up java
export JAVA_HOME=/nfs/public/rw/webadmin/java/jdks/latest_1.8
export PATH=${JAVA_HOME}/bin:${PATH}

COMMAND="bsub ${BSUB_OPTIONS} -o $LOG_FILE java $PROPERTIES"

if [ "`which bsub 2> /dev/null`" == "" ] ; then
    COMMAND="java $PROPERTIES"
fi




RESOLVE_LINK=`readlink -f $0`
SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`
LIB_PATH=${SBF_CONVERTER_HOME}/lib

if [ $# -lt 3 ] 
 then
     echo ""
     echo "Usage: "
     echo "    To convert a given file(s) of a specific model type using a provided converter"
     echo "       $0 InputModelType ConverterName [file | folder]" 
     echo ""
     echo "    For instance, to convert an SBML file to XPP : "
     echo "       $0 SBMLModel SBML2XPP [file.xml | folder]" 
     echo ""
     echo "    For retrieving the complete lists of the available models and converters, type the commands"
     echo "       ./sbfModelList.sh"
     echo "       ./sbfConverterList.sh"
     echo ""
     exit 1
fi

MODEL_NAME=$1
CONVERTER_NAME=$2
SBML_DIR=$3

LOG_FILE_FOLDER=${SBF_CONVERTER_HOME}/log/`basename $SBML_DIR .xml`
LOG_FILE=${LOG_FILE_FOLDER}/`basename $SBML_DIR .xml`-$CONVERTER_NAME-export-`date +%F`.log



PATHVISIO_CONVERTER="no"

if [[ ${CONVERTER_NAME} == *GPML* ]];
then
    PATHVISIO_CONVERTER="yes"
fi

export CLASSPATH=

for jarFile in $LIB_PATH/*.jar
do
    export CLASSPATH=$CLASSPATH:$jarFile
done

if [ ${PATHVISIO_CONVERTER} == "no" ];
then
    export CLASSPATH=$CLASSPATH:$LIB_PATH/paxtools-4.2/paxtools-4.2.0-no-jena.jar
else
    for jarFile in $LIB_PATH/pathvisio/*.jar
    do
	export CLASSPATH=$CLASSPATH:$jarFile
    done
    if [[ ${CONVERTER_NAME} == *BioPAX2GPML* ]];
    then
	echo "Using the BioPaxOldModel class for BioPAX2GPML"
	MODEL_NAME=BioPaxOldModel
    fi
fi

if [ -d $SBML_DIR ]
then
    for file in $SBML_DIR/*[0-9].xml
    do
        # Creating a log file specific to each file.
	LOG_FILE_FOLDER=${SBF_CONVERTER_HOME}/log/`basename $file .xml`
	LOG_FILE_MULTI=${LOG_FILE_FOLDER}/`basename $file .xml`-$CONVERTER_NAME-export-`date +%F`.log

	# checks that the model specific folder does exist and create it if not.
	if [ ! -d "$LOG_FILE_FOLDER" ]; then
	    mkdir -p $LOG_FILE_FOLDER
	fi
	if [ ! "1${COMMAND}" == "1java " ] ; then
	    # we are on a cluster node
	    COMMAND="bsub ${BSUB_OPTIONS} -o $LOG_FILE_MULTI java "
	fi

	echo "------------------------------------------------------------" >> $LOG_FILE_MULTI   2>&1
	echo "`date +"%F %R"`" >> $LOG_FILE_MULTI  2>&1
	echo "`basename $0`: Convertion, using $CONVERTER_NAME, for '$file'..." >> $LOG_FILE_MULTI  2>&1
	echo "------------------------------------------------------------" >> $LOG_FILE_MULTI  2>&1

	eval $COMMAND -Dmiriam.xml.export=${SBF_CONVERTER_HOME}/miriam.xml org.sbfc.converter.Converter $MODEL_NAME $CONVERTER_NAME $file >> $LOG_FILE_MULTI  2>&1
	sleep 0.1
    done
else

    file=$SBML_DIR

    # checks that the model specific folder does exist and create it if not.
    if [ ! -d "$LOG_FILE_FOLDER" ]; then
	mkdir -p $LOG_FILE_FOLDER
    fi

    echo "------------------------------------------------------------" >> $LOG_FILE  2>&1
    echo "`date +"%F %R"`" >> $LOG_FILE  2>&1
    echo "`basename $0`: Convertion, using $CONVERTER_NAME, for '$SBML_DIR'..." >> $LOG_FILE  2>&1
    echo "------------------------------------------------------------" >> $LOG_FILE  2>&1
    
    eval $COMMAND -Dmiriam.xml.export=${SBF_CONVERTER_HOME}/miriam.xml org.sbfc.converter.Converter $MODEL_NAME $CONVERTER_NAME $SBML_DIR >> $LOG_FILE  2>&1
    touch `dirname $file`/`basename $file .input`.done

fi


