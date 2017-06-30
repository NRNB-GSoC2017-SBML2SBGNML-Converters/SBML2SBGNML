package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperSpeciesGlyph {
	Species species;
	SpeciesGlyph speciesGlyph;
	String clazz;
	
	Glyph sbgnGlyph;
	
	boolean hasPort = false;
	boolean hasNestedGlyph = false;
	boolean hasExtension = false;
	boolean hasLabel = false;
	String labelText = "";
	boolean hasAuxillaryUnits = false;
	List<GeneralGlyph> listOfGeneralGlyphs;
	
	TextGlyph textGlyph;		
	
	SWrapperSpeciesGlyph(Species species, SpeciesGlyph speciesGlyph, Glyph glyph) {
		this.species = species;
		this.speciesGlyph = speciesGlyph;		
		this.clazz = glyph.getClazz();
	}	

	public void setListOfGeneralGlyphs(List<GeneralGlyph> listOfGeneralGlyphs) {
		this.listOfGeneralGlyphs = listOfGeneralGlyphs;
	}
}
