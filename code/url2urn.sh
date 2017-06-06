#!/bin/bash

## Example script to use the System Biology Format Converter to convert an SBML model with  identifiers.org URL into SBML with miriam URN.

RESOLVE_LINK=`readlink -f $0`

SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`

#
# TODO : update when the class is written as a proper SBFC converter
#
##${SBF_CONVERTER_HOME}/sbfConverter.sh SBMLModel URL2URN $1 

${SBF_CONVERTER_HOME}/identifiersUtil.sh -m $@



