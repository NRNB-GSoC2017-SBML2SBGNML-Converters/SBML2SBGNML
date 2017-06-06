/*
 * $Id: SBML2Dot.java 631 2016-03-09 11:52:19Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2dot/SBML2Dot.java $
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
package org.sbfc.converter.sbml2dot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.DotModel;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.util.graphviz.GraphViz;


/** SBML2Dot
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

 Nicolas Rodriguez
 EMBL-EBI, Welcome-Trust Genome Campus, Hinxton, Cambridge, CB10 1SD, UK
 biomodels-net-team@lists.sourceforge.net


 */

/**
 * Convert an SBML file into an dot file.
 *  
 * @author Nicolas Rodriguez
 * @author jalowcki
 * 
 */
public class SBML2Dot extends GeneralConverter {

	// Set a logger for debug message
	private static Logger logger = Logger.getLogger(SBML2Dot.class);
	
	public static int MAX_EDGE_ALLOWED = 4;
	public static int MAX_REACTION_ALLOWED = 3000;
	// set a SBGNUtils object for the conversion
	public static SBGNUtils converter = new SBGNUtils("dot");

	public static Hashtable<String, String> speciesAnnotationMap = new Hashtable<String, String>(); 

	static {

		// We are not using the 3 others hash tables.
		ConstraintFileReader.parseSBML2SBGNConfigFile(speciesAnnotationMap, new Hashtable<String, String>(), 
				new Hashtable<String, String>(), new Hashtable<String, String>(), "classSBGN");

	}

	public boolean cloning = false;

	public String fileNameSuffix = "";

	private String sbmlFileName;

	private HashMap<Species, Integer> speciesCardinalityMap = new HashMap<Species, Integer>();

	Model sbmlModel;

	/**
	 * <b>Constructor SBML2Dot.</b><br/> Export from <a href="http://sbml.org/"><b>SBML</b></a> (Systems Biology
	 * Markup Language) to <ahref="http://www.graphviz.org/Documentation.php"><b>Dot</b></a>.
	 * 
	 * Provide it a file when you call the program with the command
	 * <code><pre>java uk.ac.ebi.sbml.converter.dot.SBML2Dot &lt;SBMLfile&gt;</code>
	 * </pre>
	 * 
	 * libSBML is used to check the SBML file provided.<br/>
	 * 
	 * 
	 * Save the dot file in the same file path and name that the input file, but
	 * replace the extension by .dot.
	 * 
	 * @param sbmlFileName
	 *            Path of the SBML file to export
	 */
	public SBML2Dot(String sbmlFileName) {

		this.sbmlFileName = sbmlFileName;
		cloning = false;
		fileNameSuffix = "_auto";
	}

	public SBML2Dot() {
		this(null);
	}
	
	// TODO : use a login system everywhere

	/**
	 * Adds one to the cardinality of a species. 
	 * Used to know how many edges will arrive to one species in the graph.
	 * 
	 * @param listOf a reactant, product or modifier
	 */
	private void addCardinality(ListOf<? extends SimpleSpeciesReference> listOf) {

		for (SimpleSpeciesReference speciesRef : listOf) {
			Species species = speciesRef.getSpeciesInstance();

			Integer cardinality = speciesCardinalityMap.get(species);

			if (cardinality == null) {
				cardinality = 1;
			} else {
				cardinality++;
			}

			speciesCardinalityMap.put(species, cardinality);
		}
	}
	
	
	/**
	 * Creates a cloned species in the graph.
	 * 
	 * @param out
	 * @param reactionId
	 * @param species
	 * @param suffix
	 * @return the id of the cloned species in the graph.
	 */
	private String createCloneSpecies(PrintWriter out, String reactionId,
			Species species, String suffix, String shape) {

		String clonedSpeciesNodeId = reactionId + "_" + species.getId() + "_" + suffix;

		out.println("    " + clonedSpeciesNodeId + " [label=\"" + getDisplayName(species) + "\", shape=" + shape + ";");

		return clonedSpeciesNodeId;
	}
	
	
	/**
	 * Creates a clone node to represent the empty set. 
	 * 
	 * @param out
	 * @param reactionId
	 * @return the id of the cloned empty set in the graph.
	 */
	private String createEmptySetNode(PrintWriter out, String reactionId) {

		String emptySetNodeId = reactionId + "_empty";

		out.println("    " + emptySetNodeId + " [shape=plaintext, label=\"Ø\", fontsize=15,fontcolor=blue];");

		return emptySetNodeId;
	}

	
	
