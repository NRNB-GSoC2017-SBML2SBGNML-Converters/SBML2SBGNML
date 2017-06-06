/*
 * $Id: SBML2BioPAX.java 598 2016-01-28 11:27:47Z pdp10 $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2biopax/SBML2BioPAX.java $
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

package org.sbfc.converter.sbml2biopax;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.identifiers.registry.RegistryLocalProvider;
import org.identifiers.registry.RegistryUtilities;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.BioPAXModel;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbml.sbmlannotation.MiriamAnnotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;




/**
 * Convert an SBML file into a BioPax owl file.
 * 
 * The package uk.ac.ebi.compneur.sbmlannotation is not used at the moment !!! Work in progress from Arnaud :-)
 * 
 * @author Arnaud Henry
 * @author Nicolas Rodriguez
 * @author Camille Laibe
 * 
 * @version 2.3
 * 
 */

// TODO : check if we could use ProteinReference and equivalent stuff as well

// TODO : add a check to see if there is no Reaction and Species, then don't generate a biopax file or just with a comment about the problem

// TODO : have a look at RelationXRef


public abstract class SBML2BioPAX extends GeneralConverter {
	
	protected String PHYSICAL_ENTITY = "PhysicalEntity";
	protected String CELLULAR_LOCATION_VOCABULARY = "CellularLocationVocabulary";
	protected String PROVENANCE_CLASS_NAME = "Provenance";
	protected String BIO_SOURCE = "BioSource";
	protected String PATHWAY_CLASS_NAME = "Pathway";	
	protected String CONTROL = "Control";
	protected String BIOCHEMICAL_REACTION = "BiochemicalReaction";
	protected String COMPLEX_DIS_ASSEMBLY = "ComplexDisAssembly";
	protected String COMPLEX_ASSEMBLY = "ComplexAssembly";
	protected String TRANSPORT = "Transport";
	protected String LEFT_OF_REACTION = "LEFT";
	protected String RIGHT_OF_REACTION = "RIGHT";
	protected String PUBLICATION_XREF_CLASS_NAME = "PublicationXref";
	protected String UNIFICATION_XREF_CLASS_NAME = "UnificationXref";
	protected String PHYSICAL_ENTITY_PARTICIPANT = "PhysicalEntity";

	protected String INTERACTION = "Interaction";
	protected String CATALYSIS = "Catalysis";

	
	protected static final String EC_CODE_URI = "http://identifiers.org/ec-code"; // "urn:miriam:ec-code" 
	protected static final String BIOMODELS_URI = "http://identifiers.org/biomodels.db/"; // "urn:miriam:biomodels.db"; // 

	public static String VERSION = "3.1";
	
	// make this configurable in a configuration file ??
	public static final String MIRIAM_WS_URL = "http://www.ebi.ac.uk/miriamws/main/MiriamWebServices";


	
	// creation of the link to the web services
	public static RegistryLocalProvider link;
	
	public static HashMap<String, String> officialURIs = new HashMap<String, String>();
	public static HashMap<String, String> officialNames = new HashMap<String, String>(); 
		
	static {
		link = new RegistryLocalProvider();
		// link.setAddress(MIRIAM_WS_URL);
	}

	protected int biopaxLevel = 3;

	
	/**
	 * <b>Constructor SBML2BioPAX.</b><br/>
	 * Main method of the biological model export from <a href="http://sbml.org/"><b>SBML</b></a> (Systems Biology Markup Language)
	 *  to <a href="http://www.biopax.org/"><b>BioPAX</b></a> (Biological PAthway eXchange format).
	 * 
	 * Provide it a file when you call the program with the command 
	 * <code><pre>java uk.ac.ebi.compneur.sbml2biopax.SBML2BioPAX &lt;SBMLfile&gt;</code></pre>
	 * 
	 * JSBML is used to read and check the SBML file provided.<br/>
	 * 
	 * Create the SBMLDocument object and start the creation of the Ontological BioPAX model.<br/>
	 * 
	 * Save the BioPAX file in the same file path and name that the input file, but replace the extension by .owl.
	 * 
	 * @param theFile Path of the SBML file to export
	 */
	public SBML2BioPAX(){
		super();
	}

