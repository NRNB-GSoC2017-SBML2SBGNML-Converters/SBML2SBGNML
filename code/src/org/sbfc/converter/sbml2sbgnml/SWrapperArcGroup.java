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
	
	// Process Node
	SWrapperArcGroup(String reactionId, Arcgroup arcGroup){
		this.reactionId = reactionId;
		this.arcGroup = arcGroup;
	}
}
