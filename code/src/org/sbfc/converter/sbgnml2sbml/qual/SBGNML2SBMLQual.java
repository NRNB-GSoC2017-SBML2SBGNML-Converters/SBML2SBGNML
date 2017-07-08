package org.sbfc.converter.sbgnml2sbml.qual;

import java.io.FileNotFoundException;

import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLUtil;
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBML_GSOC2017;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;

public class SBGNML2SBMLQual {
	public static void main(String[] args) throws FileNotFoundException {
		String sbgnFileNameInput;
		String sbmlFileNameOutput;
		String workingDirectory;
		
		Sbgn sbgnObject = null;
		Map map;
		SBGNML2SBML_GSOC2017 converter;		
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLQual <SBGNML filename>. ");
			return;
		}		
		
		// Read a .sbgn file
		workingDirectory = System.getProperty("user.dir");
		sbgnFileNameInput = args[0];
		sbgnFileNameInput = workingDirectory + sbgnFileNameInput;			
		sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
				
		sbgnObject = SBGNML2SBMLUtil.readSbgnFile(sbgnFileNameInput);

		map = sbgnObject.getMap();	
		// optional
		//SBGNML2SBMLUtil.debugSbgnObject(map);
		
		// Create a new converter
		converter = new SBGNML2SBML_GSOC2017(map);
		// Load a template file containing predefined RenderInformation
		converter.storeTemplateRenderInformation();
		// Convert the file
		converter.convertToSBML();
				
		// Write converted SBML file
		SBGNML2SBMLUtil.writeSbmlFile(sbmlFileNameOutput, converter.sOutput.model);
	}
}