	/**
	 * Converts a SBML model into a BioPax Model.
	 * <p>
	 * Use JSBML to read the SBML model, and Paxtools to write the BioPax model in OWL.<br/>
	 * The conversion is composed of different parts:<br/> <ul>
	 * <li/>fill the Maps coming from the configuration file. They will be use to determine the specific Biopax class of the translated SBML elements.<br/>
	 * <li/>'model' translation into a BioPax pathway: source on BioModels database, taxonomy, publication references<br/>
	 * <li/>compartments treatment<br/>
	 * <li/>species treatment (following the BioPAX classes: complex, dna, rna, protein, smallMolecule).
	 * The Algorithm take care about the constraint. For example a annotation  about KEGG will be typing in a smallMolecule class.
	 * But the annotation "http://www.genome.jp/kegg/compound/#C00039" describe the Deoxyribonucleotide.
	 * A second loop replace the term in dna.<br/>
	 * The Ensembl annotation is special, due to the two first terms of the identifier indicating the type dna, rna or protein of the species.
	 * ex: http://www.ensembl.org/#ENSRNOG -> dna ; ENSRNOT -> rna ; ENSRNOP -> protein
	 * <li/>reaction parsing, and creation of the physicalInteraction subclasses. If the reaction contain one or several modifier, 
	 * they are translate in a control or a catalysis (if contain a EC Code). The reactants and products part are translated in biochemicalReaction as default value. 
	 * If they contain several compartment and annotations for the same species, the conversion is change for a transport.
	 * If the number of species between the reactant and product is different and the annotation correspond to a complex, the conversion is change to a complexAssembly<br/></ul>
	 * @param sbmlModel SBML object create with the path provide to the constructor
	 * @return biopaxModel BioPAX ontology corresponding to the SBML exported
	 */
	public BioPAXModel biopaxexport(SBMLModel sbmlmodel) throws SBMLException
	{
		//Mapping DB URI with the BioPAX subclass of physicalEntity
		Hashtable<String, String> annotationSpecies    = new Hashtable<String, String>();
		//Fill the MAP of publication, {key: URI_database_Publication; value:official_Name}
		Hashtable<String, String> publicationModel     = new Hashtable<String, String>();	
		//Fill the MAP of annotation database about the species in SBML, {key: URI_database_Species; value:official_Name}
		Hashtable<String, String> speciesAnnotationMap = new Hashtable<String, String>();
		/*Get all the URI for taxonomy*/
		Hashtable<String, String> taxonomyMap          = new Hashtable<String, String>();

		// Map of the created annotations to be sure that they are unique
		// The key is the identifiers.org URI, the value is the biopax class instance
		Hashtable<String, BioPAXElement> annotationXMLId = new Hashtable<String, BioPAXElement>();
		
		int nbDatasource = 0;
		int nbBiosource = 0;
		boolean biomodelsCommentAdded = false;
		
		/*Fill the Maps and List for the annotation parameter*/
		SBML2BioPAXannotationParameter.parseSBML2BioPAXConfigFile(annotationSpecies, publicationModel, speciesAnnotationMap, taxonomyMap);
		
		/*Creates the model Factory*/
		BioPAXFactory bioPAXFactory = getBioPaxFactory(); 
		BioPAXFactory elementFactory = bioPAXFactory;
		
		/*Creates the new BioPAX Model*/
		BioPAXModel bioPaxModelGen = new BioPAXModel(bioPAXFactory.createModel());
		
		Model bioPaxModel = bioPaxModelGen.getModel();
		
		org.sbml.jsbml.Model sbmlModel = sbmlmodel.getModel();
		
		
		
		if(sbmlModel == null) {
			throw new SBMLException("SBML2BioPAX: Input file is not a regular SBML file.");
		}
		
		
		/**
		 * Model element analysis.
		 * 
		 * We will create the pathway BioPax Individual.
		 * 
		 * */
		String idmodel = new String();

		if (sbmlModel.isSetId()) {
			idmodel = sbmlModel.getId();
		} else if (sbmlModel.isSetName()) {
			idmodel = sbmlModel.getName();
		} else {
			idmodel = "BioModel";
		}
		
		//Creating the Pathway
		BioPAXElement pathway = elementFactory.create(PATHWAY_CLASS_NAME, idmodel);
		
		// Comment BioPAX date creation (today)
		addComment(pathway, "This BioPAX Level" + biopaxLevel  + " file was automatically generated on " + DateFormat.getDateInstance().format(new Date()) + 
				" by SBML2BioPAX-" + VERSION + ", BioModels.net, EMBL-EBI.");
		
		//Adding the pathway to the model
		bioPaxModel.add(pathway);
		
		//Pathway annotation

		//External references		
		for (CVTerm cvterm : sbmlModel.getAnnotation().getListOfCVTerms())
		{
			for (String annotationURI : cvterm.getResources()) // The qualifier is ignored at the moment in the biopax export
			{
				MiriamAnnotation miriamAnnotation = parseMiriamAnnotation(updatedAnnotation(annotationURI));
				
				if (miriamAnnotation != null) // it is a recognized URI
				{
					String annotationDB = miriamAnnotation.getUri(); // ex: http://identifiers.org/pubmed
					String annotationIdentifier = miriamAnnotation.getId();// ex: 10659856
					
					String rdfId = miriamAnnotation.getIdentifiers_orgURI();
			
					if (rdfId == null)
					{
						debug("Could not create a proper miriamAnnotation for " + annotationURI);
						continue;
					}
					
					if (publicationModel.containsKey(annotationDB)) // This is a publication annotation
					{
						// TODO : specific thing about the publication
						
						String annotationDBname =   publicationModel.get(annotationDB);
						
						// check that the annotationDBnameplusIdentifier is unique and don't add it several times
						BioPAXElement xref = annotationXMLId.get(rdfId); 

						if (xref == null) 
						{
							//Creating the publication xref
							xref = elementFactory.create(PUBLICATION_XREF_CLASS_NAME, rdfId);
							setXrefDb(xref, annotationDBname);
							setXrefId(xref, annotationIdentifier);
							
							annotationXMLId.put(rdfId, xref); // filling the Map

							//Adding the xref to the model
							bioPaxModel.add(xref);						
						}
						
						//Adding the xref to the pathway
						addXref(pathway, xref);
					}
					else if (taxonomyMap.containsKey(annotationDB)) // Taxonomy annotation
					{
						BioPAXElement xref = annotationXMLId.get(rdfId); 

						if (xref == null) 
						{
							//Creating the unification xref
							xref = elementFactory.create(UNIFICATION_XREF_CLASS_NAME, rdfId);
							setXrefId(xref, annotationIdentifier);
							setXrefDb(xref, taxonomyMap.get(annotationDB));
							
							annotationXMLId.put(rdfId, xref); // filling the Map

							//Adding the xref to the model
							bioPaxModel.add(xref);
						}
						

						//Creating the bioSource element
						nbBiosource++;
						BioPAXElement biosource = elementFactory.create(BIO_SOURCE, "biosource_" + nbBiosource);
						// biosource.setDisplayName("bioSource");
						
						addXref(biosource, xref);

						//Adding the bioSource to the pathway
						setOrganismToPathway(pathway, biosource);

						//Adding the biosource to the model
						bioPaxModel.add(biosource);
					}
					else // Any other annotation, not publication and not taxonomy, we create a UnificationXref
					{
						//all the other externals references BioModels, GO, KEGGpathway
						// if (speciesAnnotationMap.containsKey(annotationDB)){ // Don't think this test if necessary anymore

						BioPAXElement xref = annotationXMLId.get(rdfId); 

						if (xref == null) 
						{
							//Creating the UnificationXref xref												
							xref = elementFactory.create(UNIFICATION_XREF_CLASS_NAME, rdfId);
							setXrefId(xref, annotationIdentifier);
							
							String db = speciesAnnotationMap.get(annotationDB);
							
							if (db == null) 
							{
								debug("The database " + annotationDB + " is not in the speciesAnnotation Map");
								
								db = getDatatypeName(annotationDB);								
							}
							
							setXrefDb(xref, db);
							
							annotationXMLId.put(rdfId, xref); // filling the Map

							//adding xref to the model
							bioPaxModel.add(xref);
						}

						//Adding the xref to the pathway
						addXref(pathway, xref);
												
						// dataSource: the direct link to the original model
						if (annotationDB.equals(BIOMODELS_URI) && cvterm.isModelQualifier() 
								&& cvterm.getModelQualifierType().equals(CVTerm.Qualifier.BQM_IS))
						{  
							// TODO : add Biomodels Publication ??
							// TODO : For the other biomodels isDerivedFrom we should have a look at the relationXref

							// TODO : this relationXref might more appropriate for the qualifier other than is
							
							if (!biomodelsCommentAdded)
							{
								addComment(pathway, "The original model, '" + sbmlModel.getId() + "', was published in BioModels Database (http://www.ebi.ac.uk/biomodels/).");
								biomodelsCommentAdded = true;
							}
							
							//Creating the dataSource element
							nbDatasource++;
			
							BioPAXElement datasource = elementFactory.create(PROVENANCE_CLASS_NAME, "datasource_" + nbDatasource);
							addName(datasource, "BioModels Database");							
							setDisplayName(datasource, "BioModels Database");
							
							addXref(datasource, xref);

							//Adding the datasource to the model and to the pathway
							bioPaxModel.add(datasource);
							addDataSource(pathway, datasource);
						}
					}
				}
				else 
				{
					debug("Could not create a proper miriamAnnotation for " + annotationURI);
					continue;
				}				
			}
		}
		
			// TODO : put the VCard in the BioPax file ??
			
			//VCARD annotation
//			if (annotation.contains("<vCard:")){
//			String annotationVCARD = annotation.substring(annotation.indexOf("<vCard:"));
//			String lastpart = annotationVCARD.substring(annotationVCARD.lastIndexOf("</vCard:"));
//			int lastmarkup = annotationVCARD.lastIndexOf("</vCard:")+lastpart.indexOf('>')+1;
//			annotationVCARD = annotationVCARD.substring(0, lastmarkup);
//			Resource vcard = null;
//			String personURI = new String();
//			if (annotationVCARD.contains("vCard:Family")){
//			String family = annotationVCARD.substring(annotationVCARD.indexOf("vCard:Family")+13);
//			family = family.substring(0,family.indexOf("</vCard:Family"));
//			personURI = family;
			
//			vcard = biopaxModel.createResource(personURI);
//			vcard.addProperty(VCARD.Family, family);
//			}
//			if (annotationVCARD.contains("vCard:Given")){
//			String given = annotationVCARD.substring(annotationVCARD.indexOf("vCard:Given")+12);
//			given = given.substring(0, given.indexOf("</vCard:Given"));
//			vcard.addProperty(VCARD.Given, given);
//			}
//			if (annotationVCARD.contains("vCard:EMAIL")){
//			String email = annotationVCARD.substring(annotationVCARD.indexOf("vCard:EMAIL")+12);
//			email = email.substring(0, email.indexOf("</vCard:EMAIL"));
//			vcard.addProperty(VCARD.EMAIL, email);
//			}
//			if (annotationVCARD.contains("vCard:Orgname")){
//			String orgname = annotationVCARD.substring(annotationVCARD.indexOf("vCard:Orgname")+14);
//			orgname = orgname.substring(0, orgname.indexOf("</vCard:Orgname"));
//			vcard.addProperty(VCARD.Orgname, orgname);
//			}

			//// BioPAXtool.addBiopaxLinkedProperty("COMMENT", personURI, sbmlModel.getId(), biopaxModel);
//			Individual pathwayind = biopaxModel.getIndividual(namespaceString+sbmlModel.getId());
//			Property vcardprop = biopaxModel.getProperty(biopaxString+"COMMENT");
//			Resource res = biopaxModel.getResource(namespaceString+personURI);
//			pathwayind.addProperty(vcardprop,res);
//			pathwayind.addProperty(vcardprop,"Model creator");
//			}
		


		/**
		 * Compartments
		 * 
		 * 
		 * 
		 * */
		for (Compartment compartment : sbmlModel.getListOfCompartments()) 
		{
			//Creating the openControlledVocabulary
			BioPAXElement vocab = elementFactory.create(CELLULAR_LOCATION_VOCABULARY, compartment.getId());
			
			//Adding the openControlledVocabulary to the model
			bioPaxModel.add(vocab);

			if (compartment.isSetName())
			{				
				//Adding the compartment name to the openControlledVocabulary
				addTerm(vocab, compartment.getName());				
			}
			else 
			{
				//Adding the compartment id to the openControlledVocabulary
				addTerm(vocab, compartment.getId());
			}
			
			//Compartment annotation
			for (CVTerm cvterm : compartment.getAnnotation().getListOfCVTerms())
			{
				for (String annotation : cvterm.getResources()) 
				{				
					MiriamAnnotation miriamAnnotation = parseMiriamAnnotation(updatedAnnotation(annotation));
						
					if (miriamAnnotation == null) 
					{
						debug("Could not create a proper miriamAnnotation for " + annotation);
						continue;
					}
					
					String annotationDB = miriamAnnotation.getUri();
					String annotationIdentifier = miriamAnnotation.getId();
					String rdfId = miriamAnnotation.getIdentifiers_orgURI();
						
					if (speciesAnnotationMap.containsKey(annotationDB))
					{
						annotationDB =   speciesAnnotationMap.get(annotationDB);
					}
					else 
					{
						debug("The database " + annotationDB + " is not in the speciesAnnotation Map");
						annotationDB = getDatatypeName(annotationDB);
					}

					BioPAXElement xref = annotationXMLId.get(rdfId); 

					if (xref == null) 
					{
						// Creating the xref
						xref = elementFactory.create(UNIFICATION_XREF_CLASS_NAME, rdfId);
						setXrefId(xref, annotationIdentifier);
						setXrefDb(xref, annotationDB);
						
						annotationXMLId.put(rdfId, xref);
					}
					
					//Adding the xref to the openControlledVocabulary
					addXref(vocab, xref);
					
					//adding xref to the model
					if(bioPaxModel.getByID(rdfId) == null) 
					{
						bioPaxModel.add(xref);
					}

				}
			}
		}
		
		
		/**  
		 * Species
		 * 
		 */		
		// System.out.println("SBML2BioPAX : URI Map : \n" + officialURIs);
		// System.out.println("\n\nSBML2BioPAX : annotationSpeciesMap : \n" + annotationSpecies);
		
		for (Species species : sbmlModel.getListOfSpecies()) 
		{
			String physicalEntityType = PHYSICAL_ENTITY; // default value, will change depending of the annotations
			boolean isAComplex = false; // true if the annotation contain bqbiol:hasPart or reference to BIND database
			
			ArrayList<MiriamAnnotation> speciesAnnotationList = new ArrayList<MiriamAnnotation>();

			// WARNING : if two annotations are contradictory (the last is taken)

			for (CVTerm cvterm : species.getAnnotation().getListOfCVTerms())
			{
				Qualifier cvTermQualifier = Qualifier.BQB_UNKNOWN;
				
				if (cvterm.isBiologicalQualifier()) 
				{
					cvTermQualifier = cvterm.getBiologicalQualifierType();
				}
			
				if (cvTermQualifier.equals(Qualifier.BQB_HAS_PART))
				{                                                                                             
					isAComplex = true;                                                                                                                     
				}
				
				for (String annotation : cvterm.getResources())
				{
					// here we have to be sure to normalize to the URIs used in the maps (identifiers.org URIs)
					MiriamAnnotation miriamAnnotation = parseMiriamAnnotation(updatedAnnotation(annotation));

					if (miriamAnnotation == null) 
					{
						debug("Could not create a proper miriamAnnotation for " + annotation);
						continue;
					}
					
					String annotationDB = miriamAnnotation.getUri();
					String annotationTestEnsembl = "";
					String annotationTestEnsembl2 = "";
					speciesAnnotationList.add(miriamAnnotation);

					// System.out.println("SBML2BioPax : miriamAnnotation = " + annotationDB + ", " + miriamAnnotation.getId());

					// constraint on a database which have yet an other entry
					// ex:Chebi (small molecule DB) contain DNA and RNA and Ensembl
					String officialAnnotation = miriamAnnotation.getFullURI();

					if ((annotationDB.length() + 8) < officialAnnotation.length()){
						annotationTestEnsembl = officialAnnotation.substring(0, officialAnnotation.lastIndexOf("/") + 8);
						annotationTestEnsembl2 = officialAnnotation.substring(0, officialAnnotation.lastIndexOf("/") + 5);
						
						// TODO : we need to use a regexp to be able to cover all the possible ensembl IDs
					}

					// System.out.println("SBML2BioPax : species annotationDB = " + annotationDB);

					if (annotationSpecies.containsKey(annotationDB)){

						if ((annotationSpecies.get(annotationDB)).equals("complex")){
							isAComplex = true;
						}

						physicalEntityType = annotationSpecies.get(annotationDB);
						
						// System.out.println("SBML2BioPax : species physicalEntityType = " + physicalEntityType);

						if (annotationSpecies.containsKey(officialAnnotation)){
							physicalEntityType =   annotationSpecies.get(officialAnnotation);
							// System.out.println("SBML2BioPax : species Constraint physicalEntityType = " + physicalEntityType);
						}
					}
					else 
					{
						//Test for Ensembl annotation, check the 7 first characters of the identifier
						if (annotationSpecies.containsKey(annotationTestEnsembl) 
								|| annotationSpecies.containsKey(annotationTestEnsembl2))
						{

							if (annotationSpecies.containsKey(annotationTestEnsembl))
							{
								physicalEntityType =   annotationSpecies.get(annotationTestEnsembl);
							}
							else 
							{
								physicalEntityType =   annotationSpecies.get(annotationTestEnsembl2);
							}

							 // System.out.println("SBML2BioPax : Ensembl : annotation = " + officialAnnotation);
							 // System.out.println("SBML2BioPax : Ensembl : physicalEntityType = " + physicalEntityType + "\n");
						}
						else
						{
							//constraint on a database which have not already an other entry
							if (annotationSpecies.containsKey(officialAnnotation)){
								physicalEntityType =   annotationSpecies.get(officialAnnotation);
							}
						}
					}
				}
				if (isAComplex){
					physicalEntityType = "Complex";
				}
			}
			
			if (biopaxLevel == 2)
			{
				// Putting the first letter to lower case for BioPax Level 2 class name
				physicalEntityType = physicalEntityType.substring(0, 1).toLowerCase() + physicalEntityType.substring(1);
			}
			
			// System.out.println("PhysicalEntity type : " + physicalEntityType);
			//Creating the bioPaxElement corresponding to the physicalEntityType
			BioPAXElement physicalEntity = elementFactory.create(physicalEntityType, species.getId());
			
			if (species.isSetName())
			{
				//Setting the name
				setDisplayName(physicalEntity, species.getName());
				addName(physicalEntity, species.getName());
			}
			else 
			{
				//Setting the id as name
				setDisplayName(physicalEntity, species.getId());
			}
			
			for (int l = 0; l < speciesAnnotationList.size(); l++) 
			{
				MiriamAnnotation miriamAnno = speciesAnnotationList.get(l);				
				String rdfId = miriamAnno.getIdentifiers_orgURI();
				
				if (rdfId == null)
				{
					debug("Could not create a proper miriamAnnotation for '" + speciesAnnotationList.get(l).getUri() 
							+ "' : '" + speciesAnnotationList.get(l).getId());
					continue;
				}
				
				BioPAXElement xref = annotationXMLId.get(rdfId);
				
				if (xref == null) 
				{
					//Creating the unification xref
					xref = elementFactory.create(UNIFICATION_XREF_CLASS_NAME, rdfId);
					setXrefId(xref, miriamAnno.getId());

					setXrefDb(xref, officialNames.get(miriamAnno.getUri()));
					
					annotationXMLId.put(rdfId, xref);
				}
				
				//Adding the xref to the physicalEntity
				addXref(physicalEntity, xref);
				
				// Adding the xref to the model if not already there
				if(bioPaxModel.getByID(rdfId) == null) 
				{
					bioPaxModel.add(xref);
				}
				
			}
			
			//Adding the physicalEntity to the model
			bioPaxModel.add(physicalEntity);
		}
		


		/**
		 * Reactions
		 * 
		 * */
		//Comment: -An EC-code can be added for the biochemicalReactions
		for (Reaction reaction : sbmlModel.getListOfReactions())
		{		
			String conversionId = "conversion_" + reaction.getId();
			
			// TODO changed from ControlType to Interaction
			//String controlType = CONTROL;  //Default value, if annotation contain ECnumber -> catalysis
			String interactionType = INTERACTION;

			//Recovering the reaction Type
			String reactionType = findReactionType(reaction, bioPaxModel);

			//Creating the new biochemicalReaction
			BioPAXElement bioReaction;

			//BioPax has only one Class for complexAssembly and complexDisAssembly
			if (reactionType.equals(COMPLEX_DIS_ASSEMBLY) || reactionType.equals(COMPLEX_ASSEMBLY))
			{
				bioReaction = elementFactory.create(COMPLEX_ASSEMBLY, conversionId);
				/*Usually complexAssembly and complexDisAssembly are spontaneous
				 * Let's assume so and indicate if the reaction is L-R or R-L
				 */
				setSpontaneous(bioReaction, true);
				
				if(reactionType.equals(COMPLEX_ASSEMBLY))
				{
					setConversionDirection(bioReaction, ConversionDirectionType.LEFT_TO_RIGHT);
				}
				else 
				{
					setConversionDirection(bioReaction, ConversionDirectionType.RIGHT_TO_LEFT);
				}
			}
			else
			{
				bioReaction = elementFactory.create(reactionType, conversionId);
			}

			if (reaction.isSetName())
			{
				//Setting the bioReaction name
				setDisplayName(bioReaction, reaction.getName());
				addName(bioReaction, reaction.getName());
			}
			else 
			{
				//Setting the bioReaction id as name
				setDisplayName(bioReaction, reaction.getId());
			}
			
			//Reaction annotations
			for (CVTerm cvterm : reaction.getAnnotation().getListOfCVTerms())
			{
				for (String annotation : cvterm.getResources())
				{
					MiriamAnnotation miriamAnnotation = parseMiriamAnnotation(annotation);

					if (miriamAnnotation == null) 
					{
						debug("Could not create a proper miriamAnnotation for " + annotation);
						continue;
					}
					
					String annotationDB = miriamAnnotation.getUri();
					String annotationIdentifier = miriamAnnotation.getId();
					String rdfId = miriamAnnotation.getIdentifiers_orgURI();
					String datatypeName = officialNames.get(annotationDB);
					
					BioPAXElement xref = annotationXMLId.get(rdfId);
			
					if (datatypeName == null)
					{
						datatypeName = getDatatypeName(annotationDB);
						officialNames.put(annotationDB, datatypeName);
					}
					
					if (xref == null) 
					{
						//Creating the unification xref
						xref = elementFactory.create(UNIFICATION_XREF_CLASS_NAME, rdfId);
						setXrefId(xref, annotationIdentifier);
						setXrefDb(xref, datatypeName);
												
						annotationXMLId.put(rdfId, xref);
					}
					
					//Adding the xref to the bioReaction
					addXref(bioReaction, xref);

					//Adding xref to the model if necessary
					if(bioPaxModel.getByID(rdfId) == null) 
					{
						bioPaxModel.add(xref);
					}

					String ECcodeName = officialNames.get(EC_CODE_URI);


					// TODO REPLACED WITH data field
					if (datatypeName.equals(ECcodeName)){
						interactionType = CATALYSIS;
					}
				}
			}

			//listOfReactant
			for (int i = 0; i < reaction.getReactantCount(); i++) 
			{
				addReactant(bioPaxModel, bioReaction, elementFactory, reaction.getReactant(i), i);
			}

			//listOfProduct
			for (int i = 0; i < reaction.getProductCount(); i++) 
			{
				addProduct(bioPaxModel, bioReaction, elementFactory, reaction.getProduct(i), i);
			}

			//If we have modifiers we prepare to add them to the bioReaction
			
			if (reaction.getModifierCount() > 0) 
			{
//				String controlId = "control_"+reaction.getId();
//
//				Set<? extends BioPAXElement> setOfcontrol = getListOfControl(bioReaction);
				
				String interactionId = "interaction_"+reaction.getId();

				Set<? extends BioPAXElement> setOfInteraction = getListOfControl(bioReaction);

				//listOfModifier
				for (int i = 0; i < reaction.getModifierCount(); i++) 
				{			
					Species speciesInstance = reaction.getModifier(i).getSpeciesInstance();			
					
					// creating a new interaction
					BioPAXElement reacInteraction;

					
					if (SBO.isInhibitor(reaction.getSBOTerm()) || SBO.isChildOf(reaction.getSBOTerm(), 459)) 
					{
						reacInteraction = elementFactory.create(CATALYSIS, interactionId + "_" + speciesInstance.getId() + "_" + i);
					} else
					{
						reacInteraction = elementFactory.create(INTERACTION, interactionId + "_" + speciesInstance.getId() + "_" + i);
					}
					
					
					//Creating new PEP (only for level 2)
					if (reacInteraction instanceof Control) 
					{
						// TODO should this use controlId ?? 
						BioPAXElement newPEP = elementFactory.create(PHYSICAL_ENTITY_PARTICIPANT, "PEP" + i + "_" + interactionId + speciesInstance.getId());
	
						setDisplayName(newPEP, speciesInstance.getId());
						
						setCellularLocation(newPEP, bioPaxModel.getByID(speciesInstance.getCompartment()));
	
						//Setting the Physical Entity to the PEP
						addMemberPhysicalEntity(newPEP, bioPaxModel.getByID(speciesInstance.getId()));
	
						//Adding controller to the control
						addController(reacInteraction, newPEP);
						addControlled(reacInteraction, bioReaction);
						bioPaxModel.add(newPEP);
					} 

					//Setting Interaction Type
					setInteractionType(reacInteraction, reaction);

					//Adding interaction to the reaction
					addControl(setOfInteraction, reacInteraction);

					bioPaxModel.add(reacInteraction);

				}
			}
			
			addPathwayComponent(pathway, bioReaction);
			
			bioPaxModel.add(bioReaction);
		}

		return bioPaxModelGen;//return a complete OWL model
	}

	

