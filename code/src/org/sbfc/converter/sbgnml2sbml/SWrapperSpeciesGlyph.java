package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * Mapping SBGN Glyph->SBML Species+SpeciesGlyph+TextGlyph
 * @author haoran
 *
 */
public class SWrapperSpeciesGlyph {
	Species species;
	public SpeciesGlyph speciesGlyph;
	String clazz;
	String id;
	
	Glyph sbgnGlyph;
	
	// not all of these attributes are used
	boolean hasPort = false;
	boolean hasExtension = false;
	
	boolean hasLabel = false;
	String labelText = "";
	
	boolean hasAuxillaryUnits = false;
	//listOfGlyphs stores all Glyphs that are nested inside, if the structure is recursive, the structure is now flattened
	List<GraphicalObject> listOfGlyphs;
	
	TextGlyph textGlyph;
	
	boolean hasClone = false;
	String cloneText = "";
	
	public SWrapperSpeciesGlyph(Species species, SpeciesGlyph speciesGlyph, Glyph glyph, TextGlyph textGlyph) {
		this.species = species;
		this.id = glyph.getId();
		this.speciesGlyph = speciesGlyph;	
		this.clazz = glyph.getClazz();
		this.sbgnGlyph = glyph;
		
		// clazz becomes "clazz_orientation", so that we know how to render the layout Glyph later
		if (this.clazz.equals("tag")){
			this.clazz = glyph.getClazz()+"_"+glyph.getOrientation();
		}
		if (this.clazz.equals("terminal")){
			this.clazz = glyph.getClazz()+"_"+glyph.getOrientation(); 
		}
		
		this.textGlyph = textGlyph;
	}	

	public void setListOfNestedGlyphs(List<GraphicalObject> listOfGeneralGlyphs) {
		this.listOfGlyphs = listOfGeneralGlyphs;
	}
	
	public void setCloneText(String text) {
		if (text == null){
			this.hasClone = false;
		}
		// clazz becomes clazz_clone, so that we know how to render the layout Glyph
		else {
			this.hasClone = true;
			this.cloneText = text;
			this.clazz = this.clazz + "_" + "clone";				
		}	
	}
	
}
