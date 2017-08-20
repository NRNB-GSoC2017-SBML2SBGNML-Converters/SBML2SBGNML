package org.sbfc.converter.sbgnml2sbml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

/**
 * Mapping SBGN Glyph->SBML Reaction+ReactionGlyph
 * This class also stores Arcs and the mapped SpeciesReferenceGlyph that this Reaction is associated with.
 * @author haoran
 *
 */
public class SWrapperReactionGlyph {
	String reactionId;
	Reaction reaction;
	ReactionGlyph reactionGlyph;
	String clazz;
	Glyph glyph;
	
	HashMap<String, SWrapperArc> consumptionArcs = new HashMap<String, SWrapperArc>();
	HashMap<String, SWrapperArc> productionArcs = new HashMap<String, SWrapperArc>();
	HashMap<String, SWrapperArc> modifierArcs = new HashMap<String, SWrapperArc>();

	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs = new HashMap<String, SpeciesReferenceGlyph>();
	
	// useful if we want to create the center Curve of the reactionGlyph later
	// listOfEndPoints and listOfStartPoints are the Points that touch the ReactionGlyph
	List<Point> listOfEndPoints = new ArrayList<Point>();
	List<Point> listOfStartPoints = new ArrayList<Point>();
	
	SWrapperModel sWrapperModel;
	
	SWrapperReactionGlyph(Reaction reaction, ReactionGlyph reactionGlyph, Glyph glyph, SWrapperModel sWrapperModel) {
		this.reactionId = reaction.getId();
		this.reaction = reaction;
		this.reactionGlyph = reactionGlyph;	
		this.clazz = glyph.getClazz();
		this.sWrapperModel = sWrapperModel;
		this.glyph = glyph;
	}		

	void addArc(String arcId, SWrapperArc arc, String type) {
		if (type == "consumption"){
			consumptionArcs.put(arcId, arc);
		} else if (type == "production"){
			productionArcs.put(arcId, arc);
		} else if (type == "modifierArcs") {
			modifierArcs.put(arcId, arc);
		}
	}
		
	void addSpeciesReferenceGlyph(String arcId, SWrapperSpeciesReferenceGlyph sWrapperSpeciesReferenceGlyph){
		speciesReferenceGlyphs.put(arcId, sWrapperSpeciesReferenceGlyph.speciesReferenceGlyph);
		this.sWrapperModel.addSWrapperSpeciesReferenceGlyph(arcId, sWrapperSpeciesReferenceGlyph);
	}
	
	Arc getArc(String arcId) {
		if (consumptionArcs.get(arcId) != null){
			return consumptionArcs.get(arcId).arc;
		} else if (productionArcs.get(arcId) != null){
			return productionArcs.get(arcId).arc;
		} else if (modifierArcs.get(arcId) != null){
			return modifierArcs.get(arcId).arc;
		}		
		return null;
	}
	
	void addEndPoint(Point point) {
		listOfEndPoints.add(point);
	}
	
	void addStartPoint(Point point) {
		listOfStartPoints.add(point);
	}
}
