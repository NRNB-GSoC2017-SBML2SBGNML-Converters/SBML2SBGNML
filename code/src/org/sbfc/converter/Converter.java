/*
 * $Id: Converter.java 619 2016-02-04 16:48:40Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/Converter.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2016 jointly by the following organizations:
 * 1. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 2. The Babraham Institute, Cambridge, UK
 * 3. Department of Bioinformatics, BiGCaT, Maastricht University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as 
 * <http://sbfc.sf.net/mediawiki/index.php/License>.
 * 
 * ==========================================================================
 * 
 */

package org.sbfc.converter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sbfc.converter.models.GeneralModel;
import org.util.classlist.ConverterSearcher;

/**
 * This class calls the converter for the specified converter
 * type and writes out the result.
 * 
 * @author Martina Kutmon
 * @author Nicolas Rodriguez
 * @author Piero Dalle Pezze
 */
public class Converter {

  static final Logger logger = Logger.getLogger(Converter.class);
  
  public static ArrayList<String> converterFullNames = ConverterSearcher.getConverterFullNameList();
  public static String converterPackage = ConverterSearcher.getConverterSuperPackage();

  
    /**
     * Search for the converter package associated to the converter type
     * 
     * @param converterType the converter to use
     * @param converterOptions the converters options
     * @return the converter package and type
     */
    private static ConverterData getPackageClassPath(String converterType, Map<String, String> converterOptions) {
   	
		boolean found = false;
		
		String levelAndVersion = null;
		
		if (converterType.contains("SBML2SBML") && converterType.length() > 10) {
		  levelAndVersion = converterType.substring(10);
		  converterType = "SBML2SBML";
		}
		
		for(int i=0; i < converterFullNames.size() && !found; i++) {
			String converterFullName = converterFullNames.get(i);
			
			// System.out.println("getPackageClassPath - converterFullName = " + converterFullName);
			
			if(converterFullName.contains(converterType)) {
				converterPackage = converterFullName.substring(0, converterFullName.lastIndexOf("."));
				
				// Now set the options for specific converters
				if (converterType.contains("Dot")) {
					converterOptions.put("export", "png svg");
					
				} else if (levelAndVersion != null && levelAndVersion.trim().length() > 0) 
				{
				  System.out.println("Level and version = " + levelAndVersion);					
				  if (levelAndVersion.equals("L3V1")) {
				    converterOptions.put("sbml.target.level", "3");
				    converterOptions.put("sbml.target.version", "1");
				  } else if (levelAndVersion.equals("L2V4")) {
				    converterOptions.put("sbml.target.level", "2");
				    converterOptions.put("sbml.target.version", "4");
				  } else if (levelAndVersion.equals("L2V1")) {
				    converterOptions.put("sbml.target.level", "2");
				    converterOptions.put("sbml.target.version", "1");
				  } else if (levelAndVersion.equals("L1V2")) {
				    converterOptions.put("sbml.target.level", "1");
				    converterOptions.put("sbml.target.version", "2");
				  }  
				} 
				
				found = true;
			}
		}
		
        logger.debug("getPackageClassPath - converter type: "  + converterType);
        logger.debug("getPackageClassPath - convert package: "  + converterPackage);		

		return new ConverterData(converterType, converterPackage);
		
    }
    
  
	public Converter() {
		super();
	}

	/**
	 * Converts the input file using the provided model and converter classes.
	 * 
	 * @param inputModelType the className of the input model
	 * @param converterType the className of the converter to use
	 * @param inputFileName the path to the input file
	 * @return the path of the output file
	 */
	public static String convertFromFile(String inputModelType, String converterType, String inputFileName) {

	    return convertFromFile(inputModelType, converterType, inputFileName, null);

	  }
    
