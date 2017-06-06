#!/bin/bash

SBF_CONVERTER_HOME=`dirname $0`
LIB_PATH=${SBF_CONVERTER_HOME}/lib

# Needed for the SBML2SBML converters
#LD_LIBRARY_PATH=/ebi/research/software/Linux_x86_64/opt/stow/libsbml-5.0.0-libxml2-centos-4.6/lib/
LD_LIBRARY_PATH=/ebi/research/software/Linux_x86_64/opt/stow/libsbml-5.3.0-libxml2-centos5/lib/

if [ $# -lt 2 ] 
 then
     echo "Usage: "
     echo "       $0 [-i|-m|-u] [file.xml | folder] [file suffix]"     
     echo "              will transform the given file(s) and update his annotations."
     echo ""
     echo "              -m will update the given sbml file to use miriam urn-uris"
     echo "              -i will update the given sbml file to use miriam url-uris (identifiers.org urls)"
     echo "              -u will update the given sbml file to the correct and up-to-date miriam urn-uris"

     exit 1
fi

export ANNO_UPDATE_OPTION=$1
SBML_DIR=$2
FILE_SUFFIX=""
CONVERTER_NAME="identifiersUtilLibSBML"

if [ $# -eq 3 ] 
 then
	FILE_SUFFIX=$3
fi

LOG_FILE_FOLDER=${SBF_CONVERTER_HOME}/log/`basename $SBML_DIR .xml`
LOG_FILE=${LOG_FILE_FOLDER}/`basename $SBML_DIR .xml`-$CONVERTER_NAME-export-`date +%F`.log

COMMAND="bsub -o $LOG_FILE java "

if [ "`which bsub 2> /dev/null`" == "" ] ; then
    COMMAND="java "
fi

export CLASSPATH=

for jarFile in $LIB_PATH/*.jar
do
    export CLASSPATH=$CLASSPATH:$jarFile
done

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
	if [ "$COMMAND" != "java " ] ; then
	    COMMAND="bsub -o $LOG_FILE_MULTI java  "
	fi

	echo "------------------------------------------------------------" >> $LOG_FILE_MULTI   2>&1
	echo "`date +"%F %R"`" >> $LOG_FILE_MULTI  2>&1
	echo "`basename $0`: Convertion, using $CONVERTER_NAME, for '$file'..." >> $LOG_FILE_MULTI  2>&1
	echo "------------------------------------------------------------" >> $LOG_FILE_MULTI  2>&1

	$COMMAND org.sbfc.converter.sbml2sbml.IdentifiersUtilLibSBML ${ANNO_UPDATE_OPTION} $file $FILE_SUFFIX >> $LOG_FILE_MULTI  2>&1
	sleep 0.3
    done
else

    # checks that the model specific folder does exist and create it if not.
    if [ ! -d "$LOG_FILE_FOLDER" ]; then
	mkdir -p $LOG_FILE_FOLDER
    fi

    file=${SBML_DIR}    

    echo "------------------------------------------------------------" >> $LOG_FILE  2>&1
    echo "`date +"%F %R"`" >> $LOG_FILE  2>&1
    echo "`basename $0`: Convertion, using $CONVERTER_NAME, for '$file'..." >> $LOG_FILE  2>&1
    echo "------------------------------------------------------------" >> $LOG_FILE  2>&1

    $COMMAND  org.sbfc.converter.sbml2sbml.IdentifiersUtilLibSBML ${ANNO_UPDATE_OPTION} $file $FILE_SUFFIX
##  >> $LOG_FILE  2>&1

fi


