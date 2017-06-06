/*
 * $Id: SBML2SBGNML.java 399 2015-07-13 08:43:45Z pdp10 $
 * $URL: svn+ssh://pdp10@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML.java $
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
package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBGNModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.SbgnUtil;
import org.sbgn.SbgnVersionFinder;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Glyph.Clone;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.SBGNBase;
import org.sbgn.bindings.SBGNBase.Extension;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




/**
 * Enable to convert a SBML model into SBGN-ML format. The layout part is not taken into consideration.
 * This program use libSBGN milestone 1.
 * libSBGN {@link http://sourceforge.net/apps/mediawiki/libsbgn/index.php?title=Main_Page}
 * SBGN-ML {@link http://sourceforge.net/apps/mediawiki/libsbgn/index.php?title=Exchange_Format}
 * 
 * @author jalowcki
 */
public class SBML2SBGNML extends GeneralConverter {

	private static final String SBFCANNO_PREFIX = "sbfcanno";
	public static final String SBFC_ANNO_NAMESPACE = "http://www.sbfc.org/sbfcanno";
	
	private static final int SPECIES_CARDINALITY_MAX = 4;
	private static int SBGNML_MILESTONE = 2;

	// Set a logger for debug message
	private static Logger logger = Logger.getLogger(SBML2SBGNML.class);
	
	private Model sbmlModel;
	private static SBGNUtils sbu = new SBGNUtils("sbgnml");
	private java.util.Map<String, Integer> speciesCardinalityMap = new HashMap<String, Integer>();
	private static Properties pro;
	private static HashMap<Integer, String> SBOCloneMap = new HashMap<Integer, String>(12);
	
	static {		
		pro = SBGNUtils.readProperties();
		
		// We store the clonable entities
		String scm = "simpleCloneMarker";
		SBOCloneMap.put(247, scm);// simple chemical
		SBOCloneMap.put(285, scm);// unspecified entity
		SBOCloneMap.put(405, scm);// perturbing agent
		SBOCloneMap.put(358, scm);// phenotype, beware it is a process
		SBOCloneMap.put(421, scm);// multimer simple chemical
		// Stateful entity pools (class UML diagram 3.1 in the spec) no SBOterm for this one
		String lcm = "labeledCloneMarker";
		SBOCloneMap.put(245, lcm);// macromolecule
		SBOCloneMap.put(253, lcm);// complex
		SBOCloneMap.put(354, lcm);// nucleic acid feature
		SBOCloneMap.put(420, lcm);// multimer of macromolecules
		SBOCloneMap.put(418, lcm);// multimer of complexes
		SBOCloneMap.put(419, lcm);// multimer of naf
		// all logical operators SBO:....237 logical combination (children: and, or , not, xor)
		String ncm = "noneCloneMarker";
		SBOCloneMap.put(237, ncm);

	}
	
