package org.sbfc.converter.sbgnml2sbml.qual;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;

public class SWrapperQualitativeSpecies {
	public QualitativeSpecies qualitativeSpecies;
	public SpeciesGlyph speciesGlyph;
	Glyph glyph;
	TextGlyph textGlyph;
	public String clazz;
	
	public SWrapperQualitativeSpecies(QualitativeSpecies qualitativeSpecies, SpeciesGlyph speciesGlyph, 
			Glyph glyph, TextGlyph textGlyph) {
		this.qualitativeSpecies = qualitativeSpecies;
		this.speciesGlyph = speciesGlyph;
		this.glyph = glyph;
		this.textGlyph = textGlyph;
		this.clazz = glyph.getClazz();
	}
}
