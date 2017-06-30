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
	ReferenceGlyph referenceGlyph;
	boolean isReferenceGlyph;
	
	SWrapperSpeciesReferenceGlyph(SpeciesReference speciesReference, SpeciesReferenceGlyph speciesReferenceGlyph, Arc arc) {
		this.speciesReference = speciesReference;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.arc = arc;
		
		isReferenceGlyph = false;
	}
	
	SWrapperSpeciesReferenceGlyph(ReferenceGlyph referenceGlyph, Arc arc) {
		this.referenceGlyph = referenceGlyph;
		this.arc = arc;
		
		isReferenceGlyph = true;
	}

}
