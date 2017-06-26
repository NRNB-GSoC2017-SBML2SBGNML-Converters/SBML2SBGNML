package org.sbfc.converter.sbgnml2sbml;

import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.GraphicalPrimitive2D.FillRule;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.ListOfLocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.Text;
import org.sbml.jsbml.ext.render.VTextAnchor;

public class SBGNML2SBMLRender {
	
	Model model;
	Layout layout;
	LayoutModelPlugin layoutPlugin;
	RenderLayoutPlugin renderLayoutPlugin;
	ListOfLocalRenderInformation listOfLocalRenderInformation;
	LocalRenderInformation localRenderInformation;
	Dimensions dimensions;
	ListOf<ColorDefinition> listOfColorDefinitions;
	
	public SBGNML2SBMLRender() {
		model = new Model(3, 1);
		
		layoutPlugin = (LayoutModelPlugin) model.getPlugin("layout");
		layout = layoutPlugin.createLayout();
		
		renderLayoutPlugin = (RenderLayoutPlugin) layout.getPlugin(RenderConstants.shortLabel);
		localRenderInformation = new LocalRenderInformation("LocalRenderInformation_01");
		renderLayoutPlugin.addLocalRenderInformation(localRenderInformation);		
		listOfLocalRenderInformation = renderLayoutPlugin.getListOfLocalRenderInformation();
		
		createColourDefinitions();
	}
	
	public SBGNML2SBMLRender(Model model) {
		this.model = model;
		
		layoutPlugin = (LayoutModelPlugin) model.getPlugin("layout");
		layout = layoutPlugin.getLayout(0);
		
		renderLayoutPlugin = (RenderLayoutPlugin) layout.getPlugin(RenderConstants.shortLabel);
			
		listOfLocalRenderInformation = renderLayoutPlugin.getListOfLocalRenderInformation();
		localRenderInformation = listOfLocalRenderInformation.get(0);	
	}
	
	public void example_01() {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		
		
		String styleId = "LocalStyle_01";
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		//localStyle.getTypeList();
		
		String speciesId = "Species_01";
		String speciesName = "Protein";
		Species species = new Species(speciesId, speciesName, 3, 1);
		model.getListOfSpecies().add(species);
		
		String compartmentId = "Compartment_01";
		Compartment compartment = new Compartment(compartmentId);
		model.getListOfCompartments().add(compartment);
		species.setCompartment(compartment);
		
		SpeciesGlyph speciesGlyph = layout.createSpeciesGlyph("SpeciesGlyph_01");
		speciesGlyph.setSpecies(species);
		BoundingBox boundingBox = new BoundingBox();
		speciesGlyph.setBoundingBox(boundingBox);
		Point point = new Point(330, 230, 0, 3, 1);
		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
		boundingBox.setPosition(point);
		boundingBox.setDimensions(dimension);
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		//localStyle.getIDList();
				
		Rectangle rectangle = createRectangle(0, 0, 90, 100, 10, 10, true, true, true, true, false, false);
		Ellipse ellipse = createEllipse(90, 50.0, 10.0, false, false, true);
		Text text1 = createText(-10, -9.6, true, true);
		text1.setFontFamily(FontFamily.MONOSPACE);
		text1.setTextAnchor(HTextAnchor.MIDDLE);
		text1.setVTextAnchor(VTextAnchor.MIDDLE);
		text1.setName("Protein");
		Text text2 = createText(-5, -9.6, false, true);
		text2.setFontFamily(FontFamily.MONOSPACE);
		text2.setTextAnchor(HTextAnchor.END);
		text2.setVTextAnchor(VTextAnchor.MIDDLE);	
		text2.setName("P");
				
		renderGroup.addElement(rectangle);
		renderGroup.addElement(ellipse);
		renderGroup.addElement(text1);
		renderGroup.addElement(text2);
		//localRenderInformation.getListOfLineEndings();
		
		Dimensions dimensions = new Dimensions(450, 400, 0, 3, 1);
		this.dimensions = dimensions;
		
	}
	
