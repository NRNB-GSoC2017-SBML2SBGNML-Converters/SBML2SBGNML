/*
 * $Id: SBML2Octave.java 456 2015-08-20 13:29:45Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2octave/SBML2Octave.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2015 jointly by the following organizations:
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
package org.sbfc.converter.sbml2octave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.OctaveModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Trigger;



/**
 * Convert an SBML file into an Octave file.
 *  
 * @author Nicolas Rodriguez
 * @author JB Pettit
 * @author Piero Dalle Pezze
 * 
 * @version 1.2
 *  
 */
@SuppressWarnings("deprecation")
public class SBML2Octave extends GeneralConverter{


	private HashMap<Species, String> speciesFluxMap = new HashMap<Species, String>();
	private HashMap<Species, Boolean> isHasSubstanceUnits = new HashMap<Species, Boolean>();
	Vector<String> mathOperators;
	ListOf<SpeciesReference> products;
	ListOf<SpeciesReference> reactants;
	ListOf<ModifierSpeciesReference> modifiers;
	// We count parameters from:
	// - compartment (@constant!=false)
	// - global parameters (@constant!=false)
	// - species (@constant=true)
	// - local parameters
	private int nbParameters = 0;
	
	private int nbEquaDiff = 0;

	public final static Pattern idPattern = Pattern.compile("(_|[a-z]|[A-Z])(_|[a-z]|[A-Z]|[0-9])*");
	
	public final static Pattern idOctavePattern = Pattern.compile("(_|[a-z]|[A-Z])(_|[a-z]|[A-Z]|[0-9])*\\((_|[a-z]|[A-Z]|[0-9]|,)*\\)");

	public final static Pattern plusMinusPattern = Pattern.compile("(\\+\\ \\-)|(\\+\\-)");

	Model sbmlModel;
	
	ArrayList<EquaDiff> xdot = new ArrayList<EquaDiff>();

	/**
	 * <b>Constructor SBML2Octave.</b><br/> Main method of the biological model
	 * export from <a href="http://sbml.org/"><b>SBML</b></a> (Systems Biology
	 * Markup Language) to Octave</a>.

	 * 
	 * jSBML is used to read and check the SBML file provided.<br/>
	 * 
	 *            Path of the SBML file to export
	 */
	public SBML2Octave() {
		super();

	}

	// The following protected methods differ between Octave and Matlab. 
	// These are overridden in the converter SBML2Matlab 

	protected String headerString() {
		return  "% This file works with OCTAVE and is automatically generated with \n" +
				"% the System Biology Format Converter (http://sbfc.sourceforge.net/)\n" +
				"% from an SBML file.\n" +
				"% To run this file with Matlab you must edit the comments providing\n" +
				"% the definition of the ode solver and the signature for the \n" +
				"% xdot function.\n" +
				// 	TODO : put the version used
				"%\n" +
				"% The conversion system has the following limitations:\n" +
				"%  - You may have to re order some reactions and Assignment Rules definition\n" +
				"%  - Delays are not taken into account\n" +
				"%  - You should change the lsode parameters (start, end, steps) to get better results\n%\n\n";
		// This seems not to be any longer required by Octave
//				"%\n% NOTE for Octave users ONLY:\n" +
//		        "% To prevent Octave from thinking that this is a function file, \n" + 
//			    "% the following line should be uncommented (instead comment it if using Matlab): \n" +
//				"%1;\n\n";
	}
	
	protected String xdotFunctionSignature() {
		return "% Depending on whether you are using Octave or Matlab,\n" +
			   "% you should comment / uncomment one of the following blocks.\n" +
			   "% This should also be done for the definition of the function f below.\n" +
	           "% Start Matlab code\n" +
			   "%function xdot=f(t,x)\n" +
			   "% End Matlab code\n\n" + 

			   "% Start Octave code\n" + 
			   "function xdot=f(x,t)\n" +
			   "% End Octave code\n\n";
	}
	
	protected String odeSolverCode() {
		return  "% Depending on whether you are using Octave or Matlab,\n" + 
				"% you should comment / uncomment one of the following blocks.\n" +
				"% This should also be done for the definition of the function f below.\n" +
				"% Start Matlab code\n" +
				"%\ttspan=[0:0.01:100];\n" +
				"%\topts = odeset('AbsTol',1e-3);\n" +
				"%\t[t,x]=ode23tb(@f,tspan,x0,opts);\n" +
				"% End Matlab code\n\n" +

				"% Start Octave code\n" +
				"\tt=linspace(0,100,100);\n" +
				"\tx=lsode('f',x0,t);\n" +
				"% End Octave code\n\n";
	}
	
	
	private String createHeader() {
		String octaveModel = headerString();

		String modelName = sbmlModel.getName();

		if (modelName == null || modelName.trim().length() == 0) {
			modelName = sbmlModel.getId();
		}

		octaveModel += "%\n% Model name = " + modelName + "\n%\n";

		// 	put all the bqmodel references as comment 
		for (CVTerm cvTerm : sbmlModel.getCVTerms()) {
			if (cvTerm.isModelQualifier()) {
				for (String uri : cvTerm.getResources()) {
					octaveModel += "% " + cvTerm.getModelQualifierType().getElementNameEquivalent() + " " + uri + "\n";
				}
			}
		}
		octaveModel +="%\n";

		return octaveModel;
	}

