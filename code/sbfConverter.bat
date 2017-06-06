@echo off

set CONVERTER_HOME=%~dp0

set MODEL_NAME=%1
set CONVERTER_NAME=%2
set file=%3

set PATHVISIO_CONVERTER="no"

if x%CONVERTER_NAME% == x (
  rem putting a fake converter name if it is empty to avoid an error in the next test
  set CONVERTER_NAME="x"
) 

rem Trying to replace the substring 'GPML' with '' in CONVERTER_NAME
rem If the string is changed, it means that CONVERTER_NAME contains 'GPML'
if not x%CONVERTER_NAME:GPML=% == x%CONVERTER_NAME% (
  rem echo It contains GPML
  set PATHVISIO_CONVERTER="yes"
)

if %PATHVISIO_CONVERTER% == "no" (
  set CLASSPATH=%CONVERTER_HOME%.;%CONVERTER_HOME%lib\*;%CONVERTER_HOME%lib\paxtools-4.2\*
) else (
  set CLASSPATH=%CONVERTER_HOME%.;%CONVERTER_HOME%lib\*;%CONVERTER_HOME%lib\pathvisio\*
  set MODEL_NAME=BioPaxOldModel
  rem echo setting BioPaxOldModel as model
)

rem echo "classpath= %CLASSPATH% "
rem echo %~dp0

java -Dmiriam.xml.export=miriam.xml  -classpath %CLASSPATH% org.sbfc.converter.Converter %MODEL_NAME% %CONVERTER_NAME% %file%

echo.

pause