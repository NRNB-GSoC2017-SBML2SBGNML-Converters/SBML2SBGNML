package org.sbfc.converter.sbgnml2sbml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperReactionGlyph {
	String reactionId;
	Reaction reaction;
	ReactionGlyph reactionGlyph;
	String clazz;
	
	HashMap<String, Arc> glyphToPortArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> portToGlyphArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> glyphToGlyphArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> modifierArcs = new HashMap<String, Arc>();

	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs = new HashMap<String, SpeciesReferenceGlyph>();
	
	List<Point> listOfEndPoints = new ArrayList<Point>();
	
	SWrapperModel sWrapperModel;
	
	SWrapperReactionGlyph(Reaction reaction, ReactionGlyph reactionGlyph, Glyph glyph, SWrapperModel sWrapperModel) {
		this.reactionId = reaction.getId();
		this.reaction = reaction;
		this.reactionGlyph = reactionGlyph;	
		this.clazz = glyph.getClazz();
		this.sWrapperModel = sWrapperModel;
	}		

	void addArc(String arcId, Arc arc, String type) {
		if (type == "glyphToPort"){
			glyphToPortArcs.put(arcId, arc);
		} else if (type == "portToGlyph"){
			portToGlyphArcs.put(arcId, arc);
		} else if (type == "glyphToGlyph"){
			glyphToGlyphArcs.put(arcId, arc);
		} else if (type == "modifierArcs") {
			modifierArcs.put(arcId, arc);
		}
	}
		
	void addSpeciesReferenceGlyph(String arcId, SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph){
		speciesReferenceGlyphs.put(arcId, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
		this.sWrapperModel.addSWrapperSpeciesReferenceGlyph(arcId, sWrapperSpeciesReferenceGlyph);
	}
	
	Arc getArc(String arcId) {
		if (glyphToPortArcs.get(arcId) != null){
			return glyphToPortArcs.get(arcId);
		} else if (portToGlyphArcs.get(arcId) != null){
			return portToGlyphArcs.get(arcId);
		} else if (glyphToGlyphArcs.get(arcId) != null){
			return glyphToGlyphArcs.get(arcId);
		} else if (modifierArcs.get(arcId) != null){
			return modifierArcs.get(arcId);
		}		
		return null;
	}
	
	void addPoint(Point point) {
		listOfEndPoints.add(point);
	}
}