	public SBML2SBGNML(){
	}
	
	
	/**
	 * This function tells if the given reaction can be cloned or not as a SBGN process node.
	 * Cloning SBGN PD specifications characteristics are checked:
	 * 			all children of the process class can only be cloned
	 * 			if all reactants and products are cloned then the process nodes is cloned
	 * @param {@link Reaction}
	 * @return {@link Boolean}
	 */
	private boolean canBeCloned(Reaction reaction) {
		
		boolean canBeCloned = false;
		
		Integer sboTerm = reaction.getSBOTerm();
		
		// Retrieving a SBOTerm: if cannot retrieve any SBO term, we try to analyze annotations in order to get one
		if (sboTerm == null) {
			
			// Getting SBGN class with annotations
			String SBGNclass = SBGNUtils.getSBGNClass(reaction);
			sboTerm = 0;
			
			// if the class is in the Properties, we continue
			if (pro.containsValue(SBGNclass)) {

				// looping over all keys in the properties file
				for (Object keyObj : pro.keySet()) {

					String key = (String) keyObj;
					logger.debug("key of properties file = " + key);

					if (SBGNclass == pro.getProperty(key)) {

						// Retrieving the SBO term as an integer.
						sboTerm = SBO.stringToInt(pro.getProperty(key));
						break;
					}
				}
			} else {
				logger.warn("impossible to get SBO term to determine whether it can be cloned or not");
				return false;
			}
		}
		
		if (sboTerm == null || sboTerm == 0)
		{
			return false;
		}
		
		// all processes SBO:....375 and their children can be cloned
		if (SBO.isChildOf(sboTerm, 375)) {
			canBeCloned = true;
		} else {
			return false;
		}
		
		// process duplication implies that all reactants and products can be cloned		
		if (reaction.getNumReactants() > 0) {
			for (SpeciesReference sp : reaction.getListOfReactants()) {
				if (!canBeCloned(sp.getSpeciesInstance())) {
					return false;
				}
			}
		}
		
		if (reaction.getNumProducts() > 0) {
			for (SpeciesReference sp : reaction.getListOfProducts()) {
				if (!canBeCloned(sp.getSpeciesInstance())) {
					return false;
				}
			}
		}

		return canBeCloned;
	}
	
	
	/**
	 * Check if a {@link Species} can be cloned or not.
	 * We check the compliance regarding SBGN class given by the table 3.2 of the SBGN-ML specifications.
	 * We also check cloning relevance by the number of arcs.
	 * @param Species
	 * @return true or false
	 */
	//TODO : check cloning step
	private boolean canBeCloned(Species species) {
		
		boolean hasToBeCloned = false;
		
		Integer specieSBOterm = species.getSBOTerm();// on se base sur le sboterm
		
		// if cannot retrieve any SBO term, we try to analyze annotations in order to get one
		if (specieSBOterm == null) {
			
			// Getting SBGN class with annotations
			String SBGNclass = SBGNUtils.getSBGNClass(species);
			specieSBOterm = 0;
			
			// if the class is in the Properties, we continue
			if (pro.containsValue(SBGNclass)) {
				// looping over all keys in the properties file
				for (; pro.keys().hasMoreElements(); ) {

					String key = (String) pro.keys().nextElement();
					logger.debug("key of properties file = " + key);

					if (SBGNclass == pro.getProperty(key)) {

						// Retrieving the SBO term as an integer.
						specieSBOterm = SBO.stringToInt(pro.getProperty(key));
						break;
					}
				}
			} else {
				// impossible to get SBO term to determine whether it can be cloned or not.
				logger.warn("impossible to get SBO term to determine whether it can be cloned or not");
				return false;
			}
		}
		
		if (specieSBOterm == null)
		{
			return false;
		}

		// if the species has 5 links or more then it may be clonable
		
		if (speciesCardinalityMap.get(species.getId()) == null) {
			speciesCardinalityMap.put(species.getId(), 0);
		}
		
		if (speciesCardinalityMap.get(species.getId()) > SPECIES_CARDINALITY_MAX) {
			hasToBeCloned = true;
		}
		
		if (hasToBeCloned) {
			
			// if is or is a child of a clonable entity
			if (SBOCloneMap.get(specieSBOterm) != null) {
				return true;
			}
			
			// looking if the SBOTerm of the species has a parent in the map
			for (Entry<Integer, String> entry : SBOCloneMap.entrySet()) {
				
				Integer sboTerm = entry.getKey();
				if (SBO.isChildOf(specieSBOterm, sboTerm)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Take a SBMLDocument and build the corresponding SBGN-ML object
	 * @param sbmlDocument
	 * @param outputFile
	 */
	public Sbgn convertSBGNML(SBMLDocument sbmlDocument) throws SBMLException {
		
		// xml and sbgn tags
		Sbgn sbgnObject = new Sbgn();

		// map tags
		Map map = new Map();
		sbgnObject.setMap(map);

		// Reading the SBML document
		try { 
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2SBGN: Input file is not a regular SBML file.");
		}
		
		ListOf<Species> listOfSpecies = sbmlModel.getListOfSpecies();
		ListOf<Reaction> listOfReactions = sbmlModel.getListOfReactions();
		
		// Building the species-cardinality map for cloning
		speciesCardinalityMap = getSpeciesCardinalityMap(listOfReactions);
		
		// Add glyphs
		createGlyphs(listOfSpecies, map);

		// Add process nodes glyphs for reactions
		createGlyphProcessNodes(listOfReactions, map);
		
		// Add arcs
		createArcs(listOfReactions, map);

		return sbgnObject;
	}
	
	
	/**
	 * Return for given complex {@link Species} and {@link ListOf<Species>}, a {@link ListOf<Species>} that corresponds to the hasPart 
	 * resources of the given complex. If the number of hasPart resources does not match the number of species, null is returned.
	 * 
	 * @param {@link Species} species
	 * @param {@link ListOf<Species>} listOfSpecies
	 * @return the {@link ListOf<Species>} complexComponent or null
	 */
	private static ListOf<Species> getComplexComponents(Species species, ListOf<Species> listOfSpecies) {
		
		// Retrieving the whole hasPart annotations of the species
		// Looping over the list of hasPart annotations of the given species
		Integer size = null;
		ListOf<Species> complexComponent = null;

		// Looping over the first CVTerms hasPart of the species; others are only duplicates

		logger.debug("number of cvterms = " + species.getNumCVTerms());
		
		// Check whether the species is a complex
		if (species.filterCVTerms(CVTerm.Qualifier.BQB_HAS_PART).size() <= 0) {
			return null;
		}

		CVTerm cvterm = species.filterCVTerms(CVTerm.Qualifier.BQB_HAS_PART).get(0);

		if (cvterm == null || cvterm.getResources() ==  null){
			logger.debug("cvterm.getResources() ==  null");
			return null;
		}

		// Looping over all resources of a hasPart qualifier
		for (String pattern : cvterm.getResources()) {
			
			logger.debug("the pattern is = " + pattern);

			if (listOfSpecies == null) {
				logger.debug("listOfSpecies == null");
			}
			
			// Looping over the given list of Species
			for (Species sp : listOfSpecies) {
				
				//return a list of cvterms in IS qualifier
				List<String> isCVTerm = sp.getAnnotation().filterCVTerms(CVTerm.Qualifier.BQB_IS, pattern);
				logger.debug("isCVTerm.size() = " + isCVTerm.size());
				if (isCVTerm.size() == 1) {
					logger.debug("in the complex component loop");
					complexComponent = new ListOf<Species> (species.getLevel(), species.getVersion());
					complexComponent.add(sp); // TODO : clone the java object or not ?? : sp.clone()
					size = complexComponent.size();
				}
			}
		}
		
		if (size == null) {
			logger.debug("size == null");
			return null;
		}
		
		// check that the number of hasPart resources is the same as complex component
		if (size != cvterm.getResources().size()) {
			logger.warn("A complex " + species.getName() + " has not its components in the model.");
			return null;
		}
		
		return complexComponent;
	}
	
	
	/**
	 * Return the name of a Species, if not available its id
	 * @param specie
	 * @return displayName
	 */
	public static String getName(NamedSBase specie) {
		
		String displayName = specie.getName();
		
		if (displayName == null || displayName.trim().length() == 0) {
			displayName = specie.getId();
		}
		return displayName;
		
	}
	
	
	/**
	 * Determine if the {@link Species} is a complex or not. Check first the SBOterms then the annotations.
	 * Note that for a complex: SBO:0000253
	 * @param A {@link Species}
	 * @return true or false
	 */
	public static Boolean isAComplex(Species species) {
		
		//checking the SBOterm
		if (species.getSBOTerm() == 253) {
			return true;
		}
		
		//checking the annotations
		if (species.getAnnotation().getNumCVTerms() > 0) {
			
			// loop over the list of CVTerm (bqbiol:xxx or bqmodel:xxx RDF construct)
			for (CVTerm cvTerm : species.getAnnotation().getListOfCVTerms()) {
				
				// Test is the qualifier is bqbiol:hasPart
				if (cvTerm.isBiologicalQualifier() && cvTerm.getBiologicalQualifierType().equals(CVTerm.Qualifier.BQB_HAS_PART)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Creates and returns a new {@link SBMLDocument}
	 * 
	 * @return a {@link SBMLDocument} or null if the document is not a valid SBML file.
	 */
	public SBMLDocument getSBMLDocument(String sbmlFileName) {

		SBMLDocument document = null;
		SBMLReader reader = new SBMLReader();
		try {
			document = reader.readSBML(sbmlFileName);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return document;
	}
	
	
	/**
	 * Return a map of the Species with their cardinality in the SBML file.
	 * This function is used to know which elements will have to be cloned in a graph.
	 * Cardinality can be estimated by looking at each {@link Reaction} in the SBML model. Each time an arc comes from or to a 
	 * node, cardinality is incremented. Therefore a map is being built with each {@link Species} and their cardinality.
	 * @param {@link ListOf<Reaction>} lor
	 * @return idCardTable
	 */
	private static HashMap<String, Integer> getSpeciesCardinalityMap(ListOf<Reaction> lor) {

		HashMap<String, Integer> idCardTable = new HashMap<String, Integer>();
		logger.debug("in getSpeciesCardinalityMap = " + lor.size());
		if (lor.size() != 0) {

			for (Reaction reac : lor) {
				
				ListOf<SpeciesReference> reactant = reac.getListOfReactants();
				ListOf<SpeciesReference> product = reac.getListOfProducts();
				ListOf<ModifierSpeciesReference> modifier = reac.getListOfModifiers();

				for (SpeciesReference sp : reactant) {
					Integer card = idCardTable.get(sp.getSpecies());
					if (card != null) {
						idCardTable.put(sp.getSpecies(), card + 1);
						logger.debug("name = " + sp.getSpecies() + " ## card = " + idCardTable.get(sp.getSpecies()));
					} else {
						idCardTable.put(sp.getSpecies(), 1);
					}
				}
				
				for (SpeciesReference pr : product) {
					Integer card = idCardTable.get(pr.getSpecies());
					if (card != null) {
						idCardTable.put(pr.getSpecies(), card + 1);
						logger.debug("name = " + pr.getSpecies() + " ## card = " + idCardTable.get(pr.getSpecies()));

					} else {
						idCardTable.put(pr.getSpecies(), 1);

					}				
				}
				
				for (ModifierSpeciesReference mod : modifier) {
					Integer card = idCardTable.get(mod.getSpecies());
					if (card != null) {
						idCardTable.put(mod.getSpecies(), card + 1);
						logger.debug("name = " + mod.getSpecies() + " ## card = " + idCardTable.get(mod.getSpecies()));

					} else {
						idCardTable.put(mod.getSpecies(), 1);
					}
				}
			}
		}
		return idCardTable;
	}

	
	/**
	 * Looping over the {@link ListOf<Reaction>} of the SBML model, then foreach {@link Reaction}, an arc is created for each elements 
	 * involved in (reactant, product, modifier). In SBGN-ML, default classes are:
	 * 				for reactant : consumption
	 * 				for product : production
	 * 				for modifier : modulation
	 * 
	 * @param listOfReaction
	 * @param sbgn map
	 * @return arcString
	 */
	private void createArcs(ListOf<Reaction> listOfReaction, Map map) {
		
		// A bit of a hack here: use transient glyphs with ids only to create the arcs
		Glyph hackSource = new Glyph();
		Glyph hackTarget = new Glyph();

		for (Reaction reaction : listOfReaction) {
			
			String glyphProcessId = reaction.getId();
			String glyphProcessIdPort1 = reaction.getId() + ".1";
			String glyphProcessIdPort2 = reaction.getId() + ".2";
			logger.debug("reaction.getID = " + reaction.getId());
			
			// write arcs for reactants
			if (reaction.getNumReactants() > 0) {

				for (SpeciesReference reactant : reaction.getListOfReactants()) {
					
					hackSource = new Glyph();
					hackSource.setId(reactant.getSpecies());
					hackTarget = new Glyph();
					hackTarget.setId(glyphProcessIdPort1);// Here we target the first port but we can encounter layout problems.
					
					createOneArc(reaction, "consumption", hackSource, hackTarget, map);
				}
			}

			if (reaction.getNumProducts() > 0) {
				
				for (SpeciesReference product : reaction.getListOfProducts()) {

					hackSource = new Glyph();
					hackSource.setId(glyphProcessIdPort2);// Here we target the second port but we can encounter layout problems.
					hackTarget = new Glyph();
					hackTarget.setId(product.getSpecies());
					
					createOneArc(reaction, "production", hackSource, hackTarget, map);
				}
			}
				
			if (reaction.getNumModifiers() > 0) {
				
				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
				
					hackSource = new Glyph();
					hackSource.setId(modifier.getSpecies());
					hackTarget = new Glyph();
					hackTarget.setId(glyphProcessId);
					
					createOneArc(reaction, "modulation", hackSource, hackTarget, map);
				}
			}
		}
		return;
	}
	

	/**
	 * Create the glyph sequence for the corresponding to the given {@link ListOf<Reaction>}.
	 * @param {@link ListOf<Reaction>}
	 * @param {@link Map} map
	 * @return glyphs
	 */
	private void createGlyphProcessNodes(ListOf<Reaction> listOfReaction, Map map) {
		
		for (Reaction reaction : listOfReaction) {

			createOneGlyph(reaction, map);
			
		}
		return;
	}
	
	/**
	 * Create sbgn {@link Glyph} from a {@link ListOf<Species} and add them to the given {@link Map}.
	 * In order to be able to write complexes, this function is called recursively.
	 * TODO : Complex components should be determined thanks to web services, like MIRIAM web services or 
	 * Ontology Lookup Services (GO, CHEBI) and Uniprot.
	 * @param listOfSpecies {@link ListOf<Species>}
	 * @param map {@link Map}
	 * @return
	 */
	private void createGlyphs(ListOf<Species> listOfSpecies, Map map) {
		
		for (Species species : listOfSpecies) {

			ListOf<Species> listFromSpecie = getComplexComponents(species, listOfSpecies);
			
			if  (isAComplex(species) && (listFromSpecie != null) && (listFromSpecie.size() > 1)) {
				
				// creating a glyph for complex with id and class
				Glyph glyphOfSpecies = new Glyph();
				glyphOfSpecies.setId(species.getId());
				glyphOfSpecies.setClazz("complex");
				
				// Add the glyph to the map
				map.getGlyph().add(glyphOfSpecies);
				
				// Writing the complex glyphs components, call the function recursively in order to draw other putative complexes
				createGlyphs(listFromSpecie, map);
				
			} else {

				// add the species glyph (a simple glyph)
				createOneGlyph(species, map);
			}
		}
		
		return;
	}
	

	/**
	 * For a given reaction a arc is created. This arc is added to the list of arcs in the given {@link Map}.
	 * Source and target Ids are taken from the given source and target {@link Glyph}.
	 * Coordinates of {@link Start} and {@link End} are set to 0.
	 * Through this method, default classes are given by the user. Default classes have to be chosen very carefully because they will have 
	 * priority over SBML retrieved SBO terms in case of discordance between their meaning in terms of SBGN-ML glyphs.
	 * @param specie : {@link SBase} element which is bound to the arc.
	 * @param defaultSBGNClass : {@link String} the default SBGN-ML class of the arc.
	 * @param source : {@link Glyph} source glyph of the SBGN-ML arc.
	 * @param target : {@link Glyph} target glyph of the SBGN-ML arc.
	 * @param map : {@link Map} map of the {@link Sbgn} current object.
	 * @return
	 */
	private void createOneArc(SBase reaction, String defaultSBGNClass, Glyph source, Glyph target, Map map) {
		
		// create an arc
		Arc arc = new Arc();

		// adding the arc to the list
		map.getArc().add(arc);

		// set class, source-target IDs
		arc.setClazz(sbu.getOutputFromClass(reaction, defaultSBGNClass));
		arc.setTarget(target);
		arc.setSource(source);
		arc.setId(source.getId() + "_" + target.getId());
		
		// set void start tag
		Start s = new Start();
		s.setX(0);
		s.setY(0);
		arc.setStart(s);
		
		// set void end tag
		End e = new End();
		e.setX(0);
		e.setY(0);
		arc.setEnd(e);

		return;
	}
	
	
	/**
	 * Add to a given {@link Glyph} a bbox with all coordinates set to 0.
	 */
	private void createVoidBBox(Glyph g) {

		Bbox bbox = new Bbox();
		bbox.setX(0);
		bbox.setY(0);
		bbox.setH(0);
		bbox.setW(0);
		g.setBbox(bbox);
		return;
	}
	
	
	/**
	 * Take a {@link Species}, then add a {@link Glyph} to the list of {@link Glyph} from the given {@link Map}.
	 * 
	 * 
	 * Glyphs where class=complex call this function recursively in order to draw glyphs within glyphs.
	 * Glyphs will then carry the following tags:
	 * 						a bbox (with y, x, h, w, equal to 0.0)
	 * 						a label (with text attribute) for EPN and phenotype only
	 * Furthermore, glyphs may carry:
	 * 						a clone marker for EPN only, if suitable according to the SBGN specifications and if there are 5 connections 
	 * 							around the {@link Species} at least
	 * 						
	 * @param species {@link SBase}: the species in SBML
	 * @param map {@link Map}
	 * @return
	 */
	private void createOneGlyph(Species species, Map map) {

		// create a glyph
		Glyph glyph = new Glyph();

		
		// set id and class
		glyph.setId(species.getId());
		glyph.setClazz(sbu.getOutputFromClass(species, "unspecified entity"));
		

		// add it to the map
		((Map) map).getGlyph().add(glyph);
		
		/* LABEL TAG
		 * According to the specifications, label tags have to be within the element. For convenience, we define a mandatory bbox for 
		 * compartments and let the mere tag otherwise.
		 * TODO label for cardinality
		 */
		Label lab = new Label();
		lab.setText(getName(species));
		glyph.setLabel(lab);
		if (sbu.getOutputFromClass(species, "unspecified entity").equals("compartment")) {
			
			// ... and add a bbox w/o coordinates
			createVoidBBox(glyph);
		}

		SBML2SBGNML stsML = new SBML2SBGNML();
		
		// CLONE TAG TODO has to duplicate the clone nodes and their arcs
		if (stsML.canBeCloned(species)) {
			Clone c = new Clone();
			glyph.setClone(c);
		}
		
		// mandatory BBOX for the glyph
		createVoidBBox(glyph);

		
		// TODO is it possible to retrieve informations from SBML for auxiliary units tags???
		// STATE VALUE, state tag has to be added only within a glyph, with a bbox

		// adding SBML annotations for SBGNN-ML
		if ( SBGNML_MILESTONE == 2 && species.getAnnotation() != null ) {
			
			// get annotation as a string
			Annotation ann = species.getAnnotation();

			// add the annotation in an Extension object, the annotation will be reformated
			if (!ann.equals("")) {
				try {
					addAnnotationInExtension(glyph, ann);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
	
		return;
	}
	
	
	/**
	 * Create a mere XML schema for an annotation.
	 * 
	 * @param base
	 * @param anno
	 * @throws ParserConfigurationException 
	 */
	private static void addAnnotationInExtension(SBGNBase base, Annotation anno) throws ParserConfigurationException {
		
		// new dom creation
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder constructeur = factory.newDocumentBuilder();
		Document document = constructeur.newDocument();

		// root annotation
		Element annotation = document.createElement(SBFCANNO_PREFIX + ":annotation");
		annotation.setAttribute("xmlns:" + SBFCANNO_PREFIX, SBFC_ANNO_NAMESPACE);
		
		// creating a child for each qualifier
		for (CVTerm cvt : anno.getListOfCVTerms()) {

			// get the name of the qualifier
			String cvtermName = cvt.isBiologicalQualifier() ? cvt.getBiologicalQualifierType().getElementNameEquivalent() :
				cvt.getModelQualifierType().getElementNameEquivalent();
			
			// add the CVTerm as a child
			Element cvterm = document.createElement(SBFCANNO_PREFIX + ":" + cvtermName);
			
			// append this element to the root
			annotation.appendChild(cvterm);
			
			// for each cvterm, add a resource tag corresponding to in which will be uri and url
			for (String urn : cvt.getResources()) {
				
				// create and add an element called resource
				Element resource = document.createElement(SBFCANNO_PREFIX + ":resource");
				resource.setAttribute(SBFCANNO_PREFIX + ":urn", urn);
				// TODO get the url of the corresponding urn if possible
				// resource.setAttribute(SBFCANNO_PREFIX + ":url", );
				cvterm.appendChild(resource);
				
			}
		}
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists alredy
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(annotation);

		// set the Extension for the SBGNBase
		base.setExtension(ex);
		
	}
	
	
	/**
	 * Add an {@link Extension} tag for an {@link SBGNBase} object with a {@link Element} inside. If the {@link Extension} is alredy present 
	 * for the object, a new one is created, the {@link Element} is simply added otherwise.
	 * The String will have to be in a xml structure compliant.
	 * 
	 * @param {@link SBGNbase} base
	 * @param {@link String} elementString
	 */
	private static void addExtensionElement(SBGNBase base, String elementString) {
		
		// ... and add it as extension for the SBGN-ML glyph
		Extension ex = new Extension();

		// if the Extension exists alredy
		if ( base.getExtension() != null ) {
			ex = base.getExtension();
		}
		
		// source: http://www.java2s.com/Code/JavaAPI/org.w3c.dom/DocumentgetDocumentElement.htm
		// prepare a builder factory, details have to be set
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    builderFactory.setNamespaceAware(false);       // Set namespace aware
	    builderFactory.setValidating(false);           // and validating parser features
	    builderFactory.setIgnoringElementContentWhitespace(false); 
		
	    DocumentBuilder builder = null;

		try {
			builder = builderFactory.newDocumentBuilder();  // Create the parser
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}
		Document xmlDoc = null;
		
		try {
			xmlDoc = builder.parse(new InputSource(new StringReader(elementString)));

		} catch(SAXException exception) {
			exception.printStackTrace();

		} catch(IOException exception) {
			exception.printStackTrace();
		}
		
		// finally we have our dom element
		Element e = xmlDoc.getDocumentElement();
		
		// fill the list<Element> of Extension with our dom element
		ex.getAny().add(e);

		// set the Extension for the SBGNBase
		base.setExtension(ex);
		
	}
	
	
	/**
	 * Create a {@link Glyph} and add it to the list of {@link Glyph} from the given {@link Map}.
	 * {@link Glyph}s with an Id and a class are carrying a void {@link Bbox} and two {@link Port}s. They may also have a {@link Clone} object.
	 * @param reaction {@link Reaction}
	 * @param map {@link Map}
	 * @return
	 */
	private void createOneGlyph(Reaction reaction, Map map) {
		
		// create a glyph, set ID and class
		Glyph glyph = new Glyph();
		glyph.setId(reaction.getId());
		glyph.setClazz(sbu.getOutputFromClass(reaction, "process"));
		
		// add the glyph to the map
		map.getGlyph().add(glyph);

		// if a kinetic law is defined
		if (SBGNML_MILESTONE==2 && reaction.getKineticLaw() != null) {

			// get the content mathML if any...
			String math = reaction.getKineticLaw().getMathMLString();

			// ... and put it in an Extension tag
			addExtensionElement(glyph, math);
		}
		
		// adding SBML annotations for SBGNN-ML
		if ( SBGNML_MILESTONE==2 && reaction.getAnnotation() != null ) {
			
			// get annotation as a string
			Annotation ann = reaction.getAnnotation();
			
			// add the annotation in an Extension object
			try {
				addAnnotationInExtension(glyph, ann);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		// CLONE TAG TODO check that with models
		if (canBeCloned(reaction)) {
			Clone c = new Clone();
			glyph.setClone(c);
		}	
		
		// mandatory BBOX for the glyph
		createVoidBBox(glyph);
		
		// 2 PORTS (2 lugs) for process nodes
		Port p1 = new Port();
		p1.setId(reaction.getId() + ".1");
		p1.setX(0);
		p1.setY(0);
		glyph.getPort().add(p1);
		
		Port p2 = new Port();
		p2.setId(reaction.getId() + ".2");
		p2.setX(0);
		p2.setY(0);
		glyph.getPort().add(p2);

		return;
	}
	
	
	/**
	 * Using jSBML, the given SBML file is read and loaded into objects. Then using libSBGN, the entire SBGN structure is built and 
	 * a {@link Sbgn} object is created. This object will enable the final conversion into a SBGN-ML file.
	 * @param the SBML filename
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
		
		BasicConfigurator.configure();
		
		// taking the filename in parameter
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.SBML2SBGN <SBML filename> [SBGNML milestone for output file]");
			return;
		}
		
		if (args.length == 2) {
			SBGNML_MILESTONE = Integer.parseInt(args[1]);
		}
		
		String sbmlFileNameInput = args[0];
		
		
		SBML2SBGNML sbml = new SBML2SBGNML();
				
		// reading SBML file input and creating SBMLDocument
		SBMLDocument sbmlDocument = sbml.getSBMLDocument(sbmlFileNameInput);

		if (sbmlDocument == null) {
			System.exit(1);
		}
		
		//Building the output filename path and the output filename
		String outputFilePath = sbmlFileNameInput.replaceAll("[_A-Za-z0-9-]+.xml$", "");
		String outputFile = outputFilePath + "SBGN-ML_" + sbmlFileNameInput.replaceAll("^.*/", "");
		
		//Converting SBML and building SGBN-ML object
		Sbgn sbgnObject = sbml.convertSBGNML(sbmlDocument);
		
		// writing the SBGN file to disk
		File f = new File(outputFile);
		try {
			SbgnUtil.writeToFile(sbgnObject, f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		System.out.println("output file at: " + outputFile);
		System.out.println("conversion into SBGN-ML done");

		File f2 = new File(outputFile);

		try 
		{
			SbgnUtil.readFromFile(f2);
		}
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
		
		// Check file version
		int version = SbgnVersionFinder.getVersion(f2);
		
		System.out.println("The version of the created SBGN-ML file is '" + version + "'");
		
		if (version == 1)
		{
			f2 = File.createTempFile(f2.getName(), ".sbgn");
			System.out.println ("Converted to " + f2);
			// ConvertMilestone1to2.convert (file, targetFile);
		}
	}


	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

		try {
			inputModel = model;
			SBMLDocument doc = ((SBMLModel) model).getSBMLDocument();
			Sbgn sbgnObj = convertSBGNML(doc); // TODO : maybe use another
												// function
			SBGNModel outputModel = new SBGNModel(sbgnObj);
			return outputModel;
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new ConversionException(e.getMessage());
		}
	}


	@Override
	public String getResultExtension() {
		return ".sbgn";
	}
	
	@Override
	public String getName() {
		return "SBML2SBGNML";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}
	
	
	
}
