package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Species;
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
	
	TextGlyph textGlyph;		
	
	SWrapperSpeciesGlyph(Species species, SpeciesGlyph speciesGlyph, Glyph glyph) {
		this.species = species;
		this.speciesGlyph = speciesGlyph;		
		this.clazz = glyph.getClazz();
	}	
}
