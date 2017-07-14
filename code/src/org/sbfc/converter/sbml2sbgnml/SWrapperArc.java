package org.sbfc.converter.sbml2sbgnml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperArc {
	boolean isSpeciesReferenceGlyph;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	ReferenceGlyph referenceGlyph;
	
	boolean hasSpeciesReference;
	SpeciesReference speciesReference;
	ModifierSpeciesReference modifierSpeciesReference;
	
	Arc arc;
}
