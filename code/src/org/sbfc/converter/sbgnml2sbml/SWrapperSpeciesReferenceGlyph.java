package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperSpeciesReferenceGlyph{
	SpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	Arc arc;
	SWrapperArc sWrapperArc;
	
	SWrapperSpeciesReferenceGlyph(SpeciesReference speciesReference, SpeciesReferenceGlyph speciesReferenceGlyph, 
			SWrapperArc sWrapperArc) {
		this.speciesReference = speciesReference;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.arc = sWrapperArc.arc;
		this.sWrapperArc = sWrapperArc;
	}
	
}
