package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;

public class SWrapperReferenceGlyph{
	Arc arc;
	ReferenceGlyph referenceGlyph;
	SWrapperArc sWrapperArc;
	
	SWrapperReferenceGlyph(ReferenceGlyph referenceGlyph, SWrapperArc sWrapperArc) {
		this.referenceGlyph = referenceGlyph;
		this.arc = sWrapperArc.arc;
		this.sWrapperArc = sWrapperArc;
	}
}
