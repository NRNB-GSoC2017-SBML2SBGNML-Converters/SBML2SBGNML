#!/bin/bash

## Example script to use the System Biology Format Converter to convert an SBML model into SBML.

#export LD_LIBRARY_PATH="/ebi/research/software/Linux_x86_64/opt/stow/libsbml-5.0.0-libxml2-centos-4.6/lib"
export LD_LIBRARY_PATH=/ebi/research/software/Linux_x86_64/opt/stow/libsbml-5.7.0-Linux-x64/lib64

RESOLVE_LINK=`readlink -f $0`

SBF_CONVERTER_HOME=`dirname ${RESOLVE_LINK}`

${SBF_CONVERTER_HOME}/sbfConverter.sh SBMLModel SBML2SBML_$2 $1
