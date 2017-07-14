package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Map;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperMap {
	Map map;
	Model model;
	
	HashMap<String, SpeciesGlyph> listOfWrapperSpeciesGlyphs;
	HashMap<String, CompartmentGlyph> listOfWrapperCompartmentGlyphs;
	HashMap<String, ReactionGlyph> listOfWrapperReactionGlyphs;
	HashMap<String, SpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	HashMap<String, GeneralGlyph> listOfWrapperGeneralGlyphs;
	HashMap<String, ReferenceGlyph> listOfWrapperReferenceGlyphs;
	HashMap<String, TextGlyph> listOfTextGlyphs;
	
	HashMap<String, SWrapperArc> listOfSWrapperArcs;
	HashMap<String, SWrapperArcGroup> listOfSWrapperArcGroups;
	HashMap<String, SWrapperGlyphEncapsulation> listOfSWrapperGlyphEncapsulations;
	HashMap<String, SWrapperGlyphEntityPool> listOfSWrapperGlyphEntityPools;
	HashMap<String, SWrapperGlyphProcess> listOfSWrapperGlyphProcesses;
	
}
