#!/bin/bash

## Example script to use the System Biology Format Converter to convert an SBML model into octave.

#RESOLVE_LINK=`readlink -e $0`  // the option '-e' does not exist on tomcat-11/12
RESOLVE_LINK=`readlink -f $0`

SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`

${SBF_CONVERTER_HOME}/sbfConverter.sh SBMLModel SBML2Octave $1

