package org.sbfc.converter.sbgnml2sbml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.GradientBase;
import org.sbml.jsbml.ext.render.LineEnding;
import org.sbml.jsbml.ext.render.ListOfLocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.Polygon;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.Style;
import org.sbml.jsbml.ext.render.Transformation2D;

/**
 * SBGNML2SBMLOutput contains all data structures needed to create the output XML document.
 * Example: LayoutModelPlugin, QualModelPlugin, RenderLayoutPlugin, etc.
 * @author haoran
 *
 */
public class SBGNML2SBMLOutput {
	public Model model;
	
	// Layout classes
	LayoutModelPlugin layoutPlugin;
	public Layout layout;
	
	// Rendering classes
	RenderLayoutPlugin renderLayoutPlugin;
	ListOfLocalRenderInformation listOfLocalRenderInformation;
	public LocalRenderInformation localRenderInformation;
	
	ListOf<ColorDefinition> listOfColorDefinitions;
	ListOf<LineEnding> listOfLineEndings;	
	ListOf<LocalStyle> listOfStyles;
	ListOf<GradientBase> listOfGradientDefinitions;
	
	ListOf<LineEnding> listOfLineEndings_temp;	
	ListOf<LocalStyle> listOfStyles_temp;
	HashMap<String, String> stylesInModel = new HashMap<String, String>();
	HashMap<String, String> lineEndingsInModel = new HashMap<String, String>();
	
	// Qual
	QualModelPlugin qualModelPlugin;
	
	// keep track of the maximum value for each dimension. In the end, we set these 3 values as the dimensions of the layout
	// TODO: might be broken
	Double dimensionX;
	Double dimensionY;
	Double dimensionZ;
	Dimensions dimensions;
		
	// keep track of the statistics
	int numOfSpecies = 0;
	int numOfSpeciesGlyphs = 0;
	int numOfReactions = 0;
	int numOfReactionGlyphs = 0;
	int numOfSpeciesReferences = 0;
	int numOfModifierSpeciesReferences = 0;
	int numOfSpeciesReferenceGlyphs = 0;
	int numOfCompartments = 0;
	int numOfCompartmentGlyphs = 0;
	int numOfTextGlyphs = 0;
	int numOfGeneralGlyphs = 0;
	int numOfAdditionalGraphicalObjects = 0;
	int numOfReferenceGlyphs = 0;

	int numOfSpeciesReferenceGlyphErrors = 0;
	
	public SBGNML2SBMLOutput(int level, int version, String language) {
		this.model = new Model(level, version);
		createLayout();
		createRenderInformation();
		
		if (language.equals("activity flow")){
			createQual();
		}
					
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

		this.listOfColorDefinitions = renderLayoutPlugin.getLocalRenderInformation(0).getListOfColorDefinitions();
		this.listOfLineEndings = renderLayoutPlugin.getLocalRenderInformation(0).getListOfLineEndings();
		this.listOfStyles = renderLayoutPlugin.getLocalRenderInformation(0).getListOfLocalStyles();
		this.listOfGradientDefinitions = renderLayoutPlugin.getLocalRenderInformation(0).getListOfGradientDefinitions();
	
		this.listOfLineEndings_temp = new ListOf<LineEnding>(3, 1);
	}
	
	private void createQual() {
		this.qualModelPlugin = (QualModelPlugin) model.getPlugin("qual");

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
		numOfSpecies++;
	}
	
	public void addQualitativeSpecies(QualitativeSpecies species) {
		ListOf<QualitativeSpecies> listOfSpecies = qualModelPlugin.getListOfQualitativeSpecies();
		listOfSpecies.add(species);
	}
	
	public void addSpeciesGlyph(SpeciesGlyph speciesGlyph) {
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();	
		listOfSpeciesGlyphs.add(speciesGlyph);
		numOfSpeciesGlyphs++;
	}	
	
	public void addTextGlyph(TextGlyph textGlyph){
		ListOf<TextGlyph> listOfTextGlyphs = layout.getListOfTextGlyphs();	
		listOfTextGlyphs.add(textGlyph);
		numOfTextGlyphs++;
	}
	
	public void addReaction(Reaction reaction){
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		listOfReactions.add(reaction);
		numOfReactions++;
	}
	
