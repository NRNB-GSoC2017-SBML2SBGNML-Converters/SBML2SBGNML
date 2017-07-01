package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperGeneralGlyph {
	String clazz;
	GeneralGlyph generalGlyph;
	Glyph glyph;
	boolean hasParent;
	GraphicalObject parent;
	boolean glyphIsMissing;
	Arc arc;
	
	HashMap<String, Arc> arcs = new HashMap<String, Arc>();
	HashMap<String, ReferenceGlyph> referenceGlyphs = new HashMap<String, ReferenceGlyph>();
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, GraphicalObject parent) {
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = true;
		this.parent = parent;
		this.glyph = glyph;
		this.glyphIsMissing = false;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph){
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = false;
		this.glyph = glyph;	
		this.glyphIsMissing = false;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Arc arc) {
		this.generalGlyph = generalGlyph;
		this.clazz = arc.getClazz();
		this.hasParent = false;
		this.glyphIsMissing = true;
	}
			
	void addSpeciesReferenceGlyph(String arcId, ReferenceGlyph referenceGlyph, Arc arc){
		referenceGlyphs.put(arcId, referenceGlyph);
		arcs.put(arcId, arc);
	}
}
