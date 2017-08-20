package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

/**
 * Mapping SBGN Arc->SBML ModifierSpeciesReference+SpeciesReferenceGlyph
 * @author haoran
 *
 */
public class SWrapperModifierSpeciesReferenceGlyph extends SWrapperSpeciesReferenceGlyph {
	ModifierSpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	
	SWrapperModifierSpeciesReferenceGlyph(ModifierSpeciesReference speciesReference, 
			SpeciesReferenceGlyph speciesReferenceGlyph, SWrapperArc sWrapperArc) {
		super(new SpeciesReference(), speciesReferenceGlyph, sWrapperArc);
		this.speciesReference = speciesReference;
	}
}
