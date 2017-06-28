package org.sbfc.converter.sbgnml2sbml;

import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.Model;

public class SWrapperModel {

	private List<SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	private List<SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	private List<SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	private List<SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	
	Model sbmlModel;
	
	SWrapperModel() {
		listOfWrapperSpeciesGlyphs = new ArrayList<SWrapperSpeciesGlyph>();
		listOfWrapperCompartmentGlyphs = new ArrayList<SWrapperCompartmentGlyph>();
		listOfWrapperReactionGlyphs = new ArrayList<SWrapperReactionGlyph>();
		listOfWrapperSpeciesReferenceGlyphs = new ArrayList<SWrapperSpeciesReferenceGlyph>();
		
		sbmlModel = new Model(3, 1);
	}
	
	public Model getModel() {
		return sbmlModel;
	}
	
	public List<SWrapperSpeciesGlyph> getListOfWrapperSpeciesGlyphs() {
		return listOfWrapperSpeciesGlyphs;
	}
	public List<SWrapperCompartmentGlyph> getListOfWrapperCompartmentGlyphs() {
		return listOfWrapperCompartmentGlyphs;
	}
	public List<SWrapperReactionGlyph> getListOfWrapperReactionGlyphs() {
		return listOfWrapperReactionGlyphs;
	}
}
