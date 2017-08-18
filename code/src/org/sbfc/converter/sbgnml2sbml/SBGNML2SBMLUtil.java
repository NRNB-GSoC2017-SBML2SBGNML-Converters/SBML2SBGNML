package org.sbfc.converter.sbgnml2sbml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

/**
 * SBGNML2SBMLUtil contains methods that do not depend on any information in the Model. 
 * Example: finding a value from a given list.
 * @author haoran
 *
 */
public class SBGNML2SBMLUtil {
	int level;
	int version;
	
	// for debugging
	int debugMode;
	int createOneCurveError = 0;
	
	/**
	 * Constructor. Note that the only possible value for level is 3, and version 1
	 * @param level
	 * @param version
	 */
	SBGNML2SBMLUtil(int level, int version) {
		this.level = level;
		this.version = version;
	}

	public Boolean isProcessNode(String clazz) {
		if (clazz.equals("process")) {
			return true;
		} else if (clazz.equals("omitted process")) {
			return true;
		} else if (clazz.equals("uncertain process")) {
			return true;
		} else if (clazz.equals("association")) {
			return true;
		} else if (clazz.equals("dissociation")) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isCompartment(String clazz) {
		if (clazz.equals("compartment")) {
			return true;
		}
		return false;
	}
	
	public Boolean isLogicOperator(String clazz) {
		if (clazz.equals("and")) {
			return true;
		} else if (clazz.equals("or")) {
			return true;
		} else if (clazz.equals("not")) {
			return true;
		} else if (clazz.equals("delay")) {
			return true;
		} 
		return false;
	}
	
	public boolean isTag(String clazz) {
		if (clazz.equals("tag")) {
			return true;
		} 
		// terminals are "nested glyphs" inside another glyph, so we don't create a SpeciesGlyph for it (we create a GeneralGlyph instead)
//		else if (clazz.equals("terminal")){
//			System.out.println("terminal");
//			return true;
//		}
		
		return false;
	}	
	
	public Boolean isAnnotation(String clazz) {
		if (clazz.equals("annotation")) {
			return true;
		}
		return false;
	}
	
	public Boolean isEntityPoolNode(String clazz){
		if (clazz.equals("unspecified entity")) {
			return true;
		} else if (clazz.equals("simple chemical")) {
			return true;
		} else if (clazz.equals("macromolecule")) {
			return true;
		} else if (clazz.equals("nucleic acid feature")) {
			return true;
		} else if (clazz.equals("complex multimer")) {
			return true;
		} else if (clazz.equals("complex")) {
			return true;
		} else if (clazz.equals("macromolecule multimer")) {
			return true;
		} else if (clazz.equals("nucleic acid feature multimer")) {
			return true;
		} else if (clazz.equals("simple chemical multimer")) {
			return true;
		} else if (clazz.equals("source and sink")) {
			return true;
		} else if (clazz.equals("perturbing agent")) {
			return true;
		} else if (clazz.equals("perturbation")) {
			return true;
		} 
		
		else if (clazz.equals("biological activity")) {
			return true;
		} else if (clazz.equals("phenotype")) {
			return true;
		} else if (clazz.equals("submap")) {
			return true;
		} else {
			return false;
		}		
	}	
	
	public Boolean isLogicArc(Arc arc) {
		String clazz = arc.getClazz();
		if (clazz.equals("logic arc")) {
			return true;
		} else if (clazz.equals("equivalence arc")) {
			return true;
		}
		return false;
	}
	
	public Boolean isModifierArc(String clazz) {
		if (clazz.equals("stimulation")) {
			return true;
		} if (clazz.equals("catalysis")) {
			return true;
		} if (clazz.equals("inhibition")) {
			return true;
		} if (clazz.equals("necessary stimulation")) {
			return true;
		} if (clazz.equals("modulation")) {
			return true;
		} else if (clazz.equals("unknown influence")) {
			return true;
		} // TODO: there are more
		else {
			return false;
		} 
	}
	
	public Boolean isConsumptionArc(String clazz) {
		if (clazz.equals("consumption")) {
			return true;
		}
		else {
			return false;
		} 
	}
	
	public Boolean isProductionArc(String clazz) {
		if (clazz.equals("production")) {
			return true;
		} else {
			return false;
		} 		
	}
	
	/**
	 * Map a Sbgn string clazz to a Jsbml SpeciesReferenceRole object
	 * @param clazz
	 * @return
	 */
	public SpeciesReferenceRole findReactionRole(String clazz) {
		SpeciesReferenceRole role = null;

		if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.PRODUCT;
		} else if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SIDESUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.SIDEPRODUCT;
		} else if (clazz.equals("catalysis")) {
			role = SpeciesReferenceRole.ACTIVATOR;
		} else if (clazz.equals("inhibition")) {
			role = SpeciesReferenceRole.INHIBITOR;
		} else if (clazz.equals("modulation")) {
			role = SpeciesReferenceRole.MODIFIER;
		} else if (clazz.equals("unknown influence")) {
			role = SpeciesReferenceRole.UNDEFINED;
		}
		
