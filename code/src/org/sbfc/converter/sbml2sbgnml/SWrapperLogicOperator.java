package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperLogicOperator {
	SpeciesGlyph speciesGlyph;
	Species species;
	TextGlyph textGlyph;
	
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	
	HashMap<String, SWrapperArc> logicArcs;
}