	/**
	 * <b>Method of the export.</b><br/>
	 * 
	 * Use libSBML to read the SBML model object create before.<br/>
	 * 
	 * @param sbmlModel
	 *            SBML object create with the path provide to the constructor
	 * @throws SBMLException
	 */
	public OctaveModel octaveExport(SBMLModel sbfcSBMLModel) throws SBMLException {

		System.out.println("SBML2Octave : Export in progress...");

		sbmlModel = sbfcSBMLModel.getModel();

		if(sbmlModel == null) {
			throw new SBMLException("SBML2Octave: Input file is not a regular SBML file.");
		}

		String octaveModel = createHeader();
		
		try {
		
		// SBML elements that we can export in Octave
		ListOf<FunctionDefinition> functiondefinitions = sbmlModel.getListOfFunctionDefinitions();
		ListOf<Compartment> compartments = sbmlModel.getListOfCompartments();
		ListOf<Species> listOfSpecies = sbmlModel.getListOfSpecies();
		ListOf<Parameter> parameters = sbmlModel.getListOfParameters();
		ListOf<Rule> rules = sbmlModel.getListOfRules();
		ListOf<Reaction> reactions = sbmlModel.getListOfReactions();
		ListOf<Event> events = sbmlModel.getListOfEvents();
		
		
		mathOperators = new Vector<String>();

		mathOperators.add("exp");
		mathOperators.add("pow");
		mathOperators.add("root");
		mathOperators.add("sqrt");
		mathOperators.add("eq");
		mathOperators.add("neq");
		mathOperators.add("gt");
		mathOperators.add("geq");
		mathOperators.add("lt");
		mathOperators.add("leq");
		mathOperators.add("sin");
		mathOperators.add("cos");
		mathOperators.add("tan");
		mathOperators.add("atan2");
		mathOperators.add("asin");
		mathOperators.add("acos");
		mathOperators.add("atan");
		mathOperators.add("sinh");
		mathOperators.add("cosh");
		mathOperators.add("tanh");
		mathOperators.add("log");
		mathOperators.add("log10");
		mathOperators.add("ln");
		mathOperators.add("abs");
		mathOperators.add("pi");
		mathOperators.add("time");
		mathOperators.add("Time");
		mathOperators.add("delay");
		mathOperators.add("ceil");
		mathOperators.add("flr");
		
		// because of the e-notation
		mathOperators.add("e");
		mathOperators.add("E");
		
		// For the time variable (csymbol time)
		mathOperators.add("t");

		
		// To make compatible with Matlab, the code invoking the ode solver must be at the beginning 
		// and inside a function. As the xdots are computed in the following block, it becomes 
		// quite tricky to change this parsing at this stage. Therefore, we stick a bookmark string 
		// which will be replaced with the ode solver invokation later.
		String mainBookmark = "MAIN_FUNCTION_BOOKMARK";
		octaveModel += "\n\n" + mainBookmark + "\n\n";
		
		//Starting the xdot function
		octaveModel += xdotFunctionSignature();
		
		
		
		// TODO : species not affected by rules or reaction should be put as "par speciesId"
		// TODO : check sign produce for the event !!

		// TODO : SBML level 2 version 3 : InitialAssignment (first biomodel with one is out !!!)...
		
		// TODO : units check consistency		
		
		// Compartments
		for (Compartment compartment : compartments) {

			buildIdMap(compartment,"compartment_"+compartment.getId());

			if (compartment.isConstant()) {
				nbParameters++;
				octaveModel +=printConstantCompartment(compartment);
			} else {
				octaveModel +=printCompartment(compartment);
			}

		}
		
		
		// Species 
		// id map is needed here for the rules mathML replaceId
		for (Species species : listOfSpecies) {
			
			if(species.isConstant()) {
				buildIdMap(species,"const_species_"+species.getId());
			}
			else {
				nbEquaDiff++;
			
				buildIdMap(species,"x("+nbEquaDiff+")");
			}

			if (!species.isHasOnlySubstanceUnits()) {
				if (species.getCompartmentInstance().getSpatialDimensions() > 0) {
					isHasSubstanceUnits.put(species, false);
				}
				if (species.getCompartmentInstance().getSpatialDimensions() == 0) {
					isHasSubstanceUnits.put(species, true);
				}
			} else if (species.isHasOnlySubstanceUnits()) {
				isHasSubstanceUnits.put(species, true);
			}

		}

		
		
		// Global Parameters
		for (Parameter parameter : parameters) {
			buildIdMap(parameter,"global_par_"+parameter.getId());
			if (parameter.isConstant()) {
				nbParameters++;
				octaveModel +=printConstantParameter(parameter);
			} else {

				
				octaveModel +=printParameter(parameter);
			}

		}

		// Reactions Id Map : We need to build the id map for reactions before taking care of the rules
		// in case there are any reaction id inside the rules math formula.
		for (Reaction reaction : reactions) {

			buildIdMap(reaction,"reaction_"+reaction.getId());
			
		}


		// Rules
		for (Rule rule : rules) {

			String rulemath = null;
			
			if (rule instanceof AlgebraicRule) {
				rulemath = printAlgebraicRule(rule);
			} else { 
				rulemath = printRule(rule);
			}
			
			if (rulemath == null) {
				System.out.println("Inconsistent mathML operators in Rules");
				// TODO : write a message instead : System.exit(3);
			} else {
				octaveModel +=rulemath;
			}


		}

		// Reactions
		for (Reaction reaction : reactions) {
			
			products = reaction.getListOfProducts();
			reactants = reaction.getListOfReactants();
			modifiers = reaction.getListOfModifiers();

			String reactionmath = printReaction(reaction);

			
			if (reactionmath == null) {
				System.out.println("Inconsistent mathML operators in Reaction");
				// TODO : write a message instead : System.exit(3);
			} else {
				octaveModel +=reactionmath;
			}
			


			for (SpeciesReference reactant : reactants) {

				Species species = reactant.getSpeciesInstance();
				String speciesFlux = speciesFluxMap.get(species);


				String stoichiometryStr = "1";
				double stoichiometry = reactant.getStoichiometry();
				StoichiometryMath stoichiometryMath = reactant.getStoichiometryMath();

				if (stoichiometryMath != null
						&& stoichiometryMath.isSetMath()) {
					stoichiometryStr = stoichiometryMath.getMath().toFormula();
				} else if (stoichiometry != 0) {
					stoichiometryStr = Double.toString(stoichiometry);
				}

				if (speciesFlux == null) {
					speciesFlux = new String();
				}
				if (speciesFlux.length() != 0) {
					speciesFlux += " + ";
				}
				speciesFlux += "(-" + stoichiometryStr + " * "
					+ OctaveID.getOctaveId(reaction.getId()) + ")";
				
				speciesFluxMap.put(species, speciesFlux);
				
				System.out.println("Reactant flux map : id = " + species.getId());
				
			}
						
			for (SpeciesReference product : products) {

				Species species = product.getSpeciesInstance();
				String speciesFlux = speciesFluxMap.get(species);

				String stoichiometryStr = "1";
				double stoichiometry = product.getStoichiometry();
				StoichiometryMath stoichiometryMath = product.getStoichiometryMath();

				if (stoichiometryMath != null
						&& stoichiometryMath.isSetMath()) {
					stoichiometryStr = stoichiometryMath.getMath().toFormula();
				} else if (stoichiometry != 0) {
					stoichiometryStr = Double.toString(stoichiometry);
				}

				if (speciesFlux == null) {
					speciesFlux = new String();
				}
				if (speciesFlux.length() != 0) {
					speciesFlux += " + ";
				}
				speciesFlux += "( " + stoichiometryStr + " * "
					+ OctaveID.getOctaveId(reaction.getId()) + ")";
				speciesFluxMap.put(species, speciesFlux);
			}
			

			

		}

		// Species
		for (Species species : listOfSpecies) {

			if (species.isConstant()) {
				nbParameters++;
				octaveModel +=printConstantSpecies(species);
			} else {
				String speciesmath = printSpecies(species);
				
				// TODO : write something in the Octave file
				if (speciesmath == null) {
					System.out.println("Inconsistent mathML operators ");
					// TODO : write a message instead : System.exit(3);
				} else {
				}
			}
		}
		
		
		// Events
		int i = 1;
		for (Event event : events) {

			String eventId = event.getId();

			System.out.println("Debug : Event id = " + eventId + ", " + event.getId());
			
			buildIdMap(event,"event_"+eventId);
			
			i++;
			
			// TODO : take care of Delay or write a warning with the delay formula in the Octave file and log file.
			// Delay delay = event.getDelay();
			Trigger trigger = event.getTrigger();
			
			if (trigger == null || !trigger.isSetMath()) {
				octaveModel += "%WARNING : No trigger defined for event id="+eventId+"; event ignored\n\n";
			}
			else {
				
	
				String infixTrigger = trigger.getMath().toFormula();
	
				ASTNode astTrigger = trigger.getMath();
				
				String infixASTTrigger = astTrigger.toFormula();
				
				System.out.println("Debug : Event trigger = " + infixTrigger);
				System.out.println("Debug : Event trigger = " + infixASTTrigger);
	
				octaveModel += "\n%Event: id="+eventId+"\n";
				octaveModel +="\t"+OctaveID.getOctaveId(eventId)+"="+ replaceIdInsideFormula(infixASTTrigger)+";\n\n" +
						"\tif("+OctaveID.getOctaveId(eventId)+") \n";
			
				int j = 1;
				int eventAssignmentSize = event.getNumEventAssignments();
				for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
					octaveModel +="\t\t"+OctaveID.getOctaveId(eventAssignment.getVariable()) + "=" + replaceIdInsideFormula(eventAssignment.getMath().toFormula());
					
					if (j < eventAssignmentSize) {
						octaveModel +=";\n";
					} else {
						octaveModel +=";\n\tend\n";
					}
					
					j++;
				}
			}
		}		
		

		octaveModel += "\n\txdot=zeros("+xdot.size()+",1);\n";
		
		//Printing differential equations
		for(EquaDiff equa: xdot) {
			octaveModel += "\t"+equa.getComment();
			octaveModel += "\txdot("+equa.getEquaNumber()+") = "+equa.getEqua()+";\n";
		}
		
		octaveModel += "end\n\n";
		
		
		
		
		// This will be put ahead.
		StringBuilder main = new StringBuilder();
		//Starting the xdot function
		main.append("function main()\n");				
		//Printing initial conditions and launching integration
		main.append("%Initial conditions vector\n");
		
		main.append("\tx0=zeros("+xdot.size()+",1);\n");
		for(EquaDiff equa: xdot) {
			main.append("\tx0("+equa.getEquaNumber()+") = "+equa.getInit()+";\n");
		}
		
		main.append("\n\n" + odeSolverCode() + "\n"); 
		
		main.append("\tplot(t,x);\n");
		
		main.append("end\n\n");	
		octaveModel = octaveModel.replaceFirst(mainBookmark, main.toString());
		
		
		// Function Definition
		for (FunctionDefinition functiondefinition : functiondefinitions) {
			buildIdMap(functiondefinition, functiondefinition.getId());

			String funcdefin = printFunctionDefinition(functiondefinition);
			if (funcdefin == null) {
				System.out.println("Inconsistent mathML operators in Function Definition");
				// TODO : write a message instead : System.exit(2);
			} else {
				octaveModel +=funcdefin;
			}

		}
		
		// Leave this at the end. 
		octaveModel +="% adding few functions representing operators used in SBML but not present directly \n";
		octaveModel +="% in either matlab or octave. \n";
		octaveModel +="function z=pow(x,y),z=x^y;end\n" + "function z=root(x,y),z=y^(1/x);end\n";
		octaveModel +="function z = piecewise(varargin)\n" 
		+"\tnumArgs = nargin;\n" 
		+"\tresult = 0;\n" 
		+"\tfoundResult = 0;\n" 
		+"\tfor k=1:2: numArgs-1\n" 
			+"\t\tif varargin{k+1} == 1\n" 
				+"\t\t\tresult = varargin{k};\n" 
				+"\t\t\tfoundResult = 1;\n" 
				+"\t\t\tbreak;\n" 
			+"\t\tend\n" 
		+"\tend\n" 
		+"\tif foundResult == 0\n" 
			+"\t\tresult = varargin{numArgs};\n" 
		+"\tend\n" 
		+"\tz = result;\n" +
		"end\n\n";
		
			
		} catch (AssertionError err) {
			octaveModel = createHeader();
			octaveModel += "\n%\n% " + err.getMessage() + "\n%\n";
		}

		
		OctaveModel octaveReturnModel = new OctaveModel();
		try {
			octaveReturnModel.setModelFromString(octaveModel);
		} catch (ReadModelException e) {
			e.printStackTrace();
			return null;
		}
		
