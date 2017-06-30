package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;

public class SWrapperGeneralGlyph {
	String clazz;
	GeneralGlyph generalGlyph;
	GraphicalObject parent;
	Glyph glyph;
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, GraphicalObject parent) {
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.parent = parent;
		this.glyph = glyph;
	}
}
