package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class SWrapperCompartmentGlyph {
	
	Compartment compartment;
	CompartmentGlyph compartmentGlyph;
	Glyph glyph;
	
	SWrapperCompartmentGlyph(Compartment compartment, CompartmentGlyph compartmentGlyph, Glyph glyph) {
		this.compartment = compartment;
		this.compartmentGlyph = compartmentGlyph;		
		this.glyph = glyph;
	}
	
}
