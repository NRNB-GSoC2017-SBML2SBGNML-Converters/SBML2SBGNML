package org.sbfc.converter.sbml2sbgnml;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

public class SWrapperArc {
	// does the SWrapperArc contain a SpeciesReferenceGlyph or a ReferenceGlyph?
	boolean isSpeciesReferenceGlyph = false;
	SpeciesReferenceGlyph speciesReferenceGlyph = null;
	ReferenceGlyph referenceGlyph = null;
	
	// does the SWrapperArc contain a SpeciesReference or a ModifierSpeciesReference?
	boolean hasSpeciesReference = false;
	SpeciesReference speciesReference = null;
	boolean hasModifierSpeciesReference = false;
	ModifierSpeciesReference modifierSpeciesReference = null;
	
	Arc arc;
	String id;
	String clazz;
	String reference; // this id could be the "source" or "target" of the arc
	String glyph; // this id could be the "source" or "target" of the arc
	
	// The value must be one of "reactionToSpecies", "speciesToReaction"
	String sourceTargetType;
	
	// The value must be one of "speciesReferenceGlyph+speciesReference", "speciesReferenceGlyph+modifierSpeciesReference", "referenceGlyph"
	String elementType;
	
	SWrapperArc(Arc arc, SpeciesReferenceGlyph speciesReferenceGlyph, SpeciesReference speciesReference){
		this.isSpeciesReferenceGlyph = true;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.hasSpeciesReference = true;
		this.speciesReference = speciesReference;
		
		this.arc = arc;
		this.id = speciesReference.getId();
		this.elementType = "speciesReferenceGlyph+speciesReference";
	}
	
	public SWrapperArc(Arc arc, SpeciesReferenceGlyph speciesReferenceGlyph, ModifierSpeciesReference modifierSpeciesReference) {
		this.isSpeciesReferenceGlyph = true;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.hasModifierSpeciesReference = true;
		this.modifierSpeciesReference = modifierSpeciesReference;
		
		this.arc = arc;
		this.id = modifierSpeciesReference.getId();
		this.elementType = "speciesReferenceGlyph+modifierSpeciesReference";
	}

	public void setSourceTarget(String reference, String glyph, String sourceTargetType){
		this.reference = reference;
		this.glyph = glyph;
		this.sourceTargetType = sourceTargetType;
	}
}
