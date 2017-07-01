package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.LineEnding;
import org.sbml.jsbml.ext.render.ListOfLocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.Style;

public class SBGNML2SBMLOutput {
	Model model;
	
	LayoutModelPlugin layoutPlugin;
	Layout layout;
	
	RenderLayoutPlugin renderLayoutPlugin;
	ListOfLocalRenderInformation listOfLocalRenderInformation;
	LocalRenderInformation localRenderInformation;
	
	ListOf<ColorDefinition> listOfColorDefinitions;
	ListOf<LineEnding> listOfLineEndings;	
	ListOf<Style> listOfStyles;
	
	// keep track of the maximum value for each dimension. Finally, set these 3 values as the dimensions of the layout
	Double dimensionX;
	Double dimensionY;
	Double dimensionZ;
	Dimensions dimensions;
	
	SBGNML2SBMLOutput(int level, int version) {
		this.model = new Model(level, version);
		createLayout();
		createRenderInformation();
					
		this.dimensionX = 0.0;
		this.dimensionY = 0.0;
		this.dimensionZ = 0.0;		
		
	}
	
	public Model getModel() {
		return model;
	}	
	
	private void createLayout() {
		this.layoutPlugin = (LayoutModelPlugin) model.getPlugin("layout");
		this.layout = layoutPlugin.createLayout();		
	}
	
	private void createRenderInformation() {
		this.renderLayoutPlugin = (RenderLayoutPlugin) layout.getPlugin(RenderConstants.shortLabel);
		this.localRenderInformation = new LocalRenderInformation("LocalRenderInformation_01");
		this.renderLayoutPlugin.addLocalRenderInformation(localRenderInformation);		
		this.listOfLocalRenderInformation = renderLayoutPlugin.getListOfLocalRenderInformation();		
	}
	
	public void addListOfColorDefinitions(ListOf<ColorDefinition> listOfColorDefinitions) {
		this.listOfColorDefinitions = listOfColorDefinitions.clone();
	}
	public void addListOfLineEndings(ListOf<LineEnding> listOfLineEndings) {
		this.listOfLineEndings = listOfLineEndings.clone();
	}
	
	/**
	 * Set the dimensionX, dimensionY, dimensionZ from the <code>BoundingBox</code> values.
	 * 
	 * @param <code>BoundingBox</code> boundingBox
	 */		
	public void updateDimensions(BoundingBox boundingBox) {
		Dimensions dimensions;
		Point point;
		
		dimensions = boundingBox.getDimensions();
		point = boundingBox.getPosition();
		
		if (point.getX() + dimensions.getWidth() > this.dimensionX) {
			this.dimensionX = point.getX() + dimensions.getWidth();
		}
		if (point.getY() + dimensions.getHeight() > this.dimensionY) {
			this.dimensionY = point.getY() + dimensions.getHeight();
		}
		if (point.getZ() + dimensions.getDepth() > this.dimensionZ) {
			this.dimensionZ = point.getZ() + dimensions.getDepth();
		}
	}	
	
	/**
	 * Set the dimensionX, dimensionY, dimensionZ from the <code>Point</code> values.
	 * 
	 * @param <code>Point</code> point
	 */			
	public void updateDimensions(Point point) {
		if (point.getX() > this.dimensionX) {
			this.dimensionX = point.getX();
		}
		if (point.getY() > this.dimensionY) {
			this.dimensionY = point.getY();
		}
		if (point.getZ() > this.dimensionZ) {
			this.dimensionZ = point.getZ();
		}		
	}
	
	public void createCanvasDimensions() {
		Dimensions dimensions = new Dimensions(this.dimensionX, this.dimensionY, this.dimensionZ, 3, 1);
		this.layout.setDimensions(dimensions);			
	}

	public void addSpecies(Species species) {
		ListOf<Species> listOfSpecies = model.getListOfSpecies();		
		listOfSpecies.add(species);
	}
	
	public void addSpeciesGlyph(SpeciesGlyph speciesGlyph) {
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();	
		listOfSpeciesGlyphs.add(speciesGlyph);
	}	
	
	public void addTextGlyph(TextGlyph textGlyph){
		ListOf<TextGlyph> listOfTextGlyphs = layout.getListOfTextGlyphs();	
		listOfTextGlyphs.add(textGlyph);
	}
	
	public void addReaction(Reaction reaction){
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		listOfReactions.add(reaction);
	}
	
	public void addReactionGlyph(ReactionGlyph reactionGlyph){
		ListOf<ReactionGlyph> listOfReactionGlyphs = layout.getListOfReactionGlyphs();
		listOfReactionGlyphs.add(reactionGlyph);
	}

	/**
	 * Find a <code>Reaction</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<Reaction></code> listOfReactions
	 * @param <code>String</code> id
	 * @return the Reaction from the listOfReactions
	 */			
	public Reaction findReaction(String id) {
		for (Reaction reaction : model.getListOfReactions()) {
			if (reaction.getId().equals(id)) {
				//System.out.format("findReaction reaction=%s \n", reaction.getId());
				return reaction;
			}
		}
		return null;		
	}
	
	/**
	 * Find a <code>ReactionGlyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<ReactionGlyph></code> listOfReactionGlyph
	 * @param <code>String</code> id
	 * @return the ReactionGlyph from the listOfReactionGlyph
	 */		
	public ReactionGlyph findReactionGlyph(String id) {
		for (ReactionGlyph reactionGlyph : layout.getListOfReactionGlyphs()) {
			if (reactionGlyph.getId().equals(id)) {
				return reactionGlyph;
			}
		}
		return null;
	}	
	
	/**
	 * Find a <code>Species</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<ReactionGlyph></code> listOfSpecies
	 * @param <code>String</code> id
	 * @return the Species from the listOfSpecies
	 */			
	public Species findSpecies(String id) {
		for (Species species : model.getListOfSpecies()) {
			if (species.getId().equals(id)) {
				return species;
			}
		}
		return null;
	}
	
	/**
	 * Find a <code>SpeciesGlyph</code> that has a matching <code>id</code>.
	 * 
	 * @param <code>ListOf<SpeciesGlyph></code> listOfSpeciesGlyph
	 * @param <code>String</code> id
	 * @return the SpeciesGlyph from the listOfSpeciesGlyph
	 */			
	public SpeciesGlyph findSpeciesGlyph(String id) {
		for (SpeciesGlyph speciesGlyph : layout.getListOfSpeciesGlyphs()) {
			if (speciesGlyph.getId().equals(id)) {
				return speciesGlyph;
			}
		}
		return null;
	}	
	
	public void addSpeciesReference() {
		
	}
	
	public void addSpeciesReferenceGlyph() {
		
	}
	
	public void addCompartment(Compartment compartment) {
		ListOf<Compartment> listOfCompartment = model.getListOfCompartments();
		listOfCompartment.add(compartment);
	}
	
	public void addCompartmentGlyph(CompartmentGlyph compartmentGlyph) {
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();	
		listOfCompartmentGlyphs.add(compartmentGlyph);
	}
	
	public void addGeneralGlyph(GeneralGlyph generalGlyph) {
		ListOf<GraphicalObject> listOfGeneralGlyphs = layout.getListOfAdditionalGraphicalObjects();
		listOfGeneralGlyphs.add(generalGlyph);
		
	}
	
}