	/**
	 * Updates the given annotation to the latest official URI.
	 * <br/>If miriam does not recognize the URI, the original,
	 * unmodified URI is returned.
	 * 
	 * @param annotationURI
	 * @return the updated annotation URI.
	 */
	private String updatedAnnotation(String annotationURI) 
	{
		String updateAnnotation = link.getMiriamURI(annotationURI);
		
		if (updateAnnotation != null)
		{
			return updateAnnotation;
		}
		
		System.out.println("Annotation '" + annotationURI + "' was not recognized by miriam !!");
		
		return annotationURI;
	}

	
	protected void setInteractionType(BioPAXElement reacInteraction, Reaction reaction) 
	{
		if (reacInteraction instanceof Catalysis)
		{
			//Creating the controlType
			ControlType bioPaxControlType = null;
			
			if (SBO.isInhibitor(reaction.getSBOTerm())) 
			{
				bioPaxControlType = ControlType.INHIBITION;	
			}
			else if (SBO.isChildOf(reaction.getSBOTerm(), 459)) // TODO the number 459 is id of SBO term: Stimulator 
			{
				bioPaxControlType = ControlType.ACTIVATION;
			}
			
			// TODO - check modifiers
			
			if(bioPaxControlType != null) 
			{
				((Control) reacInteraction).setControlType(bioPaxControlType);
			}
		}
	}

	
	protected void addControlled(BioPAXElement reacControl, BioPAXElement bioReaction) 
	{
		if (reacControl instanceof Control && bioReaction instanceof Process) 
		{
			((Control) reacControl).addControlled((Process) bioReaction);
		}
	}