		return octaveReturnModel;
		

	}

	/**
	 * Function Definition(mathML) from SBML file is converted to infix notation by libSBML
	 *  and checked for supporting math functions.
	 *  Infix notation is passed as a Equation in Octave
	 *  
	 *  SBML: lambda(x,y,formula)
	 *  Octave : function z=function_name(x,y), z=formula(x,y);end
	 *  @param functiondefinition : mathML from SBML model
	 * 	@return : corresponding Supported equation for Octave 
	 */

	
	private String printFunctionDefinition(FunctionDefinition functiondefinition) {

		String inputformula=null;
		try {
			inputformula = functiondefinition.getMath().toFormula();
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		inputformula = inputformula.replace("lambda(", "");

		ArrayList<String> tempal = new ArrayList<String>();

		StringTokenizer stcomma = new StringTokenizer(inputformula, ",");
		StringBuilder funcDef = new StringBuilder();
		StringBuilder formula = new StringBuilder();
		formula.append("(");
		String functionname;
		int count = 0;
		int stCommaNb = stcomma.countTokens();

		/*
		 * uses ",  as String tokenizer " if single then a variable if more than
		 * one, check for," ", ), / and add it to formula
		 */
		while (stcomma.hasMoreTokens()) {
			String read = stcomma.nextToken().trim();
			count++;
			if (read.contains(" ") || read.contains(" * ")
					|| read.contains("/") || read.contains(")")) 
			{
				formula.append(read);
				
				if (count < stCommaNb) {
					formula.append(",");
				}
			
			} else {
				tempal.add(read);
			}
		}

		// getting the octaveId instead of functionId from FunctionDefinition
		functionname = functiondefinition.getId();
		String octaveid = OctaveID.getOctaveId(functionname);
		funcDef.append("function z="+octaveid + "(");

		// add variables to stringbuilder seperated by ","
		for (int i = 0; i < tempal.size(); i++) {
			if (i < tempal.size() - 1) {
				funcDef.append(tempal.get(i) + ",");
			} else {
				funcDef.append(tempal.get(i));
			}
		}
		funcDef.append("), z=" + formula+";end\n\n");

		System.out.println("FunctionDefinition : id = " + functiondefinition.getId());
		
		if (matchMath(formula.toString(), tempal)) {
			return funcDef.toString();
		} else {
			String error = null;
			return error;
		}

	}

	/**
	 * Matches whether the elements in the formula are consistent with supported math operators <br />
	 * Function takes input formula, and Arraylist of valid names 
	 * 
	 * @param formula : Input formula to be checked
	 * @return : boolean whether math is supported or not
	 */
	private boolean matchMath(String formula,
			ArrayList<String> additionalValidNames) {
		boolean support = true;

		Matcher mathMatcher = idPattern.matcher(formula);
		ArrayList<String> checkoperator = new ArrayList<String>();
		while (mathMatcher.find()) {
			checkoperator.add(mathMatcher.group());
			// check for id
		}

		for (String check : checkoperator) {

			if (OctaveID.getSBMLId(check) != null) {
			} else {
				if (mathOperators.contains(check)) {
				} else if (additionalValidNames != null
						&& additionalValidNames.contains(check)) {
				} else {
					System.out.println("SBML2Octave : matchMath : the operator " + check + " is not supported by octave");
					System.out.println("SBML2Octave : matchMath : the formula is : " + formula);
					support = false;
				}

			}

		}

		return support;
	}

	/**
	 * Only for Function Definition <br />
	 * Matches whether the elements in the formula are consistent with supported math operators by {@link OctaveID#checkOctaveId()}
	 *  
	 * @param formula : Input formula to be checked
	 * @return : boolean whether math is supported or not
	 */
	private boolean matchMath(String formula) {
		boolean support = true;
System.out.println(formula);
		Matcher mathMatcher = idOctavePattern.matcher(formula);
		ArrayList<String> checkoperators = new ArrayList<String>();
		while (mathMatcher.find()) {
			
			checkoperators.add(mathMatcher.group());
			// check for id
		}

		for (String check : checkoperators) {
			if (OctaveID.getSBMLId(check) != null) {
			} else {
				if (mathOperators.contains(check)) {
				} else {
					System.out.println("SBML2Octave : matchMath : the operator " + check + " is not supported by Octave");
					System.out.println("SBML2Octave : matchMath : the formula is : " + formula);
					support = false;
				}

			}

		}

		return support;
	}

	/**
	 * Converts the math(reaction) to infix notation by libSBML, 
	 * Prints reaction information (Reaction id, name),  <br />
	 *   
	 * @param reaction : Reaction element of SBML model
	 * @return : corresponding Infix reaction
	 * @throws SBMLException 
	 */
	private String printReaction(Reaction reaction) throws SBMLException {

		StringBuilder reactionStr = new StringBuilder();
		String reactionId = reaction.getId();
		
		// print comment
		reactionStr.append("\n% Reaction: id = " + reactionId);

		if (reaction.getName() != null
				&& reaction.getName().trim().length() != 0) {
			reactionStr.append(", name = " + reaction.getName());
		}


		// print all local parameters
		KineticLaw kineticLaw = reaction.getKineticLaw();
		String kineticLawStr = "";

		if (kineticLaw != null) {
			kineticLawStr = kineticLaw.getMath().toFormula();

			ListOf<LocalParameter> localParameters = kineticLaw.getListOfParameters();

			for (LocalParameter parameter : localParameters) {

				buildIdMap(parameter, OctaveID.getOctaveId(reactionId)+"_"+parameter.getId());

				String parameterId = parameter.getId();
				String localParameterId = OctaveID.getOctaveId(parameterId);
				


				if (parameter.isSetValue()) {
					nbParameters++;
					reactionStr.append(printLocalParameter(parameter,
							localParameterId));
					parameter.setId(localParameterId);
				} else {
					// Error, a local parameter should always be constant
					System.out.println("Warning !! the local parameter " + localParameterId
							+ " is not constant !!");
				}

				// replace other ids if they are not "Octave valid"

				kineticLawStr = replaceIdInsideFormula(kineticLawStr, parameterId,
						localParameterId);

			}

			// replace other ids if they are not "Octave valid"
			kineticLawStr = replaceIdInsideFormula(kineticLawStr);

			// replace "+ -" by "-"
			kineticLawStr = replacePlusMinusInsideFormula(kineticLawStr);
			

		} else {			
			throw new AssertionError("The model cannot be converted as there are some kineticLaw undefined.");
		}

		reactionStr.append("\n\t" + OctaveID.getOctaveId(reaction.getId()) + "=" + kineticLawStr + ";\n");
		
		System.out.println("Reaction : id = " + reactionId);
		
		// checks mathML for reaction
		if (matchMath(kineticLawStr.toString()))
			return reactionStr.toString();
		else
			return null;

	}

	/**
	 * Replaces the formula elements in reference to OctaveId 
	 * Function takes formula, OldID and new Id
	 * @param formula : Any formula in SBML model
	 * @return : formula with changed/replaced Id's
	 */
	private String replaceIdInsideFormula(String formula, String oldId,
			String newId) {

		StringBuffer formulaWithIdsBuffer = new StringBuffer();

		Matcher formulaMatcher = idPattern.matcher(formula);

		while (formulaMatcher.find()) {
			String id = formulaMatcher.group().trim();

			if (id.equals(oldId)) {
				id = newId;
			}

			formulaMatcher.appendReplacement(formulaWithIdsBuffer, id);
		}

		formulaMatcher.appendTail(formulaWithIdsBuffer);

		return formulaWithIdsBuffer.toString();
	}

	/**
	 * Replaces the formula elements in reference to OctaveId 
	 * 
	 * @param formula : Any formula in SBML model
	 * @return : formula with changed/replaced Id's
	 */
	 
	private String replaceIdInsideFormula(String formula) {

		StringBuffer formulaWithIdsBuffer = new StringBuffer();

		Matcher formulaMatcher = idPattern.matcher(formula);

		while (formulaMatcher.find()) {
			String id = formulaMatcher.group().trim();

			String newId = OctaveID.getOctaveId(id);

			if (newId != null) {
				formulaMatcher.appendReplacement(formulaWithIdsBuffer, newId);
			}
		}

		formulaMatcher.appendTail(formulaWithIdsBuffer);

		return formulaWithIdsBuffer.toString();
	}

	/**
	 * Appends the "+" or "-" sign to parameters/elements, accordingly when it is a reactant or a product  
	 * @param formula 
	 * @return : formula with changed sign. 
	 */
	private String replacePlusMinusInsideFormula(String formula) {

		StringBuffer formulaWithIdsBuffer = new StringBuffer();

		Matcher formulaMatcher = plusMinusPattern.matcher(formula);

		while (formulaMatcher.find()) {

			formulaMatcher.appendReplacement(formulaWithIdsBuffer, "- ");
		}

		formulaMatcher.appendTail(formulaWithIdsBuffer);

		return formulaWithIdsBuffer.toString();
	}

	/**
	 * Prints information on Local parameters <br />
	 * also calls the check for consistent Octaveid length by {@link OctaveID#checkOctaveId()}
	 * @param parameter
	 * @param id
	 * @return
	 */
	private String printLocalParameter(LocalParameter parameter, String id) {
		StringBuilder paramStr = new StringBuilder();

		paramStr.append(printLocalParameterComment(parameter));

		paramStr.append("\t"+id + "=" +
				 parameter.getValue() + ";\n");

		return paramStr.toString();

	}


	/**
	 * using libSBML changes the Rules(mathML) to Infix notation
	 *  
	 * @param rule
	 * @return
	 * @throws SBMLException 
	 */
	private String printRule(Rule rule) throws SBMLException {
		
		String ruleStr ="";
		String variableId = null;
		
		/*Getting VariableId*/
		if(rule.isRate()) {
			variableId = ((RateRule)rule).getVariable();
			
		}
		else if(rule.isAssignment()) {
			variableId = ((AssignmentRule)rule).getVariable();
		}
		
		String octaveId = OctaveID.getOctaveId(variableId);
		String mathMLStr = rule.getMath().toFormula();
		mathMLStr = replaceIdInsideFormula(mathMLStr);
		String ruleComment;
		
		System.out.println("Rule : variable = " + variableId);
		
		if (matchMath(mathMLStr)) {
			ruleComment = "% " + rule.getElementName() + ": variable = "
					+ variableId+"\n";
			
			if (rule.isRate()) {
				
				// Get the initial values or have that done before ?
				RateRule variable = (RateRule)rule;
				String initialValue = "TODO";
				if (variable.getVariableInstance() instanceof Compartment) {
					initialValue = "" + ((Compartment) variable.getVariableInstance()).getSize();
				} else if (variable.getVariableInstance() instanceof Species) {
					
					Species s = (Species) variable.getVariableInstance();
					
					if (s.isSetInitialAmount()) {
						initialValue = "" + s.getInitialAmount();
					} else if (s.isSetInitialConcentration()) {
						initialValue = "" + s.getInitialConcentration();						
					} // TODO - we should handle the case where no initial value is given (or value is given through initialAssigment)
					else {
						initialValue = "NaN";
					}
					
				} else if(variable.getVariableInstance() instanceof Parameter) {
					initialValue = "" + ((Parameter) variable.getVariableInstance()).getValue();
				}  
				
				ruleStr += ruleComment;
				nbEquaDiff++;
				ruleStr += octaveId+" = x("+nbEquaDiff+");\n";
				
				String equaNum = Integer.toString(nbEquaDiff);
				
				
				xdot.add(new EquaDiff(initialValue, mathMLStr, octaveId,ruleComment,equaNum));
				
			} else if (rule.isAssignment()) {
				ruleStr+=ruleComment;
				ruleStr+="\t" + octaveId + "=" + mathMLStr + ";\n";
			}
			
			
			return ruleStr;
		} else {
			// TODO : Print a warning to say that there are some unsupported functions
			return null;
		}


	}

	/**
	 * using libSBML changes the Rules(mathML) to Infix notation
	 *  
	 * @param rule
	 * @return
	 * @throws SBMLException 
	 */
	private String printAlgebraicRule(Rule rule) throws SBMLException {
		
		StringBuilder ruleStr = new StringBuilder();

		String mathMLStr = rule.getMath().toFormula();
		mathMLStr = replaceIdInsideFormula(mathMLStr);
		
		System.out.println("Algebraic Rule");
		
		if (matchMath(mathMLStr)) {
			ruleStr.append("% " + rule.getElementName() + "\n");
			ruleStr.append("% Warning, " + rule.getElementName() + " are not supported at the moment.\n");
			ruleStr.append("% " + mathMLStr + " = 0\n");
			
			return ruleStr.toString();
		} else {
			// TODO : Print a warning to say that there are some unsupported functions
			return null;
		}


	}

	/**
	 * Checks for Consistent OctaveId {@link OctaveID#checkOctaveId()}<br/>
	 * Checks for initial Concentration and/or Initial Amount is defined
	 * Checks for Spatial Dimensions(Compartments), and accordingly returns Species Units <br/>
	 * Checks for Species Boundary Condition and Rules,
	 *  
	 * @param species : Species Element of SBML Model
	 * @return 
	 * 
	 */
	private String printSpecies(Species species) {

		StringBuilder speciesStr = new StringBuilder();
		String checkmath =null;
		String speciesComment = printSpeciesComment(species);

		String id = OctaveID.getOctaveId(species.getId());
		
		// TODO : check why we are not using these two boolean anymore.
		// TODO - indeed, we should probably use them to prevent adding unnecessary entries in the xdot list
		// boolean isAffectedByRule = false;
		// boolean isAffectedByAssignmenRule = false;		
		boolean needInit = true;
				
		for (Rule rule : sbmlModel.getListOfRules()) {
			if (!(rule.isAlgebraic())) {
				if(rule.isRate() && id.equals(((RateRule)rule).getVariableInstance().getId()) || (rule.isAssignment() && id.equals(((AssignmentRule)rule).getVariableInstance().getId()))) { 
					// isAffectedByRule = true;
				
					if (rule instanceof AssignmentRule) { // && speciesFluxMap.get(species) == null // removed as in fact, if there is an assignmentRule it is always an octave parameter.
						// isAffectedByAssignmenRule = true;
						needInit = false;
					}
				}
			}
		}

		
		String init = "0"; 

		if (needInit) {
			if (species.isSetInitialAmount()
					&& species.isSetInitialConcentration()) {

				throw new AssertionError("Error in model, both Initial concentration and Initial amount defined");

			} else if (species.isSetInitialAmount()) {
				init = ""+species.getInitialAmount();

			} else if (species.isSetInitialConcentration()) {
				init = ""+species.getInitialConcentration();

			}
		}

		if (species.isBoundaryCondition()) {
			if (!hasRule(species.getId()) && !hasEvent(species.getId())) {

				speciesStr.append("0.0");
				speciesComment+="\n%WARNING speciesID: "
						+ species.getId()
						+ ", constant= false "
						+ " , boundaryCondition = "
						+ species.isBoundaryCondition()
						+ " but is not involved in assignmentRule, rateRule or events !"
						+ "\n";

			}
		} else if (!species.isBoundaryCondition()) {
			// valid assignment rule/rate rule
			if (!hasRule(species.getId())) {
				// check if species is reactant/product
				if (speciesFluxMap.get(species) != null
						&& !(isHasSubstanceUnits.get(species))) {

					String sbmlId = species.getCompartmentInstance().getId();
					String octaveId = OctaveID.getOctaveId(sbmlId);
					checkmath = " (1/(" + octaveId	+ "))*(" + speciesFluxMap.get(species) + ")";
					speciesStr.append("(1/(" + octaveId
							+ "))*(" + speciesFluxMap.get(species) + ")");
				} else if (speciesFluxMap.get(species) != null) {
					speciesStr.append(speciesFluxMap.get(species));

				}
			}
		}
		System.out.println(hasRule(species.getId()));
		if(hasRule(species.getId())) {
			String octaveId = OctaveID.getOctaveId(species.getId());
			speciesStr.append(octaveId);
			checkmath = octaveId;
		}
		
		System.out.println("Species : id = " + species.getId());
		String equaNum = id.substring(id.lastIndexOf("(")+1,id.lastIndexOf(")"));
		
		if (checkmath != null) {
			if (matchMath(checkmath)){
				xdot.add(new EquaDiff(init, speciesStr.toString(), id,speciesComment,equaNum));
				return speciesStr.toString();
			}
			else
				return null;

		}else {
			if (!hasRule(species.getId())) {
				xdot.add(new EquaDiff(init, speciesStr.toString(), id,speciesComment,equaNum));
				return speciesStr.toString();
			}
			else {
				return null;
			}
		}
	}

	/**
	 * Checks is an SBML element, represented by the id passed as argument, is affected by a rule. 
	 *  
	 * @param id an SBML element id.
	 * @return : true if element is affected by a rule  
	 */
	private boolean hasRule(String id) {
		for (Rule rule : sbmlModel.getListOfRules()) {
			if (!(rule.isAlgebraic())){
				if((rule.isRate() && id.equals(((RateRule)rule).getVariableInstance().getId())) || (rule.isAssignment() && id.equals(((AssignmentRule)rule).getVariableInstance().getId())))
				return true;
			}

		}
		
		return false;
	}
	
	/**
	 * Checks is an SBML element, represented by the id passed as argument, is affected by an event. 
	 *  
	 * @param id an SBML element id.
	 * @return : true if element is affected by an event  
	 */
	private boolean hasEvent(String id) {
		for (Event event : sbmlModel.getListOfEvents()) {
			
			for (EventAssignment eventAssgnt : event.getListOfEventAssignments()) {
				if (eventAssgnt.getVariable().equals(id)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Prints Constant species and calls {@link OctaveID#checkOctaveId()}
	 * Also checks for Intial concentration or amount defined
	 * @param species
	 * @return
	 */
	private String printConstantSpecies(Species species) {
		StringBuilder speciesStr = new StringBuilder();

		speciesStr.append(printSpeciesComment(species));

		String id = OctaveID.getOctaveId(species.getId());

		if (species.isSetInitialAmount()
				&& species.isSetInitialConcentration()) {

			throw new AssertionError("Error in model, both Initial concentration and Initial amount defined");

		} else if (species.isSetInitialAmount()) {
			speciesStr.append("\t" + id + "=" +
					species.getInitialAmount()+";\n");

		} else if (species.isSetInitialConcentration()) {
			
			speciesStr.append("\t" + id + "=" +
					species.getInitialConcentration()+";\n");
		}
		// todo completed
		return speciesStr.toString();
	}

	/**
	 * Adds Species Information <br />
	 * Checks for Rule and Boundary condition for species(!= Constant)
	 * 
	 * @param species : Element in SBML model
	 * @return : species Flux Map
	 */
	private String printSpeciesComment(Species species) {

		StringBuilder commentStr = new StringBuilder();
		String id = species.getId();
		String name = species.getName();

		if (name == null || name.trim().length() == 0) {
			name = id;
		}

		commentStr.append("\n% Species: ");
		commentStr.append("  id = " + id);
		commentStr.append(", name = " + name);

		if (species.isConstant()) {
			commentStr.append(", constant");
		} else {
			// check for boundary condition and then display
			if (species.isBoundaryCondition()) {
				if (hasRule(id)) {
					commentStr.append(", involved in a rule ");
				} else if (!hasRule(id)) {
					
				}
			} else if (!species.isBoundaryCondition()) {
				// affected by rules or kinetic law not both
				if (hasRule(id)) {
					commentStr.append(", defined in a rule ");
				} else if (speciesFluxMap.get(species) != null) {
					commentStr.append(", affected by kineticLaw\n");
				} else {
					commentStr.append("\n% Warning species is not changed by either rules or reactions\n");
				}
			}

			int nbEvent = getNbAffectingEvent(species);
			
			if (nbEvent > 0) {
				commentStr.append("% Species is changed by " + nbEvent + " event(s)");
			}

		}

		return commentStr.toString();
	}

	
	/**
	 * Returns the number of event that have an assignment affecting the element passed as argument.
	 * 
	 * @param element and SBMl element
	 * 
	 * @return the number of event that have an assignment affecting the element passed as argument.
	 */
	private int getNbAffectingEvent(SBase element) {
		
		int n = 0;
		if(!(element instanceof AbstractSBase)) {
			String elementId = ((NamedSBase)element).getId();
			
			for (Event event : sbmlModel.getListOfEvents()) {
				for (EventAssignment eventAssignment : event.getListOfEventAssignments()) {
					if (elementId.equals(eventAssignment.getVariable())) {
						n++;
						break;
					}
				}
			}
		}
		
		return n;
	}
	
	

	/**
	 * 
	 * Adds parameter information as comments<br/> 
	 * Checks for whether parameter has a rule , or else gives a warning
	 *  
	 * @param : Parameter element of SBML
	 * @return : parameter information with Equation
	 */
	private String printParameter(Parameter parameter) {
		StringBuilder paramStr = new StringBuilder();

		paramStr.append(printParameterComment(parameter));
		
		boolean hasRule = hasRule(parameter.getId());
		
		if (!hasRule && !hasEvent(parameter.getId())) {
			paramStr.append("% Warning parameter " + parameter.getId()
					+ " is not constant, it should be controlled by a Rule and/or events" + "\n");
		}
		
		if (!hasRule) {
			String id = OctaveID.getOctaveId(parameter.getId());

			paramStr.append("\t" + id + "=" + parameter.getValue() + ";\n");
		}
		
		return paramStr.toString();
	}

	/**
	 * 
	 * Adds parameter information as comments 
	 * 
	 * @param parameter : Parameter Element of SBML model
	 * @return : constant Parameter with initial value
	 */
	private String printConstantParameter(Parameter parameter) {
		StringBuilder paramStr = new StringBuilder();

		paramStr.append(printParameterComment(parameter));

		String id = OctaveID.getOctaveId(parameter.getId());

		paramStr.append("\t" + id + "=" + parameter.getValue() + ";\n");

		return paramStr.toString();
	}

	/**
	 * Adds parameter information as comments <br /> 
	 * Checks whether parameter is a local parameter or a global parameter
	 * 
	 * @param Parameter :  Element of SBML Model
	 * @return : parameter information as string
	 */
	private String printParameterComment(Parameter parameter) {

		StringBuilder commentStr = new StringBuilder();

		String id = parameter.getId();
		String name = parameter.getName();

		if (name == null || name.trim().length() == 0) {
			name = id;
		}

		/*TODO: find getReaction
		if (parameter.getReaction() != null) {
			commentStr.append("    % Local Parameter: ");
		} else {
			commentStr.append("% Parameter: ");
		}*/

		commentStr.append("% Parameter: ");
		commentStr.append("  id =  " + id);
		commentStr.append(", name = " + name);

		/*TODO:find isConstant()
		if (parameter.isConstant()) {
			commentStr.append(", constant");
		} else if (hasRule(id)) {
			commentStr.append(", defined by a Rule");
		}*/

		commentStr.append("\n");

		return commentStr.toString();
	}
	
	/**
	 * Adds parameter information as comments <br /> 
	 * Checks whether parameter is a local parameter or a global parameter
	 * 
	 * @param Parameter :  Element of SBML Model
	 * @return : parameter information as string
	 */
	private String printLocalParameterComment(LocalParameter parameter) {

		StringBuilder commentStr = new StringBuilder();

		String id = parameter.getId();
		String name = parameter.getName();

		if (name == null || name.trim().length() == 0) {
			name = id;
		}

		/*TODO: find getReaction
		if (parameter.getReaction() != null) {
			commentStr.append("    % Local Parameter: ");
		} else {
			commentStr.append("% Parameter: ");
		}*/

		commentStr.append("\t% Local Parameter: ");
		commentStr.append("  id =  " + id);
		commentStr.append(", name = " + name);

		/*TODO:find isConstant()
		if (parameter.isConstant()) {
			commentStr.append(", constant");
		} else if (hasRule(id)) {
			commentStr.append(", defined by a Rule");
		}*/

		commentStr.append("\n");

		return commentStr.toString();
	}

	/**
	 * Adds compartment information as comments<br/> 
	 * Checks for whether compartment has a rule , or else gives a warning
	 * 
	 * @param compartment : Compartment element of SBML
	 * @return : Compartment information with Equation
	 */
	private String printCompartment(Compartment compartment) {

		StringBuilder compStr = new StringBuilder();

		compStr.append(printCompartmentComment(compartment));
		if (!hasRule(compartment.getId())) {
			compStr.append("% Warning compartment " + compartment.getId()
					+ " has no rule and is not constant" + "\n");
		}

		return compStr.toString();
	}

	/**
	 * Prints compartment comments: 
	 * id, name, constant or defined by a rule
	 * @param : compartment : Compartment element from SBML model
	 * @return : compartment information
	 */
	private String printCompartmentComment(Compartment compartment) {

		StringBuilder compCommentStr = new StringBuilder();

		String id = compartment.getId();
		String name = compartment.getName();

		if (name == null || name.trim().length() == 0) {
			name = id;
		}

		compCommentStr.append("% Compartment: id = " + id + ", name = " + name);

		if (compartment.isConstant()) {
			compCommentStr.append(", constant");
		} else if (hasRule(id)) {
			compCommentStr.append(", defined by a Rule");
		}

		compCommentStr.append("\n");

		return compCommentStr.toString();
	}

	/**
	 * Adds compartment information as comments 
	 * 
	 * @param compartment : Compartment element of SBML model 
	 * @return : constant compartment with initial size
	 */
	private String printConstantCompartment(Compartment compartment) {

		StringBuilder compStr = new StringBuilder();

		compStr.append(printCompartmentComment(compartment));

		String id = OctaveID.getOctaveId(compartment.getId());

		compStr.append("\t" + id + "=" + compartment.getSize() + ";\n");

		return compStr.toString();

	}
	
	
	private void buildIdMap(SBase element, String wantedOctaveId) {

		String id = ((NamedSBase)element).getId();

		OctaveID octaveId = new OctaveID(id,wantedOctaveId,true);
		octaveId.checkOctaveId();
	}



	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {
		try {
			inputModel = model;
			return octaveExport((SBMLModel)model);
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new ReadModelException(e.getMessage());
		}
		// this generates null pointer exception..
		//return null;
	}
	
	
	//Inner class representing one differential equation
	class EquaDiff {
		private String init;
		private String equa;
		private String octaveId;
		private String comment;
		private String equaNumber;
		
		public EquaDiff(String init, String equa, String octaveId, String comment,String equaNum) {
			super();
			this.init = init;
			this.equa = equa;
			this.octaveId = octaveId;
			this.comment = comment;
			this.equaNumber = equaNum;
		}
		public String getInit() {
			return init;
		}
		public String getEqua() {
			return equa;
		}
		public String getOctaveId() {
			return octaveId;
		}
		public String getComment() {
			return comment;
		}
		
		public void setEqua(String equa) {
			this.equa = equa;
		}
		public String getEquaNumber() {
			return equaNumber;
		}
		public void setEquaNumber(String equaNumber) {
			this.equaNumber = equaNumber;
		}
		
	}

	public String getResultExtension() {
		return ".m";
	}
	
	@Override
	public String getName() {
		return "SBML2OCTAVE";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to Octave";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to Octave";
	}
	
}
