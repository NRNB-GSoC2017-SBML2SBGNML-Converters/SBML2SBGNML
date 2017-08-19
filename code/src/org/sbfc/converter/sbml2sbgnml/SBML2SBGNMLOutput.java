package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

/**
 * SBML2SBGNMLOutput contains all data structures retrieved from the SBML Model and the lists to create the output Sbgn document.
 * @author haoran
 *
 */
public class SBML2SBGNMLOutput {
	
	Model sbmlModel;

	HashMap<String, Sbgn> listOfSbgnObjects = new HashMap<String, Sbgn>();	
	Sbgn sbgnObject = null;
	Map map = null;
			
	public ListOf<Compartment> listOfCompartments = null;
	ListOf<FunctionDefinition> listOfFunctionDefinitions = null;
	ListOf<UnitDefinition> listOfUnitDefinitions = null;
	ListOf<Species> listOfSpecies = null;
	ListOf<Parameter> listOfParameters = null;
	ListOf<InitialAssignment> listOfInitialAssignments = null;
	ListOf<Rule> listOfRules = null;
	ListOf<Constraint> listOfConstraints = null;
	ListOf<Reaction> listOfReactions = null;
	ListOf<Event> listOfEvents = null;
			
	LayoutModelPlugin sbmlLayoutModel = null;
	Dimensions layoutDimensions = null;
	ListOf<Layout> listOfLayouts = null;
	ListOf<GraphicalObject> listOfAdditionalGraphicalObjects = null;
	ListOf<CompartmentGlyph> listOfCompartmentGlyphs = null;
	ListOf<ReactionGlyph> listOfReactionGlyphs = null;
	ListOf<SpeciesGlyph> listOfSpeciesGlyphs = null;
	ListOf<TextGlyph> listOfTextGlyphs = null;		
		
	public Layout layout;
	
	QualModelPlugin qualModelPlugin = null;
	public ListOf<QualitativeSpecies> listOfQualitativeSpecies = null;
	public ListOf<Transition> listOfTransitions = null;
	
	public SBML2SBGNMLOutput(SBMLDocument sbmlDocument) {
		BasicConfigurator.configure();
		
		try { 
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2SBGN: Input file is not a regular SBML file.");
		}
		
		listOfCompartments = sbmlModel.getListOfCompartments();
		listOfSpecies = sbmlModel.getListOfSpecies();
		listOfReactions = sbmlModel.getListOfReactions();
		listOfFunctionDefinitions = sbmlModel.getListOfFunctionDefinitions();
		listOfUnitDefinitions = sbmlModel.getListOfUnitDefinitions();
		listOfParameters = sbmlModel.getListOfParameters();
		listOfInitialAssignments = sbmlModel.getListOfInitialAssignments();
		listOfRules = sbmlModel.getListOfRules();
		listOfConstraints = sbmlModel.getListOfConstraints();
		// in this converter, it is hard to represent the information stored in listOfEvents
		listOfEvents = sbmlModel.getListOfEvents();
		
		
		if (sbmlModel.isSetPlugin("qual")){
			qualModelPlugin = (QualModelPlugin) sbmlModel.getPlugin("qual");
			
			System.out.println(" getNumQualitativeSpecies " + qualModelPlugin.getNumQualitativeSpecies());
			System.out.println(" getNumTransitions " + qualModelPlugin.getNumTransitions());
			
			listOfQualitativeSpecies = qualModelPlugin.getListOfQualitativeSpecies();
			listOfTransitions = qualModelPlugin.getListOfTransitions();
			
		}
		
		if (sbmlModel.isSetPlugin("layout")){
			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
			listOfLayouts = sbmlLayoutModel.getListOfLayouts();
		}
		
		int numOfLayouts = 0;
		if (listOfLayouts == null){return;}
		
		for (Layout layout : listOfLayouts){
			
			// We only want to get the first Layout, ignore all other Layouts
			numOfLayouts++;
			if (numOfLayouts > 1){break;}
			
			sbgnObject = new Sbgn();
			map = new Map();
			sbgnObject.setMap(map);		
			
			listOfSbgnObjects.put(layout.getId(), sbgnObject);
			
			if (layout.isSetDimensions()){
				layoutDimensions = layout.getDimensions();
			}
			
			if (layout.isSetListOfCompartmentGlyphs()){
				listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();
				//System.out.println(" getNumCompartmentGlyphs " + layout.getNumCompartmentGlyphs());
			}			
			if (layout.isSetListOfSpeciesGlyphs()){
				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
				//System.out.println(" getNumSpeciesGlyphs " + layout.getNumSpeciesGlyphs());
			}
			if (layout.isSetListOfReactionGlyphs()){
				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
				//System.out.println(" getNumReactionGlyphs " + layout.getNumReactionGlyphs());
			}		
			if (layout.isSetListOfAdditionalGraphicalObjects()){
				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
				//System.out.println(" getListOfAdditionalGraphicalObjects " + layout.getListOfAdditionalGraphicalObjects().size());
			}			
			if (layout.isSetListOfTextGlyphs()){
				listOfTextGlyphs = layout.getListOfTextGlyphs();
				//System.out.println(" getNumTextGlyphs " + layout.getNumTextGlyphs());
			}	
			this.layout = layout;
		}
				
	}
	
	public void addGlyphToMap(Glyph glyph) {
		sbgnObject.getMap().getGlyph().add(glyph);
	}
	
	public void addArcToMap(Arc arc) {
		sbgnObject.getMap().getArc().add(arc);
	}
	
	public void addArcgroupToMap(Arcgroup arcgroup) {
		sbgnObject.getMap().getArcgroup().add(arcgroup);
	}
	
	public Model getModel(){
		return sbmlModel;
	}
	
}