	protected void addController(BioPAXElement reacControl, BioPAXElement physicalEntityParticipant) 
	{
		if (reacControl instanceof Control && physicalEntityParticipant instanceof PhysicalEntity) 
		{
			((Control) reacControl).addController((Controller) physicalEntityParticipant);
		}		
	}

	@SuppressWarnings("unchecked")
	protected void addControl(Set<? extends BioPAXElement> setOfcontrol, BioPAXElement reacControl)
	{
		if (reacControl instanceof Control)
		{
			((Set<Control>) setOfcontrol).add((Control) reacControl);
		}
		else 
		{
			println("addControl problem !! " + reacControl);
			
		}
	}

	protected Set<? extends BioPAXElement> getListOfControl(BioPAXElement bioReaction) 
	{
		if (bioReaction instanceof Control)
		{
			return ((Control) bioReaction).getControlledOf();
		}
		else 
		{
			println("getListOfControl problem !! " + bioReaction);
			
		}
		
		return null;
	}
	

	
	protected Set<? extends BioPAXElement> getListOfConversion(BioPAXElement bioReaction) 
	{
		if (bioReaction instanceof Conversion)
		{
			return ((Conversion) bioReaction).getControlledOf();
		}
		else 
		{
			println("getListOfConversion problem !! " + bioReaction);
			
		}
		
		return null;
	}