	/**
	 * Create an intermediate reaction node either for a reactant (suffix = r) or a product (suffix = p) 
	 * and create then an arc to link the new node to its process node.
	 * 
	 * @param out
	 * @param reaction
	 * @param suffix : available "r" (reactant); "p" (product)
	 * @return
	 */
	private String createIntermediateReactionNode(PrintWriter out, Reaction reaction, String suffix) {

		String intermediateReactionNodeId = reaction.getId() + "_intermediate_" + suffix;

		out.println("      " + intermediateReactionNodeId + " [label=\"\", shape=point,width=0.001,height=0.001];"); // ,size=0.01,style=invisible,weight=150,,style=\"invis\"
		// out.println("    " + intermediateReactionNodeId + " [label=\"\", shape=circle,width=0.01,style=filled];");

		if (suffix.equals("r")) {
			out.println("      \"" + intermediateReactionNodeId + "\" -> \"" + reaction.getId() + "\"[arrowhead=none];\n"); // weight=150,minlen=0.2,tailport=w
		} else if (suffix.equals("p")) {
			out.println("      \"" + reaction.getId() + "\" -> \"" + intermediateReactionNodeId + "\"[arrowhead=none];\n"); //,weight=150,tailport=e
		} else {
			logger.info("Bad suffix format, available: \"r\" or \"p\"");
		}

		return intermediateReactionNodeId;
	}


