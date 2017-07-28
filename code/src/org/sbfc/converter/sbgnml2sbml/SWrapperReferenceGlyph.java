package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;

public class SWrapperReferenceGlyph{
	Arc arc;
	ReferenceGlyph referenceGlyph;
	public SWrapperArc sWrapperArc;
	public String id;
	
	List<GraphicalObject> listOfGeneralGlyphs;
	
	SWrapperReferenceGlyph(ReferenceGlyph referenceGlyph, SWrapperArc sWrapperArc) {
		this.id = sWrapperArc.arcId;
		this.referenceGlyph = referenceGlyph;
		this.arc = sWrapperArc.arc;
		this.sWrapperArc = sWrapperArc;
	}
}
