package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SBML2SBGNMLOutput {
	
	Model sbmlModel;
	LayoutModelPlugin sbmlLayoutModel = null;
	ListOf<Layout> listOfLayouts = null;
	
	HashMap<String, Sbgn> listOfSbgnObjects = new HashMap<String, Sbgn>();	
	Sbgn sbgnObject = null;
	Map map = null;
		
	Dimensions layoutDimensions = null;
	ListOf<GraphicalObject> listOfAdditionalGraphicalObjects;
	ListOf<CompartmentGlyph> listOfCompartmentGlyphs;
	ListOf<ReactionGlyph> listOfReactionGlyphs;
	ListOf<SpeciesGlyph> listOfSpeciesGlyphs;
	ListOf<TextGlyph> listOfTextGlyphs;		
	
	SBML2SBGNMLOutput(SBMLDocument sbmlDocument) {
		BasicConfigurator.configure();
		
		try { 
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2SBGN: Input file is not a regular SBML file.");
		}
		
		if (sbmlModel.isSetPlugin("layout")){
			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
		}

		if (sbmlModel.isSetPlugin("layout")){
			listOfLayouts = sbmlLayoutModel.getListOfLayouts();
		}
		
		int numOfLayouts = 0;
		
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
							}			
			if (layout.isSetListOfSpeciesGlyphs()){
				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
							}
			if (layout.isSetListOfAdditionalGraphicalObjects()){
				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
							}			
			if (layout.isSetListOfTextGlyphs()){
				listOfTextGlyphs = layout.getListOfTextGlyphs();
							}			
			if (layout.isSetListOfReactionGlyphs()){
				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
			}			
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
	
}
