@echo off

set MODEL_HOME=%~dp0

set CLASSPATH=%MODEL_HOME%.;%MODEL_HOME%lib\*;%CONVERTER_HOME%lib\paxtools-4.2\*

rem echo "classpath= %CLASSPATH% "
rem echo %~dp0

java -Dmiriam.xml.export=miriam.xml  -classpath %CLASSPATH% org.util.classlist.ModelSearcher

echo. 

pause
