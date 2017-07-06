package org.sbfc.converter.sbml2sbgnml.qual;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.sbfc.converter.sbml2sbgnml.SBML2SBGNMLUtil;
import org.sbfc.converter.sbml2sbgnml.SBML2SBGNML_GSOC2017;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;
import org.xml.sax.SAXException;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AbstractMathContainer;

public class SBMLQualApplication {
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
				
		String sbmlFileNameInput;
		String sbgnFileNameOutput;
		SBMLDocument sbmlDocument;
		SBML2SBGNML_GSOC2017 sbml2sbgnml;
		Sbgn sbgnObject;
		File file;
		
		if (args.length < 1 || args.length > 3) {
			// todo: change
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.qual.SBMLQualApplication <SBML filename>. ");
		}

		String workingDirectory = System.getProperty("user.dir");

		sbmlFileNameInput = args[0];
		sbmlFileNameInput = workingDirectory + sbmlFileNameInput;	
		sbgnFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
		
		
		sbmlDocument = SBML2SBGNMLUtil.getSBMLDocument(sbmlFileNameInput);
		if (sbmlDocument == null) {
			throw new FileNotFoundException("The SBMLDocument is null");
		}
			
		sbml2sbgnml = new SBML2SBGNML_GSOC2017(sbmlDocument);
		// visualize JTree
		try {		
			sbml2sbgnml.sUtil.visualizeJTree(sbmlDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}		
//		
//		sbgnObject = sbml2sbgnml.convertToSBGNML(sbmlDocument);	
//		
//		file = new File(sbgnFileNameOutput);
//		try {
//			SbgnUtil.writeToFile(sbgnObject, file);
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
//		
		Model model;
		QualModelPlugin qualModelPlugin;
		ListOf<QualitativeSpecies> listOfQualitativeSpecies = null;
		ListOf<Compartment> listOfCompartments = null;
		ListOf<Transition> listOfTransitions = null;
		
		model = sbml2sbgnml.sOutput.getModel();
		if (model.isSetPlugin("qual")){
			//System.out.println("isSetPlugin(qual) true");
			qualModelPlugin = (QualModelPlugin) model.getPlugin("qual");
			//System.out.println("getNumQualitativeSpecies " + qualModelPlugin.getNumQualitativeSpecies());
			//System.out.println("getNumTransitions " + qualModelPlugin.getNumTransitions());
			//System.out.println("getNumCompartments " + model.getNumCompartments());
			
			listOfQualitativeSpecies = qualModelPlugin.getListOfQualitativeSpecies();
			listOfTransitions = qualModelPlugin.getListOfTransitions();
			listOfCompartments = model.getListOfCompartments();
		}
		
		String id; 
		String name;
		String compartmentForSpecies;
		boolean constant;
		int initialLevel;
		int maxLevel;	
		
		for (QualitativeSpecies qualitativeSpecies: listOfQualitativeSpecies){
			id = qualitativeSpecies.getId();
			name = qualitativeSpecies.getName();
			compartmentForSpecies = qualitativeSpecies.getCompartment();
			constant = qualitativeSpecies.getConstant();
			//initialLevel = qualitativeSpecies.getInitialLevel();
			maxLevel = qualitativeSpecies.getMaxLevel();
			
			System.out.format("QualitativeSpecies id=%s name=%s compartmentForSpecies=%s "
					+ "constant=%s maxLevel=%s \n\n", 
					id, name, compartmentForSpecies,
					constant ? "true" : "false",
					Integer.toString(maxLevel));
		}
		System.out.println("-----");
		
		ListOf<Input> listOfInputs = null;
		ListOf<Output> listOfOutputs = null;
		ListOf<FunctionTerm> listOfFunctionTerms = null;
		ASTNode math;
		String mathString;
		
		Sign sign;
		String qualitativeSpecies;
		InputTransitionEffect inputTransitionEffect;
		OutputTransitionEffect outputTransitionEffect;
		int thresholdLevel;
		int outputLevel;
		int resultLevel;
		
		for (Transition transition: listOfTransitions){
			id = transition.getId();
			name = transition.getName();
			listOfInputs = transition.getListOfInputs();
			listOfOutputs = transition.getListOfOutputs();
			listOfFunctionTerms = transition.getListOfFunctionTerms();
			
			System.out.format("Transition id=%s name=%s \n", id, name);
						
			for (Input input: listOfInputs){
				id = input.getId();
				name = input.getName();
				sign = input.getSign();
				qualitativeSpecies = input.getQualitativeSpecies();
				inputTransitionEffect = input.getTransitionEffect();
				thresholdLevel = input.getThresholdLevel();
				
				String signString;
				if (sign.equals(Sign.positive)){signString = "positive";}
				else if (sign.equals(Sign.negative)){signString = "negative";} 
				else if (sign.equals(Sign.dual)){signString = "dual";} 
				else {signString = "unknown";}
				
				System.out.format("    Input id=%s name=%s sign=%s qualitativeSpecies=%s "
						+ "inputTransitionEffect=%s thresholdLevel=%s \n",
						id, name, signString, qualitativeSpecies, 
						inputTransitionEffect.equals(InputTransitionEffect.consumption) ? "consumption" : "none",
						Integer.toString(thresholdLevel));
			}
			System.out.println();
			
			for (Output output: listOfOutputs){
				id = output.getId();
				name = output.getName();
				qualitativeSpecies = output.getQualitativeSpecies();
				outputTransitionEffect = output.getTransitionEffect();
				//outputLevel = output.getOutputLevel();
				
				System.out.format("    Output id=%s name=%s qualitativeSpecies=%s "
						+ "outputTransitionEffect=%s \n",
						id, name, qualitativeSpecies, 
						outputTransitionEffect.equals(OutputTransitionEffect.assignmentLevel) ? "assignmentLevel" : "production");
			}
			System.out.println();
			
			for (FunctionTerm functionTerm: listOfFunctionTerms){
				resultLevel = functionTerm.getResultLevel();
				
				if (functionTerm.isDefaultTerm()){
					mathString = "";
				} else {
					math = functionTerm.getMath();
					//functionTerm.getMathMLString();
					mathString = math.toMathML();
				}
				
				System.out.format("    FunctionTerm resultLevel=%s isDefaultTerm=%s mathString=%s \n",
						resultLevel, 
						functionTerm.isDefaultTerm() ? "DefaultTerm" : "FunctionTerm", 
						mathString);
			}
			System.out.println();
			System.out.println("-----");
		}
		
		for (Compartment compartment: listOfCompartments){
			id = compartment.getId();
			name = compartment.getName();
			// etc.
			
			System.out.format("Compartment id=%s name=%s \n\n", id, name);
					
		}

	}	
}
