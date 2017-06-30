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
	
	HashMap<String, Arc> glyphToPortArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> portToGlyphArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> glyphToGlyphArcs = new HashMap<String, Arc>();
	
	HashMap<String, ReferenceGlyph> referenceGlyphs = new HashMap<String, ReferenceGlyph>();
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, GraphicalObject parent) {
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = true;
		this.parent = parent;
		this.glyph = glyph;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph){
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = false;
		this.glyph = glyph;		
	}
	
	void addArc(String arcId, Arc arc, String type) {
		if (type == "glyphToPort"){
			glyphToPortArcs.put(arcId, arc);
		} else if (type == "portToGlyph"){
			portToGlyphArcs.put(arcId, arc);
		} else if (type == "glyphToGlyph"){
			glyphToGlyphArcs.put(arcId, arc);
		} else {}
	}
		
	void addSpeciesReferenceGlyph(String arcId, ReferenceGlyph referenceGlyph){
		referenceGlyphs.put(arcId, referenceGlyph);
	}
}
