package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * Arcgroup consists of a collection of Arcs and Glyphs Mapping of one SBML->SBGN element works as follows:
 * (case 1) 
 * ReactionGlyph+Reaction+TextGlyph->Glyph 
 * SpeciesReferenceGlyph+SpeciesReference->Arc
 * (case 2)
 * ReactionGlyph+Reaction+TextGlyph->Glyph
 * SpeciesReferenceGlyph+ModifierSpeciesReference->Arc
 * (case 3)
 * GeneralGlyph+TextGlyph->Glyph
 * ReferenceGlyph->Arc
 * (case 1,2,3) A GeneralGlyph might store subglyphs, they will be stored in speciesGlyphs
 * 
 * @author haoran
 *
 */
public class SWrapperArcGroup {
	boolean isReactionGlyph;
	ReactionGlyph reactionGlyph;
	GeneralGlyph generalGlyph;
	Reaction reaction;
	
	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs;
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	HashMap<String, SpeciesReference> speciesReferences;
	HashMap<String, ModifierSpeciesReference> modifierSpeciesReferences;
	HashMap<String, TextGlyph> textGlyph; 
	HashMap<String, SpeciesGlyph> speciesGlyphs;
	
	Arcgroup arcGroup;
	
	HashMap<String, SWrapperArc> arcs;
	HashMap<String, Glyph> glyphs;
	
	String reactionId;
	
	/**
	 * The Arcgroup is ReactionGlyph converted to a Process Node glyph + all the Arcs it is associated with
	 * @param reactionId
	 * @param arcGroup
	 */
	SWrapperArcGroup(String reactionId, Arcgroup arcGroup){
		this.reactionId = reactionId;
		this.arcGroup = arcGroup;
	}
}