		return role;		
	}
			
	/**
	 * Set the SBOTerm of an SBase, based on the Sbgn clazz
	 * @param sbase
	 * @param clazz
	 */
	public void addSBO(SBase sbase, String clazz){
		int sboTerm = -1;
		
		// Entity Pool
		if (clazz.equals("simple chemical")) {
			sboTerm = 247;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule")) {
			sboTerm = 245;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature")) {
			sboTerm = 354;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex multimer")) {
			sboTerm = 418;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex")) {
			sboTerm = 253;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule multimer")) {
			sboTerm = 420;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature multimer")) {
			sboTerm = 419;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("source and sink")) {
			sboTerm = 291;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("perturbing agent")) {
			sboTerm = 405;
			sbase.setSBOTerm(sboTerm);
		} else if (clazz.equals("unspecified entity")) {
			sboTerm = 285;
			sbase.setSBOTerm(sboTerm);			
		} else if (clazz.equals("simple chemical multimer")) {
			sboTerm = 421;
			sbase.setSBOTerm(sboTerm);
		}
		
		// Activity
		else if (clazz.equals("biological activity")){sboTerm = 412; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("phenotype")){sboTerm = 358; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("observable")){sboTerm = 358; sbase.setSBOTerm(sboTerm);}
		
		// Encapsulation
		else if (clazz.equals("compartment")){sboTerm = 290; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("submap")){sboTerm = 395; sbase.setSBOTerm(sboTerm);}

		// Process
		else if (clazz.equals("process")){sboTerm = 375; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("omitted process")){sboTerm = 397; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("uncertain process")){sboTerm = 396; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("association")){sboTerm = 177; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("dissociation")){sboTerm = 180; sbase.setSBOTerm(sboTerm);}
		
		// Logic Operator
		else if (clazz.equals("and")){sboTerm = 173; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("or")){sboTerm = 174; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("not")){sboTerm = 238; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("delay")){sboTerm = 225; sbase.setSBOTerm(sboTerm);}
		
		// Auxiliary Information
		else if (clazz.equals("state variable")){}
		else if (clazz.equals("unit of information")){}
		else if (clazz.equals("cardinality")){sboTerm = 364; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("annotation")){sboTerm = 550; sbase.setSBOTerm(sboTerm);}

		else if (clazz.equals("tag")){} // identified by render:objectRole
		else if (clazz.equals("perturbation")){} // this is "unit of information"
		else if (clazz.equals("terminal")){} //
		
		// TODO add sbo
		else if (clazz.equals("entity")){}
		else if (clazz.equals("existence")){}
		else if (clazz.equals("location")){}
		else if (clazz.equals("variable value")){}
		else if (clazz.equals("implicit xor")){}
		else if (clazz.equals("outcome")){}
		else if (clazz.equals("interaction")){}
		else if (clazz.equals("influence target")){}
		
		// Arc
		else if (clazz.equals("production")){sboTerm = 393; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("consumption")){sboTerm = 394; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("catalysis")){sboTerm = 172; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("modulation")){sboTerm = 168; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("stimulation")){sboTerm = 170; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("inhibition")){sboTerm = 169; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("positive influence")){sboTerm = 170; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("negative influence")){sboTerm = 169; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("unknown influence")){sboTerm = 168; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("equivalence arc")){}
		else if (clazz.equals("necessary stimulation")){sboTerm = 171; sbase.setSBOTerm(sboTerm);}
		else if (clazz.equals("logic arc")){sboTerm = 398; sbase.setSBOTerm(sboTerm);}
		
		// TODO add sbo
		else if (clazz.equals("assignment")){}
		else if (clazz.equals("interaction")){}
		else if (clazz.equals("absolute inhibition")){}
		else if (clazz.equals("absolute stimulation")){}
		
	}	
	
	/** 
	 * Store the Sbgn string clazz to a Annotation's CVTerm of the given SBase
	 * @param sbase
	 * @param clazz
	 * @param qualifier
	 */
	public void addAnnotation(SBase sbase, String clazz, Qualifier qualifier) {
		Annotation annotation;
		CVTerm cvTerm;
		
		annotation = sbase.getAnnotation();
		// TODO: use different namespace, should be urn
		cvTerm = new CVTerm(Type.BIOLOGICAL_QUALIFIER, qualifier);
		cvTerm.addResource(clazz);
		annotation.addCVTerm(cvTerm);
	}
	
	/**
	 * Create a new Species
	 * @param speciesId
	 * @param name
	 * @param clazz
	 * @param addAnnotation
	 * @param addSBO
	 * @return
	 */
	public Species createJsbmlSpecies(String speciesId, String name, String clazz, 
			boolean addAnnotation, boolean addSBO) {
		Species species;
		species = new Species(speciesId, name, level, version);
		
		// if no [] info provided
		species.setInitialConcentration(1.0);
		
		if (addAnnotation){
			// TODO change the qualifier so that it doesn't conflict 
			addAnnotation(species, clazz, Qualifier.BQB_IS_VERSION_OF);
		}
		if (addSBO){
			addSBO(species, clazz);	
		}
		
		return species;
	}	
	
	/**
	 * Create a new SpeciesGlyph
	 * @param speciesId
	 * @param name
	 * @param clazz
	 * @param species
	 * @param createBoundingBox
	 * @param bbox
	 * @return
	 */
	public SpeciesGlyph createJsbmlSpeciesGlyph(String speciesId, String name, String clazz, 
			Species species, boolean createBoundingBox, Bbox bbox) {
		SpeciesGlyph speciesGlyph;
		BoundingBox boundingBox;
		
		speciesGlyph = new SpeciesGlyph(level, version);
		speciesGlyph.setId("SpeciesGlyph_" + speciesId);
		
		// QualitativeSpecies is not a Species, so its SpeciesGlyph does not have a Species
		if (species != null){
			speciesGlyph.setSpecies(species);
		}
				
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// TODO: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			speciesGlyph.setBoundingBox(boundingBox);				
		}
		return speciesGlyph;
	}
	
	/**
	 * Create a new GeneralGlyph
	 * @param id
	 * @param createBoundingBox
	 * @param bbox
	 * @return
	 */
	public GeneralGlyph createJsbmlGeneralGlyph(String id, boolean createBoundingBox, Bbox bbox) {
		GeneralGlyph generalGlyph;
		BoundingBox boundingBox;

		generalGlyph = new GeneralGlyph(level, version);
		generalGlyph.setId("GeneralGlyph_" + id);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// TODO: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			generalGlyph.setBoundingBox(boundingBox);					
		}

		return generalGlyph;
	}
	
	/**
	 * Create a TextGlyph using values from a <code>SpeciesGlyph</code> and its 
	 * associated <code>Species</code>.
	 */		
	public TextGlyph createJsbmlTextGlyph(NamedSBase species, SpeciesGlyph speciesGlyph, Bbox labelBbox) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxSpecies;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + species.getId();
		textGlyph.setId(id);
		textGlyph.setOriginOfText(species);
		textGlyph.setGraphicalObject(speciesGlyph);
		
		boundingBoxText = new BoundingBox();
		
		if (labelBbox == null){
		boundingBoxSpecies = speciesGlyph.getBoundingBox();
		boundingBoxText.setDimensions(boundingBoxSpecies.getDimensions());
		boundingBoxText.setPosition(boundingBoxSpecies.getPosition());
		textGlyph.setBoundingBox(boundingBoxText);
		} else {
			boundingBoxText.createDimensions(labelBbox.getW(), labelBbox.getH(), 0);
			boundingBoxText.createPosition(labelBbox.getX(), labelBbox.getY(), 0);
			textGlyph.setBoundingBox(boundingBoxText);	
		}
				
		return textGlyph;
	}	
	
	/**
	 * Create a new TextGlyph, set the text to the text provided in the parameter
	 * @param generalGlyph
	 * @param text
	 * @param labelBbox
	 * @return
	 */
	public TextGlyph createJsbmlTextGlyph(GraphicalObject generalGlyph, String text, Bbox labelBbox) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxGeneralGlyph;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + generalGlyph.getId();
		textGlyph.setId(id);
		textGlyph.setGraphicalObject(generalGlyph);
		textGlyph.setText(text);
		
		boundingBoxText = new BoundingBox();
		
		if (labelBbox == null){
			boundingBoxGeneralGlyph = generalGlyph.getBoundingBox();
			boundingBoxText.setDimensions(boundingBoxGeneralGlyph.getDimensions());
			boundingBoxText.setPosition(boundingBoxGeneralGlyph.getPosition());
			textGlyph.setBoundingBox(boundingBoxText);			
		} else {
			// TODO: horizontal or vertical orientation?
			boundingBoxText.createDimensions(labelBbox.getW(), labelBbox.getH(), 0);
			boundingBoxText.createPosition(labelBbox.getX(), labelBbox.getY(), 0);
			textGlyph.setBoundingBox(boundingBoxText);	
		}
				
		return textGlyph;
	}		
	
	/**
	 * Create a new QualitativeSpecies
	 * @param speciesId
	 * @param name
	 * @param clazz
	 * @param addAnnotation
	 * @param addSBO
	 * @return
	 */
	public QualitativeSpecies createQualitativeSpecies(String speciesId, String name, String clazz, 
			boolean addAnnotation, boolean addSBO) {
		QualitativeSpecies species;
		species = new QualitativeSpecies(speciesId, name, level, version);
		
		if (addAnnotation){
			addAnnotation(species, clazz, Qualifier.BQB_IS_VERSION_OF);
		}
		if (addSBO){
			addSBO(species, clazz);	
		}
		
		return species;
	}
	
	/** 
	 * Get the text stored in a Sbgn glyph
	 * @param glyph
	 * @return
	 */
	public String getText(Glyph glyph){
		if (glyph.getLabel() != null) {

			return glyph.getLabel().getText();
		}
		if (glyph.getClazz().equals("state variable")){
			if (glyph.getState() != null){
				if (glyph.getState().getValue() != null && glyph.getState().getVariable() != null){
					return glyph.getState().getValue() + "@" + glyph.getState().getVariable();
				} else if (glyph.getState().getValue() != null){
					return glyph.getState().getValue();
				} else if (glyph.getState().getVariable() != null){
					return "@" + glyph.getState().getVariable();
				}
			}
		}
		return "";
	}
	
	/**
	 * Get the clone text in a glyph
	 * @param glyph
	 * @return
	 */
	public String getClone(Glyph glyph) {
		String text = new String();
		try {
			Glyph.Clone clone = glyph.getClone();
			Label label = clone.getLabel();
			// there's bug, we can't just return the string in label
			try {
				text = new String(String.copyValueOf(label.getText().toCharArray())) ;
			}
			catch (NullPointerException e) {
				
			}
		}
		catch (NullPointerException e) {
			return null;
		}	
		
		return text;
	}
	
	/**
	 * Create a new Reaction
	 * @param reactionId
	 * @return
	 */
	public Reaction createJsbmlReaction(String reactionId) {
		Reaction reaction;
		
		reaction = new Reaction(level, version);
		reaction.setId(reactionId);
				
		return reaction;
	}
	
	/**
	 * Create a new ReactionGlyph
	 * @param reactionId
	 * @param name
	 * @param clazz
	 * @param reaction
	 * @param createBoundingBox
	 * @param bbox
	 * @return
	 */
	public ReactionGlyph createJsbmlReactionGlyph(String reactionId, String name, String clazz, 
			Reaction reaction, boolean createBoundingBox, Bbox bbox) {
		ReactionGlyph reactionGlyph;
		BoundingBox boundingBox;
		
		reactionGlyph = new ReactionGlyph(level, version);
		reactionGlyph.setId("ReactionGlyph_" + reactionId);
		reactionGlyph.setReaction(reaction);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// TODO: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			reactionGlyph.setBoundingBox(boundingBox);				
		}
		return reactionGlyph;
	}	
	
	/**
	 * Create the dimensions for a boundingBox
	 * @param boundingBox
	 * @param bbox
	 * @return
	 */
	public Dimensions createDimensions(BoundingBox boundingBox, Bbox bbox){
		Dimensions dimension;
		
		boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
		
		return boundingBox.getDimensions();
	}
	
	/**
	 * Create the position for a boundingBox
	 * @param boundingBox
	 * @param bbox
	 * @return
	 */
	public Point createPoint(BoundingBox boundingBox, Bbox bbox){
		Point point;
		
		boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);	
		
		return boundingBox.getPosition();
	}
	
	/** 
	 * Create the boundingBox for a graphicalObject
	 * @param graphicalObject
	 * @param glyph
	 */
	public void createBoundingBox(GraphicalObject graphicalObject, Glyph glyph){
		BoundingBox boundingBox = new BoundingBox();
		Bbox bbox = glyph.getBbox();
		
		graphicalObject.setBoundingBox(boundingBox);
		createDimensions(boundingBox, bbox);
		createPoint(boundingBox, bbox);
	}	
	
	/**
	 * Create a temporary <code>Curve</code> for the given <code>ReactionGlyph</code>. 
	 * This Curve has incorrect Start and End Points. 
	 * The values of the <code>Curve</code> will be modified later;
	 */	
	public void createReactionGlyphCurve(ReactionGlyph reactionGlyph, Glyph glyph) {
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		curve = new Curve();
		bbox = glyph.getBbox();
		point = new Point(bbox.getX(), bbox.getY());
		point = new Point(bbox.getX()+bbox.getW(), bbox.getY()+bbox.getH());
		// do nothing with the point

		reactionGlyph.setCurve(curve);		
	}
		
	/**
	 * Create a new SpeciesReference
	 * @param reaction
	 * @param species
	 * @param speciesReferenceId
	 * @return
	 */
	public SpeciesReference createSpeciesReference(Reaction reaction, Species species, 
			String speciesReferenceId) {
		SpeciesReference speciesReference;
		
		speciesReference = new SpeciesReference(level, version);
		speciesReference.setId(speciesReferenceId);
		speciesReference.setSpecies(species);
	
		return speciesReference;
	}	
	
	/**
	 * Create a new ModifierSpeciesReference
	 * @param reaction
	 * @param species
	 * @param speciesReferenceId
	 * @return
	 */
	public ModifierSpeciesReference createModifierSpeciesReference(Reaction reaction, Species species, 
			String speciesReferenceId) {
		ModifierSpeciesReference speciesReference;
		
		speciesReference = new ModifierSpeciesReference(level, version);
		speciesReference.setId(speciesReferenceId);
		speciesReference.setSpecies(species);
	
		return speciesReference;
	}		
	
	/**
	 * Create a new Transition for the special case where there are no Logic Operators
	 * @param id
	 * @param inputQualitativeSpecies
	 * @param outputQualitativeSpecies
	 * @return
	 */
	public Transition createTransition(String id, 
			QualitativeSpecies inputQualitativeSpecies, QualitativeSpecies outputQualitativeSpecies){
		Transition transition = new Transition(level, version);
		Input input;
		Output output;
		String inputId;
		String outputId;
		FunctionTerm functionTerm;
		
		if (inputQualitativeSpecies != null){
			inputId = "Input_" + inputQualitativeSpecies.getId() + "_in_" + id;
			input = new Input(inputId, inputQualitativeSpecies, InputTransitionEffect.none);
			transition.addInput(input);
		}
		
		if (outputQualitativeSpecies != null){
			outputId = "Output_" + outputQualitativeSpecies.getId() + "_in_" + id; 
			output = new Output(outputId, outputQualitativeSpecies, OutputTransitionEffect.assignmentLevel);
			transition.addOutput(output);
		}
		
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(true);
		functionTerm.setResultLevel(0);
		transition.addFunctionTerm(functionTerm);
		
		// TODO: only create one if both input and output is known
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(false);
		functionTerm.setResultLevel(1);
		transition.addFunctionTerm(functionTerm);
		
		return transition;
	}
	
	/**
	 * Create a new Transition
	 * @param id
	 * @return
	 */
	public Transition createTransition(String id){
		Transition transition = new Transition(id, level, version);
		
		return transition;
	}
	
	/**
	 * Add a input QualitativeSpecies to the transition
	 * @param transition
	 * @param inputQualitativeSpecies
	 * @return
	 */
	public Input addInputToTransition(Transition transition, QualitativeSpecies inputQualitativeSpecies){
		Input input;
		String inputId;		

		inputId = "Input_" + inputQualitativeSpecies.getId() + "_in_" + transition.getId();
		input = new Input(inputId, inputQualitativeSpecies, InputTransitionEffect.none);
		transition.addInput(input);
		
		return input;
	}
	
	/**
	 * Add a output QualitativeSpecies to the transition
	 * @param transition
	 * @param outputQualitativeSpecies
	 * @return
	 */
	public Output addOutputToTransition(Transition transition, QualitativeSpecies outputQualitativeSpecies){
		Output output;
		String outputId;
		
		outputId = "Output_" + outputQualitativeSpecies.getId() + "_in_" + transition.getId(); 
		output = new Output(outputId, outputQualitativeSpecies, OutputTransitionEffect.assignmentLevel);
		transition.addOutput(output);	
		
		return output;
	}
	
	/**
	 * Add a new functionTerm to transition without a MathML. The MathML will be created in other functions
	 * @param transition
	 * @param setDefaultTerm
	 * @param resultLevel
	 * @return
	 */
	public FunctionTerm addFunctionTermToTransition(Transition transition, boolean setDefaultTerm, int resultLevel){
		FunctionTerm functionTerm;
		
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(setDefaultTerm);
		functionTerm.setResultLevel(resultLevel);
		transition.addFunctionTerm(functionTerm);
		
		return functionTerm;
	}
	
	/**
	 * Create a new ASTNode, and insert it into an existing parent ASTNode
	 * @param parentMath
	 * @param type
	 * @return
	 */
	public ASTNode createMath(ASTNode parentMath, String type){
		//ASTNode math = functionTerm.getMath();
		ASTNode math = null;

		if (type.equals("and")){
			math = new ASTNode(ASTNode.Type.LOGICAL_AND);
		} if (type.equals("or")){
			math = new ASTNode(ASTNode.Type.LOGICAL_OR);
		} if (type.equals("not")){
			math = new ASTNode(ASTNode.Type.LOGICAL_NOT);
		} 
		
		parentMath.insertChild(0, math);
				
		return math;
	}
	
	/**
	 * Create a new ASTNode, it will be root of the MathML in a FunctionTerm
	 * @param type
	 * @param mathContainer
	 * @return
	 */
	public ASTNode createMath(String type, FunctionTerm mathContainer){
		//ASTNode math = functionTerm.getMath();
		ASTNode math = null;

		if (type.equals("and")){
			math = new ASTNode(ASTNode.Type.LOGICAL_AND);
		} if (type.equals("or")){
			math = new ASTNode(ASTNode.Type.LOGICAL_OR);
		} if (type.equals("not")){
			math = new ASTNode(ASTNode.Type.LOGICAL_NOT);
		} 
		
		math.setParentSBMLObject(mathContainer);
		
		return math;
	}
	
	/**
	 * Create a <code>SpeciesReferenceGlyph</code> using values from an SBGN <code>Arc</code>. 
	 * Associate the <code>SpeciesReferenceGlyph</code> with a <code>SpeciesGlyph</code> and 
	 * a <code>SpeciesReference</code>.
	 * 
	 * @param <code>String</code> id
	 * @param <code>Arc</code> arc
	 * @param <code>SpeciesReference</code> speciesReference
	 * @param <code>Glyph</code> speciesGlyph
	 * @return <code>SpeciesReferenceGlyph</code> speciesReferenceGlyph
	 */		
	public SpeciesReferenceGlyph createOneSpeciesReferenceGlyph(String id, Arc arc, 
			SimpleSpeciesReference speciesReference, Glyph speciesGlyph, SBGNML2SBMLOutput sOutput) {
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		speciesReferenceGlyph = new SpeciesReferenceGlyph(level, version);
		speciesReferenceGlyph.setId("SpeciesReferenceGlyph_"+id);
		speciesReferenceGlyph.setRole(findReactionRole(arc.getClazz()));
		speciesReferenceGlyph.setSpeciesGlyph("SpeciesGlyph_"+speciesGlyph.getId());
		speciesReferenceGlyph.setSpeciesReference(speciesReference);	
		sOutput.numOfSpeciesReferences++;			
		
		return speciesReferenceGlyph;
	}
	
	/**
	 * Create an SBML <code>Curve</code> from values in an SBGN <code>Arc</code>. 
	 * It creates both LineSegments as well as CurveSegments
	 */		
	public Curve createOneCurve(Arc arc) {
		Curve curve;
		CurveSegment curveSegment;
		
		Arc.Start start;
		Arc.End end;
		List<Arc.Next> listOfNext;
		List<org.sbgn.bindings.Point> points;
		
		// We will use this list of curvePoints to construct the CurveSegments of a Curve
		// first, we store the points, later, we use them
		List<Point> curvePoints = new ArrayList<Point>();
		
		// The starting point
		start = arc.getStart();
		curvePoints.add(new Point(start.getX(), start.getY()));
		
		// The points in Arc.Next
		listOfNext = arc.getNext();
		for (Arc.Next next: listOfNext){	
			points = next.getPoint();
			
			// We have a CubicBezier
			if (points.size() > 0){
				// create a Wrapper for the list of points in this CubicBezier
				SBGNWrapperPoint sWrapperPoint = new SBGNWrapperPoint(next.getX(), next.getY());
				sWrapperPoint.addbasePoint(points);
				curvePoints.add(sWrapperPoint);
			} 
			// We have a Line
			else {
				curvePoints.add(new Point(next.getX(), next.getY()));
			}
		}
		
		// The end point or end CubicBezier points
		end = arc.getEnd();
		points = end.getPoint();
		// We have a CubicBezier
		if (points.size() > 0){
			SBGNWrapperPoint sWrapperPoint = new SBGNWrapperPoint(end.getX(), end.getY());
			sWrapperPoint.addbasePoint(points);
			curvePoints.add(sWrapperPoint);
		} 
		// We have a Line
		else {
			curvePoints.add(new Point(end.getX(), end.getY()));
		}
		
		// Now we create a new Curve, and construct the CurveSegments using the curvePoints we stored
		curve = new Curve();
		Point startPoint;
		Point endPoint;
		for (int i = 0; i < curvePoints.size(); i++){
			// last Point
			if (i == curvePoints.size() - 1){
				break;
			}
			
			curveSegment = null;
			// a curveSegment will have a start and an end point
			startPoint = curvePoints.get(i);
			// We have a CubicBezier, so we need to get the point, not the Wrapper of the point
			if (startPoint instanceof SBGNWrapperPoint){
				startPoint = ((SBGNWrapperPoint) startPoint).onePoint.clone();
			}
			
			// the end point will be the next point stored in curvePoints
			endPoint = curvePoints.get(i + 1);
			// We have a CubicBezier, so we need to get the point, not the Wrapper of the point
			if (endPoint instanceof SBGNWrapperPoint){
				Point endPointNew = ((SBGNWrapperPoint) endPoint).onePoint.clone();
				Point basePoint1 = ((SBGNWrapperPoint) endPoint).basePoint1.clone();
				Point basePoint2 = ((SBGNWrapperPoint) endPoint).basePoint2.clone();
				
				curveSegment = new CubicBezier();
				// set the start and end
				curveSegment.setStart(startPoint);
				curveSegment.setEnd(endPointNew);	
				
				// set the 2 base points as well
				((CubicBezier) curveSegment).setBasePoint1(basePoint1);
				((CubicBezier) curveSegment).setBasePoint2(basePoint2);
				
			} 
			// We have a Line
			else {
				endPoint = curvePoints.get(i + 1).clone();
				curveSegment = new LineSegment();
				
				// set the start and end
				curveSegment.setStart(startPoint);
				curveSegment.setEnd(endPoint);	
			}	
			
			// add CurveSegment to the Curve
			curve.addCurveSegment(curveSegment);				
		}
		
		// error
		if (curve.getCurveSegmentCount() == 0){
			createOneCurveError ++;
			System.out.format("! createOneCurve sbml="+curvePoints.size());
			for (Arc.Next next: listOfNext){
				points = next.getPoint();
				
				System.out.format(" sbgn.next="+next.getPoint().size());
			}
			System.out.format(" sbgn.end="+end.getPoint().size()+" \n");

		}
		
		return curve;
	}	
	
	/**
	 * Create a new Compartment
	 * @param compartmentId
	 * @param name
	 * @return
	 */
	public Compartment createJsbmlCompartment(String compartmentId, String name) {
		Compartment compartment = new Compartment(compartmentId, name, level, version);
		return compartment;
	}
	
	/**
	 * Create a new CompartmentGlyph
	 * @param glyph
	 * @param compartmentId
	 * @param compartment
	 * @param createBoundingBox
	 * @return
	 */
	public CompartmentGlyph createJsbmlCompartmentGlyph(Glyph glyph, String compartmentId, Compartment compartment,
			boolean createBoundingBox) {
		CompartmentGlyph compartmentGlyph;
		Bbox bbox;
		BoundingBox boundingBox;
		
		compartmentGlyph = new CompartmentGlyph();
		compartmentGlyph.setId("CompartmentGlyph_" + compartmentId);
		compartmentGlyph.setCompartment(compartment);
		
		if (createBoundingBox){
			bbox = glyph.getBbox();
			boundingBox = new BoundingBox();
			// TODO: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			compartmentGlyph.setBoundingBox(boundingBox);			
		}
		return compartmentGlyph;
	}
	
	/**
	 * Set the compartmentOrder attribute of the compartmentGlyph
	 * @param compartmentGlyph
	 * @param glyph
	 */
	public void setCompartmentOrder(CompartmentGlyph compartmentGlyph, Glyph glyph) {	
		try {
			float order = glyph.getCompartmentOrder();
			if ((Object) order != null){
				compartmentGlyph.setOrder(order);
			}
		} catch (NullPointerException e) {
			
		}
	}
	
	/**
	 * We always create a default compartment and assign Species that do not have a Compartment to this Compartment
	 * TODO: need to check compartmentRef of an Sbgn glyph to find its compartment
	 * TODO: move to a ModelCompleter
	 * @param modelObject
	 */
	public void createDefaultCompartment(Model modelObject) {
		String compartmentId = "DefaultCompartment_01";
		Compartment compartment = new Compartment(compartmentId);
		modelObject.getListOfCompartments().add(compartment);
		
		for (Species species: modelObject.getListOfSpecies()){
			if (species.getCompartment() == ""){
				species.setCompartment(compartment);	
			}
		}
	}
	
	/**
	 * Create a new ReferenceGlyph for a ModifierSpeciesReference.
	 * The ModifierSpeciesReference is part of a logic network (see the NOT network in SBGN-PD_all.sbgn)
	 * Note that normally, a ModifierSpeciesReference associates with a Reaction and a Species,
	 * but if we have a Logic Operator instead of a Species, we can't create a SpeciesReferenceGlyph
	 * we have to create a ReferenceGlyph instead.
	 * @param id
	 * @param arc
	 * @param reference
	 * @param object
	 * @return
	 */
	public ReferenceGlyph createOneReferenceGlyph(String id, Arc arc, ModifierSpeciesReference reference, 
			SBase object) {
		ReferenceGlyph referenceGlyph;
		
		referenceGlyph = new ReferenceGlyph(level, version);
		referenceGlyph.setId("ReferenceGlyph_" + id);

		referenceGlyph.setGlyph(object.getId());
		
		// the following does not work because we can't create a ModifierSpeciesReference. 
		// A ModifierSpeciesReference needs to associate with a Species, but we don't have a Species
		//referenceGlyph.setReference(reference.getId());

		return referenceGlyph;
	}		
	
	/**
	 * Check if the arc's source and target objects are both glyphs
	 * @param arc
	 * @return
	 */
	public Boolean isGlyphToGlyphArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;
	}
	
	public Boolean isPortToGlyphArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;		
	}
	
	public Boolean isGlyphToPortArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Port) {
			return true;
		}
		return false;		
	}

	public Boolean isPortToPortArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Port) {
			return true;
		}		
		return false;
	}
	
	/**
	 * For debugging only
	 */
	public static void debugSbgnObject(Map map){
		
		List<Arc> listOfArcs = map.getArc();
		List<Glyph> listOfGlyphs = map.getGlyph();
		// ...
		List<Port> listOfPorts;
		List<Glyph> listOfContainingGlyphs;
		
		String id;
		Glyph.State state;
		Glyph.Clone clone;
		//Glyph.Callout callout;
		Glyph.Entity entity;
		Label label;
		Bbox bbox;
		String clazz; 
		String orientation;
		Object compartmentRef;
		Float compartmentOrder;
		
		Arc.Start start;
		List<Arc.Next> next;
		Arc.End end;
		Object source;
		Object target;
		
		System.out.println("sbgnml2sbml.SBGNML2SBML_GSOC2017.debugSbgnObject: \n");
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			listOfContainingGlyphs = arc.getGlyph();
			listOfPorts = arc.getPort();
			start = arc.getStart();
			next = arc.getNext();
			end = arc.getEnd();
			clazz = arc.getClazz();
			source = arc.getSource();
			target = arc.getTarget();
			
			System.out.format("arc id=%s clazz=%s start,end=%s \n"
					+ "next=%s source=%s target=%s \n "
					+ "source,target=%s \n \n",
					id, clazz, displayCoordinates(start, end),
					sizeOf(next, 3), isNull(source), isNull(target),
					displaySourceAndTarget(source, target));
		}
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			state = glyph.getState();
			clone = glyph.getClone();
			//callout = glyph.getCallout();
			entity = glyph.getEntity();
			label = glyph.getLabel();
			bbox = glyph.getBbox();
			listOfContainingGlyphs = glyph.getGlyph();
			listOfPorts = glyph.getPort();
			clazz = glyph.getClazz();
			orientation = glyph.getOrientation();
			compartmentRef = glyph.getCompartmentRef();
			compartmentOrder = glyph.getCompartmentOrder();
			
			System.out.format("glyph id=%s clazz=%s label=%s bbox=%s \n"
					+ "state=%s clone=%s callout=%s entity=%s \n"
					+ "listOfContainingGlyphs=%s listOfPorts=%s \n"
					+ "orientation=%s compartmentRef=%s compartmentOrder=%s \n \n",
					id, clazz, isNull(label), displayCoordinates(bbox), 
					isNull(state), isNull(clone), "isNull(callout)", isNull(entity),  
					sizeOf(listOfContainingGlyphs, 0), sizeOf(listOfPorts, 2), 
					orientation, isNull(compartmentRef), isNull(compartmentOrder));
			
		}
	}
	
	/**
	 * For debugging only
	 */
	public static String displaySourceAndTarget(Object source, Object target) {

		String type = "";
		if (source instanceof Glyph && target instanceof Port ) {
			type = "inward";
		}
		if (source instanceof Port && target instanceof Glyph ) {
			type = "outward";
		}	
		if (source instanceof Glyph && target instanceof Glyph ) {
			type = "undirected";
		}		
		
		return String.format("%s %s %s", source.getClass(), target.getClass(), type);		
	}
	
	/**
	 * For debugging only
	 */
	public static String displayCoordinates(Bbox bbox) {
		if (bbox == null) {
			return "[ ]";
		}
		String X = Float.toString(bbox.getX());
		String Y = Float.toString(bbox.getY());
		String W = Float.toString(bbox.getW());
		String H = Float.toString(bbox.getH());
			
		return String.format("[x=%s y=%s w=%s h=%s]", X, Y, W, H);
	}
	
	/**
	 * For debugging only
	 */
	public static String displayCoordinates(Arc.Start start, Arc.End end) {
		List<org.sbgn.bindings.Point> listOfPoints;
		int numOfPoints;
		
		if (start == null || end == null) {
			return "[ ]";
		}
		String startX = Float.toString(start.getX());
		String startY = Float.toString(start.getY());
		listOfPoints = end.getPoint();
		numOfPoints = listOfPoints.size();		
		String endX = Float.toString(end.getX());
		String endY = Float.toString(end.getY());
		
		return String.format("[(%s,%s) %s (%s,%s)]", startX, startY, Integer.toString(numOfPoints), endX, endY);
	}
	
	public static String isNull(Object o) {
		if  (o == null) {
			return "NULL";
		}
		return "NOT NULL";
	}
	
	/**
	 * For debugging only
	 */
	public static String sizeOf(Object list, int elementType) {		
		if (list != null) {
			if (elementType == 0) {
				List<Glyph> listOfGlyphs = (List<Glyph>) list;
				return Integer.toString(listOfGlyphs.size());
			} else if (elementType == 2) {
				List<Port> listOfPorts = (List<Port>) list;
				return Integer.toString(listOfPorts.size());
			} else if (elementType == 3) {
				List<Arc.Next> listOfNext = (List<Arc.Next>) list;
				int numOfPoints = 0;
				List<org.sbgn.bindings.Point> listOfPoints;
				for (Arc.Next next : listOfNext) {
					listOfPoints = next.getPoint();
					numOfPoints += listOfPoints.size();
				}
				return String.format("%s/%s", Integer.toString(listOfNext.size()), Integer.toString(numOfPoints));
			}			
		}
			
		return "0";
	}
	
	/**
	 * For debugging only
	 */
	public void printHelper(String source, String message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + message);
		}
	}

	/**
	 * For debugging only
	 */
	public void printHelper(String source, Integer message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + Integer.toString(message));
		}
	}	
	