	protected Set<? extends BioPAXElement> getListOfInteraction(BioPAXElement bioReaction) 
	{
		if (bioReaction instanceof Interaction)
		{
			return ((Interaction) bioReaction).getControlledOf();
		}
		else 
		{
			println("getListOfConversion problem !! " + bioReaction);
			
		}
		
		return null;
	}
	
	protected void setConversionDirection(BioPAXElement bioReaction,
			ConversionDirectionType direction) 
	{
		if (bioReaction instanceof Conversion)
		{
			((Conversion) bioReaction).setConversionDirection(direction);
		}
	}

	protected void setSpontaneous(BioPAXElement bioReaction, boolean spontaneous) 
	{
		if (bioReaction instanceof Conversion)
		{
			((Conversion) bioReaction).setSpontaneous(spontaneous);
		}
	}

	protected void addTerm(BioPAXElement vocab, String term) 
	{
		if (vocab instanceof ControlledVocabulary)
		{
			((ControlledVocabulary) vocab).addTerm(term);
		}
	}

	protected void addDataSource(BioPAXElement pathway, BioPAXElement datasource) 
	{
		if (pathway instanceof Pathway && datasource instanceof Provenance) 
		{
			((Pathway) pathway).addDataSource((Provenance) datasource);
		}
		
	}

