package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;

/**
 * Mapping SBGN Arc->SBML SpeciesReference+SpeciesReferenceGlyph
 * @author haoran
 *
 */
public class SWrapperSpeciesReferenceGlyph{
	SpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	Arc arc;
	SWrapperArc sWrapperArc;
	public String id;
	
	List<GraphicalObject> listOfGeneralGlyphs;
	
	SWrapperSpeciesReferenceGlyph(SpeciesReference speciesReference, SpeciesReferenceGlyph speciesReferenceGlyph, 
			SWrapperArc sWrapperArc) {
		this.id = sWrapperArc.arcId;
		this.speciesReference = speciesReference;
		this.speciesReferenceGlyph = speciesReferenceGlyph;
		this.arc = sWrapperArc.arc;
		this.sWrapperArc = sWrapperArc;
	}
	
}
