package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;


public class SWrapperCompartmentGlyph {
	
	Compartment compartment;
	CompartmentGlyph compartmentGlyph;
	Glyph glyph;
	String clazz;
	Float compartmentOrder;
	
	SWrapperCompartmentGlyph(Compartment compartment, CompartmentGlyph compartmentGlyph, Glyph glyph) {
		this.compartment = compartment;
		this.compartmentGlyph = compartmentGlyph;		
		this.glyph = glyph;
		this.clazz = glyph.getClazz();
		this.compartmentOrder = glyph.getCompartmentOrder() != null ? glyph.getCompartmentOrder() : 0;
	}
	
}