	protected void setDisplayName(BioPAXElement named, String displayName) 
	{
		if (named instanceof Named) 
		{
			((Named) named).setDisplayName(displayName);
		}
	}

	protected void addName(BioPAXElement named, String name) 
	{
		if (named instanceof Named) 
		{
			((Named) named).addName(name);
		}
	}

	protected void setXrefId(BioPAXElement xref, String annotationIdentifier) 
	{
		if (xref instanceof Xref)
		{
			((Xref) xref).setId(annotationIdentifier);
		}		
	}

	protected void setXrefDb(BioPAXElement xref, String annotationDBname) 
	{
		if (xref instanceof Xref)
		{
			((Xref) xref).setDb(annotationDBname);
		}		
	}

	protected void addPathwayComponent(BioPAXElement pathway, BioPAXElement bioReaction) 
	{
		if (pathway instanceof Pathway && bioReaction instanceof Process) 
		{
			((Pathway) pathway).addPathwayComponent((Process) bioReaction);
		}		
	}

	protected void setOrganismToPathway(BioPAXElement pathway, BioPAXElement biosource) 
	{
		if (pathway instanceof Pathway && biosource instanceof BioSource) 
		{
			((Pathway) pathway).setOrganism((BioSource) biosource);
		}
	}

	protected void addXref(BioPAXElement biopaxElement, BioPAXElement xref) 
	{
		if (biopaxElement instanceof XReferrable && xref instanceof Xref) 
		{
			((XReferrable) biopaxElement).addXref((Xref) xref);
		}		
	}

