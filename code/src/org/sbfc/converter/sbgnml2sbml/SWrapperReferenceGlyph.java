package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;

public class SWrapperReferenceGlyph {
	Arc arc;
	ReferenceGlyph referenceGlyph;
	
	SWrapperReferenceGlyph(ReferenceGlyph referenceGlyph, Arc arc) {
		this.referenceGlyph = referenceGlyph;
		this.arc = arc;
	}
}