	/**
	 * Converts the input file using the provided model and converter classes.
	 * 
     * @param inputModelType the className of the input model
     * @param converterType the className of the converter to use
     * @param inputFileName the path to the input file
	 * @param outputFileName the path of the output file (create one automatically if null is passed)
     * @return the path of the output file
	 */
	public static String convertFromFile(String inputModelType, String converterType, String inputFileName, String outputFileName) {

		//Let's instantiate the proper Converter
		
		Map<String, String> converterOptions = new HashMap<String, String>();
		converterOptions.put("save.result", "yes");
		
		// Retrieve the full package class path
        ConverterData data = getPackageClassPath(converterType, converterOptions);
		
        converterType = data.getConverterType();
        converterPackage = data.getConverterPackage();
        
		//Instantiating the converter
		GeneralConverter converter=null;
		try {
			converter = (GeneralConverter) Class.forName(converterPackage+"."+converterType).newInstance();
			converter.setOptions(converterOptions);
		} catch (Exception e1) {
			//Creating an error report
			int pos = inputFileName.lastIndexOf(".");
			FileOutputStream fconv;
			try {
				// We can not have the result file extension at this point because
				//There's been an error while instantiating the converter itself... 
				//We create a file InputModel.errorLog
				fconv = new FileOutputStream (inputFileName.substring(0,pos)+".errorLog");
				PrintStream convprint = new PrintStream(fconv);
				convprint.println ("######################################################\n" +
						"The converter "+converterType+" you asked for can not be found...\n");
				e1.printStackTrace(convprint);
				
				logger.info("The converter "+converterType+" you asked for can not be found...\n");
				e1.printStackTrace();
				
				fconv.close();
				System.exit(1);
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			};
		}
		try {

			String modelPackage = "org.sbfc.converter.models";
			//Instantiating the inputModel
			GeneralModel inputModel = (GeneralModel) Class.forName(modelPackage+"."+inputModelType).newInstance();
			inputModel.setModelFromFile(inputFileName);
			//Converting the Model
			GeneralModel result = converter.convert(inputModel);
			
			if (converterOptions.get("save.result").equals("yes")) {
			  //Creating the OutputFile			
			  
			  if (outputFileName == null || outputFileName.trim().length() == 0) {
			    int pos = inputFileName.lastIndexOf(".");
			    outputFileName = inputFileName.substring(0,pos) + converter.getResultExtension();
			  }
			  result.modelToFile(outputFileName);
			  return outputFileName;
			}
			
		} catch (Exception e) {
			//Replacing the result file by an error report
			int pos = inputFileName.lastIndexOf(".");
			FileOutputStream fconv;
			try {
			  String extension = ".err";
			  
			  if (converter != null) {
			    extension = converter.getResultExtension();
			  }
			  
				fconv = new FileOutputStream (inputFileName.substring(0,pos) + extension);
				PrintStream convprint = new PrintStream(fconv);
				convprint.println ("######################################################\n" +
						"#Something went wrong during the conversion !\n" +
						"#Try to validate your input file before converting it\n\n");
						//+ "Input file: " + inputFileName.substring(0,pos) + "\n\n");
				e.printStackTrace(convprint);

				if (logger.isDebugEnabled()) {
				  logger.debug("#Something went wrong during the conversion !\n");
                  e.printStackTrace();
                }
				
				fconv.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e.printStackTrace();
			};
		}
		
		return null;
	}



	/**
	 * Converts the input String using the provided model and converter classes.
	 * 
     * @param inputModelType the className of the input model
     * @param converterType the className of the converter to use
	 * @param modelInputString the content of the input model as a String
	 * @return a String representing the result of the conversion
	 */
	public static String convertFromString(String inputModelType, String converterType, String modelInputString) {

		String retModel = null;
		//Let's instantiate the proper Converter

		try {
		  
			Map<String, String> converterOptions = new HashMap<String, String>();
			converterOptions.put("save.result", "yes");			

			// Retrieve the full package class path
			ConverterData data = getPackageClassPath(converterType, converterOptions);
	        
	        converterType = data.getConverterType();
	        converterPackage = data.getConverterPackage();	
			
			//Instantiating the converter
			GeneralConverter converter = (GeneralConverter) Class.forName(converterPackage+"."+converterType).newInstance();

			String modelPackage = "org.sbfc.converter.models";
			//Instantiating the inputModel
			GeneralModel inputModel = (GeneralModel) Class.forName(modelPackage+"."+inputModelType).newInstance();
			inputModel.setModelFromString(modelInputString);
			//Converting the Model
			GeneralModel result = converter.convert(inputModel);

			retModel = result.modelToString();

		}catch (Exception e) {
			e.printStackTrace();
		}
		return retModel;
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		if(args.length < 3) {
			// TODO : Allow to do several conversion at the same time ?
			
			System.out.println("Wrong number of arguments :\n"+
					"Usage: Converter.java [InputModelClass] [ConverterClass] [ModelFile]");
			
		}
		else {
			convertFromFile(args[0], args[1], args[2]);
			System.exit(0);
		}
	}

}

/* used to return the correct converterType and converterPackage */
class ConverterData {
    private String converterType = null;
    private String converterPackage = null;
    public ConverterData(String converterType, String converterPackage) {
        this.converterType = converterType;
        this.converterPackage = converterPackage;
    }
    public String getConverterType() { 
        return converterType;
    }
    public String getConverterPackage() {
        return converterPackage;
    }
}


