package org.sbfc.converter.sbml2sbgnml;

import org.sbgn.bindings.Glyph;

import org.sbml.jsbml.ext.layout.GeneralGlyph;


public class SWrapperAuxiliary {
	Glyph glyph;
	GeneralGlyph generalGlyph;
	String originalId;
	String clazz;
	
	SWrapperAuxiliary(Glyph glyph, GeneralGlyph generalGlyph, String id){
		this.glyph = glyph;
		this.generalGlyph = generalGlyph;
		this.originalId = id;
		this.clazz = glyph.getClazz();
	}
}
