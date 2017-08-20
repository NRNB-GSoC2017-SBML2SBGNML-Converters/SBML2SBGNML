package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * Mapping SBML Species+SpeciesGlyph+TextGlyph->SBGN Logic OperatorGlyph
 * @author haoran
 *
 */
public class SWrapperLogicOperator {
	SpeciesGlyph speciesGlyph;
	Species species;
	TextGlyph textGlyph;
	
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	//logicArcs stores Arcs that has either Source or Target pointing to the Glyph, 
	//note that this extra information is not part of the Logic Operator Glyph
	//Mapping of SBML ReferenceGlyph->SBGN Arc
	HashMap<String, SWrapperArc> logicArcs;
}
