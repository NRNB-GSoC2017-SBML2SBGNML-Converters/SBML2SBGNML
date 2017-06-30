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
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.GraphicalPrimitive2D.FillRule;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.LineEnding;
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

	public SBGNML2SBMLRender() {
		createColourDefinitions();
	}
		
	public void displayReactionGlyphInfo() {
		for (SWrapperReactionGlyph sWrapper : converter.sWrapperModel.getListOfWrapperReactionGlyphs()){
			converter.debugMode = 1;
			converter.printHelper(sWrapper.reactionId+"-inward", sWrapper.inwardArcs.size());
			converter.printHelper(sWrapper.reactionId+"-outward", sWrapper.outwardArcs.size());
			converter.printHelper(sWrapper.reactionId+"-undirected", sWrapper.undirectedArcs.size());
			converter.printHelper(sWrapper.reactionId+"-bidirected", sWrapper.bidirectedArcs.size());
			converter.debugMode = 0;
		}
	}
	
	public void createDefaultCompartment(Model modelObject) {
		String compartmentId = "Compartment_01";
		Compartment compartment = new Compartment(compartmentId);
		modelObject.getListOfCompartments().add(compartment);
		
		for (Species species: modelObject.getListOfSpecies()){
			species.setCompartment(compartment);	
		}
	}
		
	public void renderReferenceGlyphs(LocalRenderInformation localRenderInformation, SWrapperReactionGlyph sWrapperReactionGlyph) {
		for (String arcKey : sWrapperReactionGlyph.referenceGlyphs.keySet()){
			System.out.println("[===renderReferenceGlyphs] "+sWrapperReactionGlyph.reactionId+"===>"+arcKey);
			ReferenceGlyph referenceGlyph = sWrapperReactionGlyph.referenceGlyphs.get(arcKey);
			
			//temp
			Arc arc = sWrapperReactionGlyph.inwardArcs.get(arcKey);
			if (arc == null){arc = sWrapperReactionGlyph.outwardArcs.get(arcKey);}
			
			createStyle(localRenderInformation, referenceGlyph, arc);
		}		
	}
	
	public void renderGeneralGlyphs(SWrapperModel sWrapperModel) {
		//todo: generalGlyphs as reactions (for a process node) or species (for a logic operator)?
		// as reactions might not work
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender(sWrapperModel.getModel());
		
		//LocalRenderInformation localRenderInfo = renderer.listOfLocalRenderInformation.get(0);
		renderer.localRenderInformation = new LocalRenderInformation("LocalRenderInformation_01");
		renderer.renderLayoutPlugin.addLocalRenderInformation(renderer.localRenderInformation);
		renderer.listOfLineEndings = this.listOfLineEndings;
		
		for (LineEnding lineEnding: renderer.listOfLineEndings) {System.out.println("[renderGeneralGlyphs] lineEnding "+lineEnding.getId());}
		
		for (SWrapperReactionGlyph sWrapperReactionGlyph : sWrapperModel.getListOfWrapperReactionGlyphs()){
			if (!sWrapperReactionGlyph.isGeneralGlyph){
				continue;
			}
			createStyle(renderer.localRenderInformation, sWrapperReactionGlyph.generalGlyph, sWrapperReactionGlyph.clazz);
			renderReferenceGlyphs(renderer.localRenderInformation, sWrapperReactionGlyph);
		}
		
		for (SWrapperReactionGlyph sWrapperReactionGlyph : sWrapperModel.getListOfWrapperReactionGlyphs()){
			if (sWrapperReactionGlyph.isGeneralGlyph){
				continue;
			}
			createStyle(renderer.localRenderInformation, sWrapperReactionGlyph.reactionGlyph, sWrapperReactionGlyph.clazz);
			renderReferenceGlyphs(renderer.localRenderInformation, sWrapperReactionGlyph);
		}
		
		for (SWrapperSpeciesGlyph sWrapperSpeciesGlyph : sWrapperModel.getListOfWrapperSpeciesGlyphs()){
			createStyle(renderer.localRenderInformation, sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.clazz);
		}
	}

	public void createStyle(LocalRenderInformation localRenderInfo, GraphicalObject generalGlyph, String clazz) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		
		
		String styleId = "LocalStyle_" + generalGlyph.getId();
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInfo.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) generalGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
				
		Image image = createImage(generalGlyph, clazz);
		
		renderGroup.addElement(image);		
	}
	
	public void createStyle(LocalRenderInformation localRenderInfo, ReferenceGlyph generalGlyph, Arc arc) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		
		
		String styleId = "LocalStyle_" + generalGlyph.getId();
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInfo.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) generalGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		
		System.out.println("createStyle "+ this.listOfLineEndings.size());
		
		if (arc.getClazz().equals("catalysis")){
			//String endHead = this.listOfLineEndings.get("catalysisHead").getId();
			renderGroup.setEndHead("catalysisHead");			
		}
		if (arc.getClazz().equals("production")){
			LineEnding lineEnding = this.listOfLineEndings.get("productionHead");
			renderGroup.setEndHead("productionHead");			
		}
		
	}	
	
	public Image createImage(GraphicalObject generalGlyph, String clazz){
		Image image = new Image("Image_" + generalGlyph.getId());
		image.setX(0);
		image.setX(0);
		image.setWidth(100);
		image.setHeight(100);
		image.setAbsoluteX(false);
		image.setAbsoluteY(false);		
		image.setAbsoluteWidth(false);
		image.setAbsoluteHeight(false);	
		
		
		// todo: horizontal or vertical?
		if (clazz.equals("or")){
			image.setHref("or-glyph.png");	
		}
		
		if (clazz.equals("process")){
			image.setHref("process-glyph.png");	
		} 
		if (clazz.equals("macromolecule")){
			image.setHref("macromolecule-glyph.png");	
		}
		
		if (clazz.equals("simple chemical")){
			image.setHref("simple-chemical-glyph.png");	
		} 
		
		return image;
	}
	
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

	public Model example_04(Sbgn sbgnObject, ListOf<LineEnding> listOfLineEndings) {

		Map map = sbgnObject.getMap();		
		
		converter = new SBGNML2SBML_GSOC2017(map);
		converter.convertToSBML();
		
		converter.debugMode = 1;
//		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperCompartmentGlyphs().size());
//		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperReactionGlyphs().size());
//		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperSpeciesGlyphs().size());
		converter.printHelper("example_04 listOfLineEndings",listOfLineEndings.size());
		converter.debugMode = 0;
		
		displayReactionGlyphInfo();
		
		this.listOfLineEndings = listOfLineEndings;
		createDefaultCompartment(converter.sWrapperModel.getModel());
		renderGeneralGlyphs(converter.sWrapperModel);
		
		return converter.model;
	}
		
	public static void runExample_04(Model newModel){
		String sbmlFileNameOutput;
		String sbmlFileNameInput;
		Sbgn sbgn;
		
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
		
		// temp
		RenderLayoutPlugin renderLayoutPlugin = (RenderLayoutPlugin) ((LayoutModelPlugin) newModel.getPlugin("layout")).getLayout(0).getPlugin(RenderConstants.shortLabel);
		LocalRenderInformation localRenderInformation = renderLayoutPlugin.getListOfLocalRenderInformation().get(0);
		ListOf<LineEnding> listOfLineEndings = localRenderInformation.getListOfLineEndings();
		
		try {
			inputProperties = new FileInputStream("config_unittest.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");

		sbmlFileNameOutput = examplesDirectory + "Render_example_04.xml";
		sbmlFileNameInput = examplesDirectory + "or-simple.sbgn";

		sbgn = SBGNML2SBML_GSOC2017.readSbgnFile(sbmlFileNameInput);
		
		renderer = new SBGNML2SBMLRender();
		renderer.model = renderer.example_04(sbgn, listOfLineEndings);	
		
		SBGNML2SBML_GSOC2017.writeSbgnFile(sbmlFileNameOutput, renderer.model);

	}
	
	public static void main(String[] args) {

		// with species reference
		// with image
		// use general glyph
		// from sbgn
		// show 2 versions of render
		
		// wrappers
		// edit points
		// bounding box for reaction glyphs
		
		Model model = runExample_02();
		runExample_04(model);
	}
}
