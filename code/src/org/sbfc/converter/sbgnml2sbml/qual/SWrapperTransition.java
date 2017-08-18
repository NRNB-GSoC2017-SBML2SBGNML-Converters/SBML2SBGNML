package org.sbfc.converter.sbgnml2sbml.qual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sbfc.converter.sbgnml2sbml.SWrapperArc;
import org.sbfc.converter.sbgnml2sbml.SWrapperGeneralGlyph;
import org.sbfc.converter.sbgnml2sbml.SWrapperModel;
import org.sbfc.converter.sbgnml2sbml.SWrapperReferenceGlyph;
import org.sbfc.converter.sbgnml2sbml.SWrapperSpeciesReferenceGlyph;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.qual.Transition;

public class SWrapperTransition {

	HashMap<String, Glyph> logicOperators = new HashMap<String, Glyph>();
	HashMap<String, SWrapperArc> logicArcs = new HashMap<String, SWrapperArc>();
	HashMap<String, SWrapperArc> modifierArcs = new HashMap<String, SWrapperArc>();
		
	public HashMap<String, SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs = 
			new HashMap<String, SWrapperGeneralGlyph>();
	public HashMap<String, SWrapperReferenceGlyph> listOfWrapperReferenceGlyphs = 
			new HashMap<String, SWrapperReferenceGlyph>();

	
	// assume there's only 1 modifier arc per transition in logic model
	public SWrapperReferenceGlyph outputModifierArc;
	public String outputClazz;
	public ASTNode rootFunctionTerm;
	
	SWrapperModel sWrapperModel;
	public String id;
	public Transition transition;
	
	public HashMap<String, SWrapperQualitativeSpecies> inputs = new HashMap<String, SWrapperQualitativeSpecies>();
	public HashMap<String, SWrapperQualitativeSpecies> outputs = new HashMap<String, SWrapperQualitativeSpecies>();
		
	
	public SWrapperTransition(String id, Transition transition, SWrapperGeneralGlyph sWrapperGeneralGlyph, Glyph glyph,
			SWrapperModel sWrapperModel) {
		this.id = id;
		this.transition = transition;	
		this.listOfWrapperGeneralGlyphs.put(sWrapperGeneralGlyph.id, sWrapperGeneralGlyph);
		this.logicOperators.put(glyph.getId(), glyph);
		this.sWrapperModel = sWrapperModel;
	}

	/**
	 * For special case, where there is only one Arc to map to Transition
	 * @param transition
	 * @param sWrapperGeneralGlyph
	 * @param arc
	 * @param sWrapperModel
	 */
	public SWrapperTransition(Transition transition, SWrapperGeneralGlyph sWrapperGeneralGlyph, SWrapperArc arc,
			SWrapperModel sWrapperModel) {
		this.id = transition.getId();
		this.transition = transition;
		this.listOfWrapperGeneralGlyphs.put(sWrapperGeneralGlyph.id, sWrapperGeneralGlyph);
		this.logicArcs.put(arc.arcId, arc);
		this.sWrapperModel = sWrapperModel;
	}
	
	public void addReference(SWrapperReferenceGlyph sWrapperReferenceGlyph, SWrapperArc sWrapperArc){
		this.listOfWrapperReferenceGlyphs.put(sWrapperReferenceGlyph.id, sWrapperReferenceGlyph);
		this.logicArcs.put(sWrapperArc.arcId, sWrapperArc);
		
		String sourceId = sWrapperArc.sourceId;
		String targetId = sWrapperArc.targetId;
		
		SWrapperQualitativeSpecies input = sWrapperModel.listOfSWrapperQualitativeSpecies.get(sourceId);
		SWrapperQualitativeSpecies output = sWrapperModel.listOfSWrapperQualitativeSpecies.get(targetId); 
		
		if (input != null){
			inputs.put(sourceId, input);
		} else if (output != null){
			outputs.put(targetId, output);
			
			outputClazz = sWrapperArc.arc.getClazz();
			outputModifierArc = sWrapperReferenceGlyph;
		}
		
	}
	
	
	public void addGeneralGlyph(String id, SWrapperGeneralGlyph sWrapperGeneralGlyph, Glyph glyph){
		this.listOfWrapperGeneralGlyphs.put(sWrapperGeneralGlyph.id, sWrapperGeneralGlyph);
		this.logicOperators.put(glyph.getId(), glyph);		
	}
	

}
