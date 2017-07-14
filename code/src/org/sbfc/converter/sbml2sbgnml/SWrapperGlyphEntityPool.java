package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperGlyphEntityPool {
	Species species;
	SpeciesGlyph speciesGlyph;
	TextGlyph textGlyph;
	String clazz;
	
	Glyph glyph;
	HashMap<String, Glyph> glyphs;
}
