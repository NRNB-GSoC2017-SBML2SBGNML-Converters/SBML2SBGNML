#!/bin/bash

## Example script to use the System Biology Format Converter to convert an SBML model with URN into SBML with identifiers.org URL.

RESOLVE_LINK=`readlink -f $0`

SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`

#
# TODO : update when the class is written as a proper SBFC converter
#
##${SBF_CONVERTER_HOME}/sbfConverter.sh SBMLModel URN2URL $1 

${SBF_CONVERTER_HOME}/identifiersUtil.sh -i $@