	public Model example_02() {
		Model newModel = new Model(3, 1);
		
		newModel.setListOfCompartments(model.getListOfCompartments());
		newModel.setListOfSpecies(model.getListOfSpecies());
		newModel.getListOfSpecies().remove("ATP");
		newModel.getListOfSpecies().remove("ADP");
		newModel.setListOfReactions(model.getListOfReactions());
		newModel.getListOfReactions().remove("Dephosphorylation");
		newModel.getListOfReactions().get("Phosphorylation").getListOfReactants().remove("SpeciesReference_ATP");
		newModel.getListOfReactions().get("Phosphorylation").getListOfProducts().remove("SpeciesReference_ADP");
		
		LayoutModelPlugin plugin = (LayoutModelPlugin) newModel.getPlugin("layout");
		Layout newLayout = plugin.createLayout();
		
		newLayout.setDimensions(layout.getDimensions());
		newLayout.setListOfSpeciesGlyphs(layout.getListOfSpeciesGlyphs());
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ATP");
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ADP");
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_P");

		newLayout.setListOfReactionGlyphs(layout.getListOfReactionGlyphs());
		newLayout.getListOfReactionGlyphs().remove("ReactionGlyph_Dephosphorylation");
		ReactionGlyph reactionGlyph = newLayout.getListOfReactionGlyphs().get(0);
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ATP");
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ADP");
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_P");

		newLayout.setListOfTextGlyphs(layout.getListOfTextGlyphs());
		newLayout.getListOfTextGlyphs().remove("TextGlyph_ATP");
		newLayout.getListOfTextGlyphs().remove("TextGlyph_ADP");
		newLayout.getListOfTextGlyphs().remove("TextGlyph_P");
				
		RenderLayoutPlugin newRenderLayoutPlugin = (RenderLayoutPlugin) newLayout.getPlugin(RenderConstants.shortLabel);
		//LocalRenderInformation newLocalRenderInformation = new LocalRenderInformation("LocalRenderInformation_01");
		//newRenderLayoutPlugin.addLocalRenderInformation(newLocalRenderInformation);			
		
		newRenderLayoutPlugin.setListOfLocalRenderInformation(renderLayoutPlugin.getListOfLocalRenderInformation());
		LocalRenderInformation newLocalRenderInformation = newRenderLayoutPlugin.getListOfLocalRenderInformation().get(0);
		if (newModel.isSetPlugin("render")) {System.out.println("[" + "NO" + "] " + newLocalRenderInformation.toString());}
		
		//...... not done yet
		
		return newModel;
	}	
	
	
	public void example_03() {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		
		
		String styleId = "LocalStyle_01";
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		//localStyle.getTypeList();
		
		String speciesId = "Species_01";
		String speciesName = "Protein";
		Species species = new Species(speciesId, speciesName, 3, 1);
		model.getListOfSpecies().add(species);
		
		String compartmentId = "Compartment_01";
		Compartment compartment = new Compartment(compartmentId);
		model.getListOfCompartments().add(compartment);
		species.setCompartment(compartment);
		
		SpeciesGlyph speciesGlyph = layout.createSpeciesGlyph("SpeciesGlyph_01");
		speciesGlyph.setSpecies(species);
		BoundingBox boundingBox = new BoundingBox();
		speciesGlyph.setBoundingBox(boundingBox);
		Point point = new Point(330, 230, 0, 3, 1);
		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
		boundingBox.setPosition(point);
		boundingBox.setDimensions(dimension);
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		//localStyle.getIDList();
				
		//Rectangle rectangle = createRectangle(0, 0, 90, 100, 10, 10, true, true, true, true, false, false);
		Image image = new Image("Image_01");
		image.setX(0);
		image.setX(0);
		image.setWidth(90);
		image.setHeight(100);
		image.setAbsoluteX(false);
		image.setAbsoluteY(false);		
		image.setAbsoluteWidth(false);
		image.setAbsoluteHeight(false);		
		image.setHref("https://2.bp.blogspot.com/-W6ljvUx0q0g/WUzwohODWqI/AAAAAAAAGZU/OOX4cHCT1ks0yWctZShFgrsAgYdXmpKxgCLcBGAs/s1600/multimer-1.jpg");
		
		Ellipse ellipse = createEllipse(90, 50.0, 10.0, false, false, true);
		Text text1 = createText(-10, -9.6, true, true);
		text1.setFontFamily(FontFamily.MONOSPACE);
		text1.setTextAnchor(HTextAnchor.MIDDLE);
		text1.setVTextAnchor(VTextAnchor.MIDDLE);
		text1.setName("Protein");
		Text text2 = createText(-5, -9.6, false, true);
		text2.setFontFamily(FontFamily.MONOSPACE);
		text2.setTextAnchor(HTextAnchor.END);
		text2.setVTextAnchor(VTextAnchor.MIDDLE);	
		text2.setName("P");
				
		renderGroup.addElement(image);
		renderGroup.addElement(ellipse);
		renderGroup.addElement(text1);
		renderGroup.addElement(text2);
		//localRenderInformation.getListOfLineEndings();
		
		Dimensions dimensions = new Dimensions(450, 400, 0, 3, 1);
		this.dimensions = dimensions;
		
	}	

//	public void example_04(File inputFile) {
//		Sbgn sbgnObject;
//		Map map = null;
//		try {
//			sbgnObject = SbgnUtil.readFromFile(inputFile);
//			map = sbgnObject.getMap();
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}		
//		
//		List<Glyph> listOfGlyphs = map.getGlyph();
//		List<Arc> lisOfArcs = map.getArc();
//		
//		for (Glyph glyph: listOfGlyphs) {
//			
//		}
//	}
	
	public void createColourDefinitions() {
		ColorDefinition colorDefinition;
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("white");
		colorDefinition.setValue(Color.decode("#FFFFFF"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
		
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("black");
		colorDefinition.setValue(Color.decode("#000000"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
		listOfColorDefinitions = localRenderInformation.getListOfColorDefinitions();
	}
	
	public void initializeDefaultRenderGroup(RenderGroup renderGroup) {
		renderGroup.setStroke("black");
		renderGroup.setStrokeWidth(3);
		renderGroup.setFillRule(FillRule.NONZERO);
		renderGroup.setFontSize((short) 12);
		renderGroup.setFontFamily(FontFamily.SANS_SERIF);
		renderGroup.setFontStyleItalic(false);
		renderGroup.setFontWeightBold(false);
		renderGroup.setTextAnchor(HTextAnchor.MIDDLE);
		renderGroup.setVTextAnchor(VTextAnchor.MIDDLE);
	}
	
	public Rectangle createRectangle(double x, double y, 
			double width, double height, 
			double rx, double ry, 
			boolean absoluteX, boolean absoluteY, 
			boolean absoluteRx, boolean absoluteRy,
			boolean absoluteW, boolean absoluteH) {
		Rectangle rectangle = new Rectangle();
		
		rectangle.setHeight(height);
		rectangle.setWidth(width);
		rectangle.setX(x);
		rectangle.setY(y);
		rectangle.setRx(rx);
		rectangle.setRy(ry);
		rectangle.setAbsoluteX(absoluteX);
		rectangle.setAbsoluteY(absoluteY);		
		rectangle.setAbsoluteRx(absoluteRx);
		rectangle.setAbsoluteRy(absoluteRy);		
		rectangle.setAbsoluteWidth(absoluteW);
		rectangle.setAbsoluteHeight(absoluteH);
		
		return rectangle;
	}
	
	public Ellipse createEllipse(double cx, double cy, double rx,
			boolean absoluteCx, boolean absoluteCy, boolean absoluteRx) {
		Ellipse ellipse = new Ellipse(3, 1);
		
		ellipse.setRx(rx);
		ellipse.setCx(cx);
		ellipse.setCy(cy);
		ellipse.setAbsoluteCx(absoluteCx);
		ellipse.setAbsoluteCy(absoluteCy);	
		ellipse.setAbsoluteRx(absoluteRx);	
		
		ellipse.setFill("white");
		
		return ellipse;
	}
	
	public Text createText(double x, double y, 
			boolean absoluteX, boolean absoluteY) {
		Text text = new Text();
		
		text.setAbsoluteX(absoluteX);
		text.setAbsoluteY(absoluteY);
		text.setX(x);
		text.setY(y);	
			
		return text;
	}

	public SBMLDocument getSBMLDocument(String sbmlFileName) {

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
	
	public static void main(String[] args) {
		String sbmlFileNameOutput;
		File inputFile;
		File outputFile;
		List<String> testFiles = new ArrayList<String>();
		
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
	
		try {
			inputProperties = new FileInputStream("config_unittest.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
		//testFiles.add(examplesDirectory + "Render_example_01.xml");
		sbmlFileNameOutput = examplesDirectory + "Render_example_01.xml";
		outputFile = new File(sbmlFileNameOutput);	
		
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
		//renderer.example_01();	
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(renderer.model);
		
		renderer.layout.setDimensions(renderer.dimensions);
		SBMLWriter sbmlWriter = new SBMLWriter();
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		sbmlFileNameOutput = examplesDirectory + "Render_example_02.xml";
		outputFile = new File(sbmlFileNameOutput);		
		
		String sbmlFileNameInput = examplesDirectory + "Render_example - Copy.xml";
		sbmlDocument = renderer.getSBMLDocument(sbmlFileNameInput);
		
		renderer = new SBGNML2SBMLRender(sbmlDocument.getModel());
		Model newModel = null;
		//newModel = renderer.example_02();	
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(newModel);

		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		

		sbmlFileNameOutput = examplesDirectory + "Render_example_03.xml";
		outputFile = new File(sbmlFileNameOutput);	
		
		renderer = new SBGNML2SBMLRender();
		//renderer.example_03();	
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(renderer.model);
		
		renderer.layout.setDimensions(renderer.dimensions);
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// with species reference
		// with image
		// use general glyph
		// from sbgn
		// show 2 versions of render
		
		// wrappers
		// edit points
		// bounding box for reaction glyphs
		
//		sbmlFileNameOutput = examplesDirectory + "Render_example_04.xml";
//		outputFile = new File(sbmlFileNameOutput);		
//		
//		sbmlFileNameInput = examplesDirectory + "or-simple.sbgn";
//		sbmlDocument = renderer.getSBMLDocument(sbmlFileNameInput);
//		
//		inputFile = new File(sbmlFileNameInput);
//		renderer = new SBGNML2SBMLRender(sbmlDocument.getModel());
//		renderer.example_04(inputFile);	
//		
//		sbmlDocument = new SBMLDocument(3, 1);
//		sbmlDocument.setModel(renderer.model);
//
//		try {
//			sbmlWriter.writeSBML(sbmlDocument, outputFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}			
	}

}
