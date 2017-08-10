package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;

public class SWrapperGlyphEntityPool {
	Species species;
	QualitativeSpecies qualitativeSpecies;
	SpeciesGlyph speciesGlyph;
	TextGlyph textGlyph;
	String clazz;
	String id;
	String parentId;
	HashMap<String, GraphicalObject> graphicalObjects;
	
	Glyph glyph;
	HashMap<String, Glyph> glyphs;
	
	SWrapperGlyphEntityPool(Glyph glyph, Species species, SpeciesGlyph speciesGlyph){
		this.glyph = glyph;
		this.species = species;
		this.speciesGlyph = speciesGlyph;
		this.id = species.getId();
	}
	
	SWrapperGlyphEntityPool(Glyph glyph, QualitativeSpecies species, SpeciesGlyph speciesGlyph){
		this.glyph = glyph;
		this.qualitativeSpecies = species;
		this.speciesGlyph = speciesGlyph;
		this.id = species.getId();
	}
	
	void setParentId(String id){
		this.parentId = id;
	}
}
