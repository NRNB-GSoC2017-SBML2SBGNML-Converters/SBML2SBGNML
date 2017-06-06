# SBML-SBGNML Converters
## How to run the converters
`git clone https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML.git`  
Load the project in an IDE such as Eclipse. Add all JAR files in `sbfc/lib`, `sbfc/lib/pathviso to Java Build Path`, but not `sbfc/lib/paxtools-4.2`.  
Go to the [sbml2sbgnml](code/src/org/sbfc/converter/sbml2sbgnml) code.  
In Eclipse, specify the input file path (relative path) in Run Configurations Arguments.  
Run the `main()` method.  
The converted file will be located in the same directory as the input file.