	protected void addComment(BioPAXElement biopaxElement, String commentString) 
	{
		if (biopaxElement instanceof Level3Element) 
		{
			((Level3Element) biopaxElement).addComment(commentString);
		}		
	}

	protected BioPAXFactory getBioPaxFactory() 
	{
		if (biopaxLevel == 3) 
		{
			return BioPAXLevel.L3.getDefaultFactory();
		}
		else if (biopaxLevel == 2) 
		{
			return BioPAXLevel.L2.getDefaultFactory();
		}
		
		return null;
	}

	private void addProduct(Model bioPaxModel, BioPAXElement bioReaction,
			BioPAXFactory elementFactory, SpeciesReference sr, int index) 
	{
		addSpeciesReference(bioPaxModel, bioReaction, elementFactory, sr, RIGHT_OF_REACTION, index);
	}

	private void addReactant(Model bioPaxModel, BioPAXElement bioReaction,
			BioPAXFactory elementFactory, SpeciesReference sr, int index) 
	{
		addSpeciesReference(bioPaxModel, bioReaction, elementFactory, sr, LEFT_OF_REACTION, index);
	}

	
	private void addSpeciesReference(Model bioPaxModel, BioPAXElement bioReaction,
			BioPAXFactory elementFactory, SpeciesReference speciesReferenceObj, 
			String direction, int index) 
	{
		String speciesReference = speciesReferenceObj.getSpecies();
		Species speciesInstance = speciesReferenceObj.getSpeciesInstance();

		//Creating the new PEP
		BioPAXElement newPEP = elementFactory.create(PHYSICAL_ENTITY_PARTICIPANT, direction + "_" + index + "_" + bioReaction.getRDFId() + "_" + speciesReference);

		setDisplayName(newPEP, speciesReference);
		
		//Setting the Physical Entity to the PEP
		addMemberPhysicalEntity(newPEP, bioPaxModel.getByID(speciesReference));
		setCellularLocation(newPEP, bioPaxModel.getByID(speciesInstance.getCompartment()));

		//setting participant stoichiometry
		double stoichiometry = speciesReferenceObj.getStoichiometry();

		BioPAXElement stoichio = setStoichiometry(bioReaction, newPEP, stoichiometry, direction + "_" + index + "_" + bioReaction.getRDFId() + "_" + speciesReference + "_STOICHIOMETRY", elementFactory);

		if (direction.equals(LEFT_OF_REACTION))
		{
			//Adding PEP to the Left
			addLeft(bioReaction, newPEP);
		}
		else if (direction.equals(RIGHT_OF_REACTION))
		{
			//Adding PEP to the Right
			addRight(bioReaction, newPEP);			
		}
		
		bioPaxModel.add(newPEP);
		
		if (biopaxLevel == 3 && stoichio != null)
		{
			bioPaxModel.add(stoichio);
		}
	}

	protected void addRight(BioPAXElement bioReaction, BioPAXElement physicalEntityParticipant) 
	{
		if (bioReaction instanceof Conversion && physicalEntityParticipant instanceof PhysicalEntity)
		{
			((Conversion) bioReaction).addRight((PhysicalEntity) physicalEntityParticipant);
		}
	}

	protected void addLeft(BioPAXElement bioReaction, BioPAXElement physicalEntityParticipant) 
	{
		if (bioReaction instanceof Conversion && physicalEntityParticipant instanceof PhysicalEntity)
		{
			((Conversion) bioReaction).addLeft((PhysicalEntity) physicalEntityParticipant);
		}
	}

	protected BioPAXElement setStoichiometry(BioPAXElement bioReaction,
			BioPAXElement physicalEntityParticipant, double stoichiometry, String stochiometryId, BioPAXFactory elementFactory) 
	{

		if (bioReaction instanceof Conversion && physicalEntityParticipant instanceof PhysicalEntity)
		{
			Stoichiometry stoichio = elementFactory.create(Stoichiometry.class, stochiometryId);

			stoichio.setStoichiometricCoefficient((float) stoichiometry);
			stoichio.setPhysicalEntity((PhysicalEntity) physicalEntityParticipant);

			((Conversion) bioReaction).addParticipantStoichiometry(stoichio);
			
			return stoichio;
		}
		else 
		{
			println("setStoichiometry problem !! " + physicalEntityParticipant + "; " + bioReaction);
		}
		
		return null;
	}

	protected void addMemberPhysicalEntity(BioPAXElement physicalEntityParticipant,
			BioPAXElement physicalEntity) 
	{
		if (physicalEntityParticipant instanceof PhysicalEntity && physicalEntity instanceof PhysicalEntity)
		{
			((PhysicalEntity) physicalEntityParticipant).addMemberPhysicalEntity((PhysicalEntity) physicalEntity);
		}
		else 
		{
			println("addMemberPhysicalEntity problem !! " + physicalEntityParticipant + "; " + physicalEntity);
		}
	}