//	public void displayReactionGlyphInfo(SWrapperModel sWrapperModel) {
//		for (String key : sWrapperModel.listOfWrapperReactionGlyphs.keySet()){
//			SWrapperReactionGlyph sWrapper = sWrapperModel.getWrapperReactionGlyph(key);
//			debugMode = 1;
//			printHelper(sWrapper.reactionId+"-inward", sWrapper.glyphToPortArcs.size());
//			printHelper(sWrapper.reactionId+"-outward", sWrapper.portToGlyphArcs.size());
//			printHelper(sWrapper.reactionId+"-undirected", sWrapper.glyphToGlyphArcs.size());
//			debugMode = 0;
//		}
//	}
	
	/**
	 * Read in a .sbgn file and create an Sbgn object
	 */
	public static Sbgn readSbgnFile(String sbgnFileNameInput) {
		Sbgn sbgnObject = null;
		File inputFile;
		
		inputFile = new File(sbgnFileNameInput);
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return sbgnObject;
	}
	
	/**
	 * Write a Model out to a .xml file
	 */
	public static void writeSbmlFile(String sbmlFileNameOutput, Model model) {
		File outputFile;
		outputFile = new File(sbmlFileNameOutput);
		
		SBMLWriter sbmlWriter;
		SBMLDocument sbmlDocument;
		
		sbmlWriter = new SBMLWriter();
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(model);
		
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

		
}