	/**
	 * <b>Method of the export.</b><br/>
	 * 
	 * Use jSBML to read the SBML model object create before.<br/>
	 * 
	 * @param sbmlDocument
	 * @param out
	 */
	public void dotExport(SBMLDocument sbmlDocument, PrintWriter out) throws SBMLException {

		try {
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2Dot: Input file is not a regular SBML file.");
		}			
		
		
		String modelName = sbmlModel.getName();

		// SBML elements that we can export in Dot
		ListOf<Species> listOfSpecies = sbmlModel.getListOfSpecies();
		ListOf<Reaction> reactions = sbmlModel.getListOfReactions();

		// Analyze the graph and decide to clone some species with too much links 
		if (cloning) {
			for (Reaction reaction : reactions) {

				addCardinality(reaction.getListOfReactants());

				addCardinality(reaction.getListOfProducts());

				addCardinality(reaction.getListOfModifiers());
			}
		}


		if (modelName == null || modelName.trim().length() == 0) {
			modelName = sbmlModel.getId();
		}				
		out.println("// " + modelName + "\n");

		println("// " + modelName + "\n");

		// print the beginning of the graph object
		out.println("digraph G {\n");
		out.println("  node [fontsize=10];\n");
		out.println("  graph [rank=same];\n");
		out.println("  graph [rankdir=LR];\n");

		if (listOfSpecies.size() == 0 || reactions.size() == 0) {
			out.println(" noSpecies[fontsize=14,label=\"There are no species or no reactions in this model so no graph can be automatically generated.\", shape=\"plaintext\"] }" );
			out.close();
			return;
		}
		println("SBML2Dot : dotExport : nb Reactions = " + reactions.size());
		if (reactions.size() >= MAX_REACTION_ALLOWED) {
			out.println(" toMuchReaction[fontsize=14,label=\"There are too many species or reactions in this model so no graph are automatically generated.\", shape=\"plaintext\"] }" );
			out.close();
			return;
		}

		out.println("  // Species\n");

		for (Species species : listOfSpecies) {

			if (speciesCardinalityMap.get(species) == null) {
				speciesCardinalityMap.put(species, 0);
			}

			out.println(printSpeciesComment(species));
			// the empty set is not printed as a species here, it will be printed as an arc later
			if (species.getId().equalsIgnoreCase("emptyset") || speciesCardinalityMap.get(species) > MAX_EDGE_ALLOWED ) {
				out.println("\n");
				continue;
			}

			String displayName = species.getName();

			if (displayName == null || displayName.trim().length() == 0) {
				displayName = species.getId();
			} else {
				if (displayName.equalsIgnoreCase("emptyset") || displayName.equalsIgnoreCase("empty set")) {
					out.println("\n");
					continue;
				}
			}

			//checking sboTerm and annotations to get a most appropriate shape
			String shape = converter.getOutputFromClass(species, "unspecified entity");

			
			// an empty set cannot support a label
			if ( species.getSBOTerm() == 291 ) {
				
				// print the species
				out.println("    \"" + species.getId() + "\" [ shape= " + shape 
						+ "];\n");
			} else {
				
				// print the species
				out.println("    \"" + species.getId() + "\" [label=\"" + displayName + "\", shape= " + shape 
						+ "];\n"); // , color=pink // , style=rounded] (will be included in the shape string)
			}
			
			
			

		}

		// Reactions
		for (Reaction reaction : reactions) {

			String reactionId = reaction.getId(); 

			// Checking sboTerm or annotations to get a shape, (process)
			String shape = converter.getOutputFromClass(reaction, "process");

			// print the reaction node
			out.println("    " + reactionId + " [label=\"\", shape=" + shape 
					+ ", fixedsize=true, width=0.2, height=0.2];\n");

			ListOf<SpeciesReference> products = reaction.getListOfProducts();
			ListOf<SpeciesReference> reactants = reaction.getListOfReactants();
			ListOf<ModifierSpeciesReference> modifiers = reaction.getListOfModifiers();

			if (reactants.size() == 0) {
				// Create an empty set node with arc
				String emptySetNodeId = createEmptySetNode(out, reactionId);
				out.println("      \"" + emptySetNodeId + "\" -> \"" + reactionId + "\" [arrowhead=none];\n");
			}

			if (products.size() == 0) {
				// Create an empty set node with arc
				String emptySetNodeId = createEmptySetNode(out, reactionId);
				out.println("      \"" + reactionId + "\" -> \"" + emptySetNodeId + "\";\n");
			}

			//ARCS from reactants.
			String reactionNodeId = reactionId;

			//if (true) { // we create an intermediary node to try to force the edge to start on the middle of the reaction node 
			if (reactants.size() >= 1) {
				reactionNodeId = createIntermediateReactionNode(out, reaction, "r");
			}

			for (SpeciesReference reactant : reactants) {

				Species species = reactant.getSpeciesInstance();
				String speciesNodeId = species.getId();

				String speciesShape = converter.getOutputFromClass(species, "unspecified entity");

				if (species.getId().equalsIgnoreCase("emptyset")) {
					speciesNodeId = createEmptySetNode(out, reactionId);
				} else if (speciesCardinalityMap.get(species) > MAX_EDGE_ALLOWED) {
					// a clone is created... and ? TODO
					// how do we know the MAX is not exceeded anymore? Is there enough clones?
					speciesNodeId = createCloneSpecies(out, reactionId, species, "r", speciesShape);
				}

				// print the arrow / edge
				if (reaction.isReversible()) {
					out.println("      \"" + speciesNodeId + "\" -> \"" + reactionNodeId + "\"[arrowhead=none, arrowtail=normal,arrowsize=1.0];\n");					
				} else {
					out.println("      \"" + speciesNodeId + "\" -> \"" + reactionNodeId + "\"[arrowhead=none];\n");
				}
			}

			reactionNodeId = reactionId;

			//if (true) { // we create an intermediary node to try to force the edge to start on the middle of the reaction node 
			if (products.size() >= 1) {
				reactionNodeId = createIntermediateReactionNode(out, reaction, "p");
			}

			for (SpeciesReference product : products) {

				Species species = product.getSpeciesInstance();
				String speciesNodeId = species.getId();
				String speciesShape = converter.getOutputFromClass(species, "unspecified entity");

				if (speciesShape == null) {
				  speciesShape = "ellipse";
				}
				
				if (speciesShape.equalsIgnoreCase("emptyset")) {
					speciesNodeId = createEmptySetNode(out, reactionId);
				} else if (speciesCardinalityMap.get(species) > MAX_EDGE_ALLOWED) {
					speciesNodeId = createCloneSpecies(out, reactionId, species, "p", speciesShape);
				}

				// print the arrow / edge
				out.println("      \"" + reactionNodeId + "\" -> \"" + speciesNodeId + "\";\n");					
			}



			reactionNodeId = reactionId;

			for (ModifierSpeciesReference modifier : modifiers) {

				Species species = modifier.getSpeciesInstance();
				String speciesNodeId = species.getId();
				String arrowHead = converter.getOutputFromClass(modifier, "modulation");

				if (speciesCardinalityMap.get(species) > MAX_EDGE_ALLOWED) {
					String speciesShape = converter.getOutputFromClass(species, "unspecified entity");
					speciesNodeId = createCloneSpecies(out, reactionId, species, "m", speciesShape);					
				}

				// print the arrow / edge
				out.println("      \"" + speciesNodeId + "\" -> \"" + reactionNodeId + "\"[arrowhead=" + arrowHead + "];\n");					
			}

		}

		out.println("\n}\n");

		out.close();

		println("SBML2Dot : dotExport : Export done");

	}
	