	protected void setCellularLocation(BioPAXElement physicalEntityParticipant, BioPAXElement cellularLocation) 
	{
		if (physicalEntityParticipant instanceof PhysicalEntity && cellularLocation instanceof CellularLocationVocabulary)
		{
			((PhysicalEntity) physicalEntityParticipant).setCellularLocation((CellularLocationVocabulary) cellularLocation);
		}
		else 
		{
			println("setCellularLocation problem !! " + physicalEntityParticipant + "; " + cellularLocation);
		}
	}

	/**
	 * @param reaction
	 * @param bioPaxModel
	 * @return
	 */
	private String findReactionType(Reaction reaction, Model bioPaxModel) 
	{
		//List of reactants
		ListOf<SpeciesReference> listReactants = reaction.getListOfReactants();
		//List of Products
		ListOf<SpeciesReference> listProducts = reaction.getListOfProducts();

		/*
		 * Let's see if we have a complex assembly or a complex disassembly
		 */

		// Difference between number of complex in the reactants and in the products
		int complexBalance = 0;

		//Counting complex in the reactants
		for (SpeciesReference speciesReferenceReac: listReactants) 
		{
			if (isComplex(bioPaxModel, speciesReferenceReac.getSpecies())) 
			{
				complexBalance--;
			}
		}

		//Counting complex in the products
		for (SpeciesReference speciesReferenceProd: listProducts) 
		{
			if (isComplex(bioPaxModel, speciesReferenceProd.getSpecies())) 
			{
				complexBalance++;
			}
		}

		/*
		 * If we have a complexBalance == 0, there has be no complexAssembly nor complexDisAssembly
		 * if complexBalance < 0 it is a complexdisAssembly
		 * if complexBalance > 0 it is a complexAssembly
		 */

		/*
		 * Let's see if we have a transport or a transport with reaction
		 * Is the reaction a transport ? A -> A' (compartment(A) != compartment(A'))
		 */

		boolean isTransport = false;				
		String compartmentProd;		
		String compartmentReac;		
		String reactionTypeReturn;	

		// For each reactant
		for (SpeciesReference speciesReferenceReac: listReactants) 
		{
			// reset of the product compartment
			compartmentProd = "";
			
			// setting the reactant compartment
			compartmentReac = speciesReferenceReac.getSpeciesInstance().getCompartment();
			
			//Looping on the products
			for (SpeciesReference speciesReferenceProd: listProducts) 
			{				
				compartmentProd = speciesReferenceProd.getSpeciesInstance().getCompartment();
				
				if (! compartmentProd.equals(compartmentReac)) 
				{
					isTransport = true;
					break;
				}
			}

			if (isTransport) 
			{
				break;
			}
		}
			
		// Returning the reaction type
		if (isTransport)
		{
			reactionTypeReturn = TRANSPORT;
		}
		else if (complexBalance > 0) 
		{
			reactionTypeReturn = COMPLEX_ASSEMBLY;
		}
		else if (complexBalance < 0) 
		{
			reactionTypeReturn = COMPLEX_DIS_ASSEMBLY;
		}
		else 
		{
			reactionTypeReturn = BIOCHEMICAL_REACTION;
		}		
		
		return reactionTypeReturn;
	}

	protected boolean isComplex(Model bioPaxModel, String species) 
	{
		if (bioPaxModel.getByID(species) instanceof Complex)
		{
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @param annotation
	 * @return
	 */
	private MiriamAnnotation parseMiriamAnnotation(String annotation) {

		String uri = link.getMiriamURI(annotation);
		String id = RegistryUtilities.getElementPart(uri);
				
		// System.out.println("SBML2Biopax : parseMiriamAnnotation : uri = " + uri + ", id = " + id);
		
		if (uri != null && id != null)
		{			
			return new MiriamAnnotation(id, RegistryUtilities.getDataPart(uri), uri);			
		}
		else 
		{
			println("WARNING : the annotation '" + annotation + "' could not be handled correctly !!!");
		}
		
		return null;
	}

	
	
	/**
	 * Gets the official name of the datatype with the given URI.
	 * <p>
	 * example:
	 * <code><pre>link.getName("http://www.ec-code.org/");</code></pre>
	 * reply: Enzyme Nomenclature
	 * 
	 * @param uri URI for the MIRIAM request. Looking for the official name of this database
	 * @return the Official name of the datatype.
	 */
	private String getDatatypeName(String uri) 
	{
		return link.getName(uri);
	}



	/**
	 * Control the validity of the identifier RDF<br/>
	 * The rdf:ID mustn't accept the characters: space, '/', ':'<br/>
	 * ex:  "Gene Ontology/GO:0019236"  give  "Gene_Ontology_GO_0019236"
	 * @param db String to be check
	 * @return Corrected string
	 */
	@SuppressWarnings("unused")
	private String checkDBname(String db){
		db = db.replace(" ", "_").replace(":", "_").replace("/", "_").replace("(", "_").replace(")", "_");
		return db;
	}

	static void println(String msg){
		System.out.println(msg);
	}
	static void println(boolean msg){
		System.out.println(msg);
	}
	static void println(int msg){
		System.out.println(msg);
	}
	static void println(double msg){
		System.out.println(msg);
	}

	static void debug(String mesg)
	{
		// TODO : add proper logging
		System.out.println("DEBUG : " + mesg);
	}
	
	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {
		try {
			inputModel = model;
			return biopaxexport((SBMLModel)model);
		} catch (SBMLException e) {
			throw new ReadModelException(e);
		}
	}

	public String getResultExtension() {
		return "-biopax" + biopaxLevel + ".owl";
	}
	
	@Override
	public String getName() {
		return "SBML2BioPAX";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to BioPAX";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to BioPAX";
	}
	
	
}
