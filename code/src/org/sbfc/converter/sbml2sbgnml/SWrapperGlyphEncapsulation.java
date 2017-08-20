package org.sbfc.converter.sbml2sbgnml;

import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;

/**
 * Mapping SBML Compartment+CompartmentGlyph->SBGN Glyph
 * @author haoran
 *
 */
public class SWrapperGlyphEncapsulation {
	Compartment compartment;
	CompartmentGlyph compartmentGlyph;
	Glyph glyph;
}
