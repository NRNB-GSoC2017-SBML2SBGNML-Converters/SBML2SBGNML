package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperModifierSpeciesReferenceGlyph extends SWrapperSpeciesReferenceGlyph {
	ModifierSpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	Arc arc;
	
	SWrapperModifierSpeciesReferenceGlyph(ModifierSpeciesReference speciesReference, 
			SpeciesReferenceGlyph speciesReferenceGlyph, Arc arc) {
		super(new SpeciesReference(), speciesReferenceGlyph, arc);
		this.speciesReference = speciesReference;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.arc = arc;
	}
}
