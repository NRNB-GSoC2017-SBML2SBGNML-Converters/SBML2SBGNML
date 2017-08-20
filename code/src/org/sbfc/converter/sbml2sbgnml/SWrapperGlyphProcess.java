package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * Mapping SBML Reaction+ReactionGlyph+TextGlyph->SBGN Process Glyph
 * @author haoran
 *
 */
public class SWrapperGlyphProcess {
	ReactionGlyph reactionGlyph;
	Reaction reaction;
	TextGlyph textGlyph;
	
	SWrapperArcGroup processNode;
	Glyph processNodeGlyph;
	String id;
	
	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs;
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	HashMap<String, SpeciesReference> speciesReferences;
	HashMap<String, ModifierSpeciesReference> modifierSpeciesReferences;
	
	/*
	 * arcs stores Arcs that has either Source or Target pointing to the Glyph, 
	 * note that this extra information is not part of the Process Glyph
	 * Mapping of SBML->SBGN works as follows:
	 * (case 1) SpeciesReferenceGlyph+SpeciesReference->Arc 
	 * (case 2) SpeciesReferenceGlyph+ModifierSpeciesReference->Arc 
	 * (case 3) ReferenceGlyph->Arc
	 */
	HashMap<String, SWrapperArc> arcs;

	SWrapperGlyphProcess(SWrapperArcGroup processNode, ReactionGlyph reactionGlyph, Reaction reaction, TextGlyph textGlyph, Glyph processNodeGlyph) {
		this.processNode = processNode;
		this.reactionGlyph = reactionGlyph;
		this.reaction = reaction;
		this.textGlyph = textGlyph;
		this.id = processNode.reactionId;
		this.processNodeGlyph = processNodeGlyph;
	}
}
