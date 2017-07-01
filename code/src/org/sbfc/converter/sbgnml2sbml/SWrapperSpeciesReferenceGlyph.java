package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperSpeciesReferenceGlyph {
	SpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	Arc arc;
	
	SWrapperSpeciesReferenceGlyph(SpeciesReference speciesReference, SpeciesReferenceGlyph speciesReferenceGlyph, Arc arc) {
		this.speciesReference = speciesReference;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.arc = arc;
	}
	
}
