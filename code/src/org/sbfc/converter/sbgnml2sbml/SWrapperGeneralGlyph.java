package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperGeneralGlyph {
	String clazz;
	GeneralGlyph generalGlyph;
	TextGlyph textGlyph;
	Glyph glyph;
	boolean hasParent;
	GraphicalObject parent;
		
	boolean glyphIsMissing;
	Arc arc;
	
	HashMap<String, Arc> arcs = new HashMap<String, Arc>();
	HashMap<String, ReferenceGlyph> referenceGlyphs = new HashMap<String, ReferenceGlyph>();
	
	List<GraphicalObject> listOfGeneralGlyphs;
	
	SWrapperModel sWrapperModel;
	
	boolean isAnnotation = false;
	Point calloutPoint;
	String calloutTarget;
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, GraphicalObject parent, TextGlyph textGlyph,
			SWrapperModel sWrapperModel) {
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = true;
		this.parent = parent;
		this.glyph = glyph;
		this.glyphIsMissing = false;
		this.textGlyph = textGlyph;
		
		this.sWrapperModel = sWrapperModel;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, TextGlyph textGlyph, SWrapperModel sWrapperModel){
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = false;
		this.glyph = glyph;	
		this.glyphIsMissing = false;
		this.textGlyph = textGlyph;
		
		this.sWrapperModel = sWrapperModel;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Arc arc, SWrapperModel sWrapperModel) {
		this.generalGlyph = generalGlyph;
		this.clazz = arc.getClazz();
		this.hasParent = false;
		this.glyphIsMissing = true;
		
		this.sWrapperModel = sWrapperModel;
	}
			
	void addSpeciesReferenceGlyph(String arcId, SWrapperReferenceGlyph sWrapperReferenceGlyph, Arc arc){
		referenceGlyphs.put(arcId, sWrapperReferenceGlyph.referenceGlyph);
		arcs.put(arcId, arc);
		this.sWrapperModel.addSWrapperReferenceGlyph(arcId, sWrapperReferenceGlyph);
	}
	
}
