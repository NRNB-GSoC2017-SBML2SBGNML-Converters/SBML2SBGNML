@echo off

rem Example script to use the System Biology Format Converter to convert an SBML model into octave.

set CONVERTER_HOME=%~dp0

%CONVERTER_HOME%\sbfConverter.bat SBMLModel SBML2Octave %*