	/**
	 * Creates an appropriate name to be displayed on the graph.
	 * 
	 * If the species has no name, use the id. 
	 * 
	 * TODO : If the name is too long, use the id or shorten the name or try use a web service to get a shorter name.
	 * 
	 * @param {@link Species}
	 * @return displayName
	 */
	private String getDisplayName(Species species) {

		String displayName = species.getName();

		if (displayName == null || displayName.trim().length() == 0) {
			displayName = species.getId();
		}

		return displayName;
	}
	
	
	/**
	 * Creates and returns a new SBMLDocument
	 * 
	 * @return an SBMLDocument or null if the document is not a valid SBML file.
	 */
	public SBMLDocument getSBMLDocument() {

		SBMLDocument document = null;
		SBMLReader reader = new SBMLReader();

		try {
			document = reader.readSBML(sbmlFileName);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		long errors = 0; // TODO : add the validate :
		// LibSBMLUtilities.validate(document);

		if (errors != 0) {

			println("SBML2Dot : getSBMLDocument : checkConsistency : model not valid:\n--"
					+ errors + " error(s)");
			document = null;

		} else {

			println("SBML2Dot : getSBMLDocument : SBML File = " + sbmlFileName);
			println("SBML2Dot : getSBMLDocument : SBML Level "
					+ document.getLevel() + " Version " + document.getVersion()
					+ " to XPP\n");

			Model sbmlModel = document.getModel();
			println("SBML2Dot : getSBMLDocument : Model: " + sbmlModel.getId()
					+ ", " + sbmlModel.getName() + "\n");
		}

		return document;
	}
	
	
	/**
	 * 
	 * @param id id of an SBML element
	 * @return true if this element is affected by a rule.
	 */
	private boolean hasRule(String id) {
		for (Rule rule : sbmlModel.getListOfRules()) {
			if (rule instanceof ExplicitRule){
				ExplicitRule explicitRule = (ExplicitRule) rule;
				if (id.equals(explicitRule.getVariable())) {
					return true;
				}
			}
		}

		return false;
	}

	
	/**
	 * TODO maybe useful to see if we have old or new MIRIAM annotations
	 * @param annotation
	 * @return
	 */
	MiriamAnnotation parseMiriamAnnotation(String annotation) {

		String uri = null;
		String id = null;

		int indexOfDash = annotation.indexOf("#");
		int indexOfColon = annotation.lastIndexOf(":");

		if (indexOfDash != -1) {
			String[] annotationParts = annotation.split("#");
			uri = annotationParts[0];
			id = annotationParts[1];
		} else if (indexOfColon != -1) {
			uri = annotation.substring(0, indexOfColon);

			try {
				id = URLDecoder.decode(annotation.substring(indexOfColon + 1), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// System.out.println("SBML2Biopax : parseMiriamAnnotation : uri = " + uri + ", id = " + id);

		if (uri != null && id != null) {

			// Could use a call to miriamWS.getMiriamUri(String)
			// but this way avoid a lot of unecessary call to the webservice
			// if the miriam scheme is changing again may be it would be simplier to use the WS function.
			String officialURN = ConstraintFileReader.officialURIs.get(uri);

			if (officialURN == null) {
				officialURN = uri;
			}

			return new MiriamAnnotation(id, officialURN);			
		}

		return null;
	}
	
	
	static void println(String msg) {
		logger.info(msg);
	}

	static void println(boolean msg) {
		logger.info(msg);
	}

	static void println(int msg) {
		logger.info(msg);
	}

	static void println(double msg) {
		logger.info(msg);
	}

	
	/**
	 * Adds Species Information in the dot file
	 * 
	 * @param species : Element in SBML model
	 * 
	 */
	private String printSpeciesComment(Species species) {

		StringBuilder commentStr = new StringBuilder();
		String id = species.getId();
		String name = species.getName();

		if (name == null || name.trim().length() == 0) {
			name = id;
		}

		commentStr.append("    // Species: ");
		commentStr.append("  id = " + id);
		commentStr.append(", name = " + name);

		if (species.isConstant()) {
			commentStr.append(", constant");
		} else {
			// check for boundary condition and then display
			if (species.isBoundaryCondition()) {

				commentStr.append(", involved in a rule ");

			} else if (!species.isBoundaryCondition()) {
				// affected by rules or kinetic law not both
				if (hasRule(id)) {
					commentStr.append(", involved in a rule ");
				} else {
					commentStr.append(", affected by kineticLaw");
				} 
			}
		}

		if (speciesCardinalityMap.get(species) != null && speciesCardinalityMap.get(species) > MAX_EDGE_ALLOWED) {
			commentStr.append(", will be cloned in the graph.");
		}

		return commentStr.toString();
	}
	
	
	/**
	 * Inputs the SBML file from the specified path. Generate a dot file and can generate a image file through graphviz if image
	 * format is specified.
	 * @param args
	 *            MUST have just one parameter: the SBML file path
	 *            MAY have just one image format export parameter: svg, png, jpg
	 * */
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		
		if (args.length < 1 || args.length > 2) {
			println("  usage: java org.sbfc.converter.dot.sbml2dot <SBML filename> [<image format>]");
			println("The filename has to be an absolute path.");
			return;
		}

		String fileName = args[0];
		
		SBML2Dot sbml2dot = new SBML2Dot(fileName);

		Context.loadProperties();

		sbml2dot.cloning = Context.getPropertyAsBoolean(Context.CLONING);
		sbml2dot.fileNameSuffix = Context.getProperty(Context.SUFFIX);

		GraphViz.setDotPath("/usr/local/bin/wrongdot");
		
		SBMLDocument sbmlDocument = sbml2dot.getSBMLDocument();

		if (sbmlDocument == null) {
			logger.debug("sbmlDocument == null");
			System.exit(1);
		}

		// writing the dot file
		StringWriter stringWriter = new StringWriter();

		sbml2dot.dotExport(sbmlDocument, new PrintWriter(stringWriter));

		GraphViz graphViz = new GraphViz();
		String dotFileStr = stringWriter.toString();

		// Printing file as a result only in debugging mode
		logger.debug(dotFileStr);

		String dotFileName = fileName.replaceFirst(".xml", ".dot");

		try {
			FileWriter dotFile = new FileWriter(dotFileName);
			dotFile.write(dotFileStr);
			dotFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		graphViz.setImageFormat("dot");
		byte[] dotWithLayoutBytes = graphViz.getGraph(dotFileStr);
		
		if (dotWithLayoutBytes != null) {
		  String dotWithLayout = new String(dotWithLayoutBytes);

		  System.out.println("SBML2Dot : conversion with layout done : " + java.util.Calendar.getInstance().getTimeInMillis());

		  // Do the different exports if needed
		  String exports = "svg png";

		  if (args.length > 1) {
		    exports = args[1];
		  }

		  if (exports != null && exports.trim().length() > 0) {
		    String[] exportArr = exports.split(" "); 

		    // writing the images with graphviz
		    int lastIndexDot = fileName.lastIndexOf(".");

		    String exportFileSuffix = fileName.substring(0, lastIndexDot);

		    System.out.println("SBML2Dot : FILE name suffix = " + exportFileSuffix);
		    logger.debug("Dot export file name suffix = " + exportFileSuffix);

		    for (String export : exportArr) {

		      String imgFileName = exportFileSuffix + "." + export.trim();
		      System.out.println("image file name: " + imgFileName);

		      graphViz.setImageFormat(export);
		      byte[] imgBytes = graphViz.getGraph(dotWithLayout, "-n2");
		      graphViz.writeGraphToFile(imgBytes, imgFileName);

		      System.out.println("SBML2Dot : conversion without layout done : " + java.util.Calendar.getInstance().getTimeInMillis());

		    }
		  }
		}
	}

	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

		if (! (model instanceof SBMLModel)) {
			throw new ReadModelException("SBML2Dot: Input file is not a regular SBML file.");
		}
		
		inputModel = model;
		SBMLDocument sbmlDocument = ((SBMLModel) model).getSBMLDocument();
		StringWriter dotStringWriter = new StringWriter();
		
		try {
			dotExport(sbmlDocument, new PrintWriter(dotStringWriter));
		} catch (SBMLException e) {
			throw new ConversionException(e.getMessage());
		}
		
		String dotFileStr = dotStringWriter.toString();

		// System.out.println(dotFileStr);

		File tmpDotFile;
		try {
			tmpDotFile = File.createTempFile("sbml2dot_", "dot");

			FileWriter dotFile = new FileWriter(tmpDotFile);
			dotFile.write(dotFileStr);
			dotFile.close();
		} catch (Exception e) {
		  throw new ConversionException(e);
		}

		boolean dotAvailable = GraphViz.isDotAvailable();
        String dotWithLayout = dotFileStr; 
		
		if (dotAvailable) {
		  GraphViz graphViz = new GraphViz();

		  graphViz.setImageFormat("dot");
		  byte[] dotWithLayoutBytes = graphViz.getGraph(dotFileStr);

		  dotWithLayout = new String(dotWithLayoutBytes);

		  System.out.println("SBML2Dot : convert : dot with layout = \n" + dotWithLayout);

		  // Do the different exports if needed
		  String exports = options.get("export");
		  if (exports != null && exports.trim().length() > 0) {
		    String[] exportArr = exports.split(" "); 

		    // writing the images with graphviz
		    String fileName = ((SBMLModel) model).getModelFileName();

		    if (fileName == null) {
		      // TODO - create the files from the DotModel class when the modelToFile method is called ?
		      logger.debug("No fileName available to save the export files!");
		      return new DotModel(dotWithLayout);
		    }

		    int lastIndexDot = fileName.lastIndexOf(".");

		    String exportFileSuffix = fileName.substring(0, lastIndexDot);

		    logger.debug("Dot export file name suffix = " + exportFileSuffix);

		    for (String export : exportArr) {

		      String imgFileName = exportFileSuffix + "." + export.trim();
		      logger.info("image file name: " + imgFileName);

		      graphViz.setImageFormat(export);
		      byte[] imgBytes = graphViz.getGraph(dotWithLayout, "-n2");
		      graphViz.writeGraphToFile(imgBytes, imgFileName);
		    }
		  }
		} // TODO - check exception when the dot PATH is invalid
		else {
		  // writing a note at the beginning of the file to say that the layout was not done and how to do it.
		  System.out.println("The Graphviz 'dot' program does not seems to be setup properly, saving the dot file without layout information!");
		  dotWithLayout = "// The Graphviz 'dot' program does not seems to be setup properly, this dot file does not contain layout information\n" 
		      + "// If you want to have layout and being able to export the graph as png or other, you need to install Graphviz from http://www.graphviz.org/.\n"
		      + "// If 'dot' is not installed on the default location ('/usr/bin/dot'), you can specified the path with the java option '-Dsbml2dot.dot.path=/usr/local/bin/dot'\n\n"
		      + dotWithLayout;
		}
		
		return new DotModel(dotWithLayout);
	}

	@Override
	public String getResultExtension() {
		return ".dot";
	}
	
	@Override
	public String getName() {
		return "SBML2Dot";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to Dot";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to Dot";
	}
	
}
