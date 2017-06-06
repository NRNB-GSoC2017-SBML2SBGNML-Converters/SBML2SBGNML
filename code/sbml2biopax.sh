#!/bin/bash

#RESOLVE_LINK=`readlink -e $0`  // the option '-e' does not exist on tomcat-11/12
RESOLVE_LINK=`readlink -f $0`

SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`


${SBF_CONVERTER_HOME}/sbml2biopax2.sh $@

${SBF_CONVERTER_HOME}/sbml2biopax3.sh $@


