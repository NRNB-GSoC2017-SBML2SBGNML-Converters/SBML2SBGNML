package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperReactionGlyph {
	String reactionId;
	Reaction reaction;
	ReactionGlyph reactionGlyph;
	GeneralGlyph generalGlyph;
	boolean isGeneralGlyph = false;
	String clazz;
	
	HashMap<String, Arc> inwardArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> outwardArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> undirectedArcs = new HashMap<String, Arc>();
	HashMap<String, Arc> bidirectedArcs = new HashMap<String, Arc>();
	
	HashMap<String, ReferenceGlyph> referenceGlyphs = new HashMap<String, ReferenceGlyph>();
	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs = new HashMap<String, SpeciesReferenceGlyph>();
	
	SWrapperReactionGlyph(Reaction reaction, ReactionGlyph reactionGlyph, Glyph glyph) {
		reactionId = reaction.getId();
		this.reaction = reaction;
		this.reactionGlyph = reactionGlyph;	
		clazz = glyph.getClazz();
	}		
	
	SWrapperReactionGlyph(Reaction reaction, GeneralGlyph generalGlyph, Glyph glyph) {
		reactionId = reaction.getId();
		this.reaction = reaction;
		this.generalGlyph = generalGlyph;	
		this.isGeneralGlyph = true;
		clazz = glyph.getClazz();
	}		
	
	void addArc(String id, Arc arc, String type) {
		if (type == "inward"){
			inwardArcs.put(id, arc);
		} else if (type == "outward"){
			outwardArcs.put(id, arc);
		} else if (type == "undirected"){
			undirectedArcs.put(id, arc);
		} else if (type == "bidirected"){
			bidirectedArcs.put(id, arc);
		} else {}
	}
	
	void addReferenceGlyph(String id, ReferenceGlyph referenceGlyph){
		referenceGlyphs.put(id, referenceGlyph);
	}
	
	void addSpeciesReferenceGlyph(String id, SpeciesReferenceGlyph speciesReferenceGlyph){
		speciesReferenceGlyphs.put(id, speciesReferenceGlyph);
	}
}
