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

/**
 * This class is incomplete, to convert SBML qual to SBGN, please use the SBML2SBGNML_GSOC2017, which is able to convert all the layout
 * to Sbgn glyphs. However, the clazz of the converted glyphs may not be correct if the target language is SBGN AF. For example, in 
 * AF, there are many types of modulation arcs (Positive, Negative, Unknown influence), but SBML2SBGNML_GSOC2017 will set all of these 
 * arcs to "modulation"
 * @author haoran
 *
 */
public class SBML2SBGNMLQual {
	
	public static void toStringDebug(SBML2SBGNML_GSOC2017 sbml2sbgnml){

		ListOf<QualitativeSpecies> listOfQualitativeSpecies = sbml2sbgnml.sOutput.listOfQualitativeSpecies;
		ListOf<Compartment> listOfCompartments = sbml2sbgnml.sOutput.listOfCompartments;
		ListOf<Transition> listOfTransitions = sbml2sbgnml.sOutput.listOfTransitions;
	
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
		ASTNode math = null;
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
					
					System.out.println("====getParent====" + math.getParent().toString());
					System.out.println("====getChildren====" + math.getChildren().toString());
					System.out.println("====getType====" + math.getType().toString());
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
			
			System.out.format("Compartment id=%s name=%s \n\n", id, name);
					
		}		
	}
		
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
				
		String sbmlFileNameInput;
		String sbgnFileNameOutput;
		SBMLDocument sbmlDocument;
		SBML2SBGNML_GSOC2017 sbml2sbgnml;
		Sbgn sbgnObject;
		File file;
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.qual.SBML2SBGNMLQual <SBML filename>. ");
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

		toStringDebug(sbml2sbgnml);
		
	}	
}
