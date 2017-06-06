@echo off

set CONVERTER_HOME=%~dp0

set CLASSPATH=%CONVERTER_HOME%.;%CONVERTER_HOME%lib\*;%CONVERTER_HOME%lib\paxtools-4.2\*

rem echo "classpath= %CLASSPATH% "
rem echo %~dp0

java -Dmiriam.xml.export=miriam.xml  -classpath %CLASSPATH% org.sbfc.converter.ConverterGUI

echo.

pause