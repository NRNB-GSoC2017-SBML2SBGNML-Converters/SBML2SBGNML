package org.sbfc.converter.sbml2sbgnml;

import java.util.HashMap;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
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
	HashMap<String, SWrapperAuxiliary> listOfSWrapperAuxiliary;
	
	// all the child glyphs (e.g. unit of information) that are going to be added to they parent in the end of the conversion
	HashMap<String, String> notAdded = new HashMap<String, String>();
	
	// all the glyphs added to the Map
	HashMap<String, Glyph> listOfGlyphs = new HashMap<String, Glyph>();
	
	SWrapperMap(Map map, Model model){
		this.map = map;
		this.model = model;
		
		listOfWrapperSpeciesGlyphs = new HashMap<String, SpeciesGlyph>();
		listOfWrapperCompartmentGlyphs = new HashMap<String, CompartmentGlyph>();
		listOfWrapperReactionGlyphs = new HashMap<String, ReactionGlyph>();
		listOfWrapperSpeciesReferenceGlyphs = new HashMap<String, SpeciesReferenceGlyph>();
		listOfWrapperGeneralGlyphs = new HashMap<String, GeneralGlyph>();
		listOfWrapperReferenceGlyphs = new HashMap<String, ReferenceGlyph>();
		listOfTextGlyphs = new HashMap<String, TextGlyph>();
		
		listOfSWrapperArcs = new HashMap<String, SWrapperArc>();
		listOfSWrapperArcGroups = new HashMap<String, SWrapperArcGroup>();
		listOfSWrapperGlyphEncapsulations = new HashMap<String, SWrapperGlyphEncapsulation>();
		listOfSWrapperGlyphEntityPools = new HashMap<String, SWrapperGlyphEntityPool>();
		listOfSWrapperGlyphProcesses = new HashMap<String, SWrapperGlyphProcess>();
		listOfSWrapperAuxiliary = new HashMap<String, SWrapperAuxiliary>();
	}

	/**
	 * Find the glyph with the id
	 * @param id
	 * @return
	 */
	Glyph getGlyph(String id){
		for (String key : listOfSWrapperGlyphEntityPools.keySet()){
			if (key.equals(id)){
				return listOfSWrapperGlyphEntityPools.get(key).glyph;
			}
		}
		
		for (String key : listOfSWrapperArcGroups.keySet()){
			for (Glyph g : listOfSWrapperArcGroups.get(key).arcGroup.getGlyph()) {
				if (g.getId().equals(id)){
					return g;
				}
			}
		}
		
		for (String key : listOfSWrapperAuxiliary.keySet()){
			if (key.equals(id)){
				System.out.println("listOfSWrapperAuxiliary.get(key)"+listOfSWrapperAuxiliary.get(key).glyph.getId());
				return listOfSWrapperAuxiliary.get(key).glyph;
			}
		}
		return null;
	}

	/**
	 * Find the arc with the id
	 * @param id
	 * @return
	 */
	public Arc getArc(String id) {
		for (String key : listOfSWrapperArcs.keySet()){
			if (key.equals(id)){
				System.out.println("listOfSWrapperArcs.get(key)"+listOfSWrapperArcs.get(key).sourceTargetType+" "+listOfSWrapperArcs.get(key).arc.getId());
				return listOfSWrapperArcs.get(key).arc;
			}
		}
		
		for (String key : listOfSWrapperArcGroups.keySet()){
			for (Arc a : listOfSWrapperArcGroups.get(key).arcGroup.getArc()) {
				if (a.getId().equals(id)){
					return a;
				}
			}
		}		
		return null;
	}
}