	public void addReactionGlyph(ReactionGlyph reactionGlyph){
		ListOf<ReactionGlyph> listOfReactionGlyphs = layout.getListOfReactionGlyphs();
		listOfReactionGlyphs.add(reactionGlyph);
		numOfReactionGlyphs++;
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
	
	public void addModifierSpeciesReference() {
		
	}
	
	public void addSpeciesReferenceGlyph(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph) {
		reactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
		numOfSpeciesReferenceGlyphs++;
		if (speciesReferenceGlyph.getCurve().getCurveSegmentCount() == 0){
			numOfSpeciesReferenceGlyphErrors ++;
		}
	}
	
	public void addCompartment(Compartment compartment) {
		ListOf<Compartment> listOfCompartment = model.getListOfCompartments();
		listOfCompartment.add(compartment);
		numOfCompartments++;
	}
	
	public void addCompartmentGlyph(CompartmentGlyph compartmentGlyph) {
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();	
		listOfCompartmentGlyphs.add(compartmentGlyph);
		numOfCompartmentGlyphs++;
	}
	
	public void addGeneralGlyph(GeneralGlyph generalGlyph) {
		ListOf<GraphicalObject> listOfGeneralGlyphs = layout.getListOfAdditionalGraphicalObjects();
		listOfGeneralGlyphs.add(generalGlyph);
		numOfGeneralGlyphs++;
		
	}
	
	public void addGraphicalObject(GraphicalObject generalGlyph) {
		ListOf<GraphicalObject> listOfGeneralGlyphs = layout.getListOfAdditionalGraphicalObjects();
		listOfGeneralGlyphs.add(generalGlyph);
		numOfAdditionalGraphicalObjects++;
		
	}
	
	public void addReferenceGlyph(GeneralGlyph generalGlyph, ReferenceGlyph referenceGlyph){
		generalGlyph.addReferenceGlyph(referenceGlyph);
		numOfReferenceGlyphs++;
	}
	
	public void addTransition(Transition transition) {
		qualModelPlugin.addTransition(transition);
	}
	
	/**
	 * Load the SBML file
	 * @param sbmlFileName
	 * @return
	 */
	public static SBMLDocument getSBMLDocument(String sbmlFileName) {

		SBMLDocument document = null;
		SBMLReader reader = new SBMLReader();
		try {
			document = reader.readSBML(sbmlFileName);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		return document;
	}	
	
	/**
	 * Load a template rendering styles file.
	 * TODO: remove the hard-coded file names
	 * @return
	 */
	public LocalRenderInformation loadTemplateFromFile() {
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;		
		
		try {
			inputProperties = new FileInputStream("sbgnml2sbml.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");		
		
		String sbmlFileNameInput;
		sbmlFileNameInput = examplesDirectory + "template.xml";
		sbmlDocument = getSBMLDocument(sbmlFileNameInput);
		
		Model model =  sbmlDocument.getModel();
		LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getPlugin("layout");
		Layout templateLayout = layoutPlugin.getLayout(0);
		RenderLayoutPlugin renderLayoutPlugin = (RenderLayoutPlugin) templateLayout.getPlugin(RenderConstants.shortLabel);
		LocalRenderInformation localRenderInformation = renderLayoutPlugin.getLocalRenderInformation(0);
		
		return localRenderInformation;
	}
	
	/**
	 * We loaded a rendering styles template containing all the styles of all the layout Glyphs. We load them in our lists 
	 * so that we can use them later.
	 * @param localRenderInformation
	 */
	public void storeTemplateLocalRenderInformation(LocalRenderInformation localRenderInformation) {
		
		ListOf<ColorDefinition> listOfColorDefinitions = localRenderInformation.getListOfColorDefinitions();
		for (ColorDefinition cd : listOfColorDefinitions){
			this.listOfColorDefinitions.add(cd.clone());
		}
		
		ListOf<GradientBase> listOfGradientDefinitions = localRenderInformation.getListOfGradientDefinitions();
		for (GradientBase gb : listOfGradientDefinitions){
			this.listOfGradientDefinitions.add(gb.clone());
		}
				
		ListOf<LineEnding> listOfLineEndings = localRenderInformation.getListOfLineEndings();
		for (LineEnding le : listOfLineEndings){
			LineEnding leClone = le.clone();
			RenderGroup rgClone = le.getGroup().clone();
			ListOf<Transformation2D> listOfTransformation2D = le.getGroup().getListOfElements();
			for (Transformation2D t2d : listOfTransformation2D){
				if (t2d instanceof Ellipse){
					rgClone.addElement((Ellipse) t2d.clone());
				} else if (t2d instanceof Rectangle) {
					rgClone.addElement(t2d.clone());
				} else if (t2d instanceof Polygon) {
					Polygon polygon = (Polygon) t2d.clone();
					ListOf<CurveSegment> curve_segs = polygon.getListOfCurveSegments();
					if (curve_segs.size() > 0){
						Point s = curve_segs.get(0).getStart();
						Point e = curve_segs.get(0).getEnd();
					}
					rgClone.addElement(polygon);
				} else {
					rgClone.addElement(t2d.clone());
				}
			}
			
			leClone.setGroup(rgClone);
			this.listOfLineEndings_temp.add(leClone);;
		}
		
		ListOf<LocalStyle> listOfStyles = localRenderInformation.getListOfLocalStyles();
		
		this.listOfStyles_temp = localRenderInformation.getListOfLocalStyles();
				
	}
	
	/**
	 * We only store the render styles that we used in the model. We need to removed all the styles that we don't use so that
	 * the converted file will not be swamped by all the styles.
	 */
	public void removeExtraStyles() {
		for (String id : stylesInModel.keySet()){
			// TODO: remove the extra styles in here as well
		}
		for (String id : lineEndingsInModel.keySet()){
			// TODO: remove the extra styles in here as well
		}
		
		this.listOfStyles = renderLayoutPlugin.getLocalRenderInformation(0).getListOfLocalStyles(); 
		
		for (LocalStyle ls : listOfStyles_temp){
			if (stylesInModel.containsKey(ls.getId())){
				LocalStyle ls_clone = ls.clone();
				ls_clone.setId(ls.getId());
				this.listOfStyles.add(ls_clone);
			}
		}

		for (LineEnding le : listOfLineEndings_temp){
			if (lineEndingsInModel.containsKey(le.getId())){
				listOfLineEndings.add(le.clone());
			}			
		}
	}

	/**
	 * Auto-fill missing values for required fields in the SBML Model
	 */
	public void completeModel() {

		SBMLModelCompleter modelCompleter = new SBMLModelCompleter();
		model = modelCompleter.autoCompleteRequiredAttributes(model);

	}


}

