package org.sbfc.converter.sbml2sbgnml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLRender;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.Text;
import org.sbml.jsbml.ext.render.VTextAnchor;

public class TestConverter {
	String sbmlFileNameInput = null;
	String sbgnmlFileNameOutput = null;
	File file = null;	
	List<String> testFiles = new ArrayList<String>();
	int numOfFilesConverted = 0;
	
	Properties properties = new Properties();	
	InputStream inputProperties;

	SBMLDocument sbmlDocument = null;
//	Sbgn sbgnObject = null;
//	Map map = null;
	SBML2SBGNML_GSOC2017 converter = null;
	//Model sbmlModel = null;
	//LayoutModelPlugin sbmlLayoutModel = null;
	//ListOf<Layout> listOfLayouts = null;
	
//	int numAdditionalGraphicalObject = 0;
//	int numCompartmentGlyphs = 0;
//	int numReactionGlyphs = 0;
//	int numSpeciesGlyphs = 0;
//	int numTextGlyphs = 0;	
//	ListOf<GraphicalObject> listOfAdditionalGraphicalObjects;
//	ListOf<CompartmentGlyph> listOfCompartmentGlyphs;
//	ListOf<ReactionGlyph> listOfReactionGlyphs;
//	ListOf<SpeciesGlyph> listOfSpeciesGlyphs;
//	ListOf<TextGlyph> listOfTextGlyphs;			
	
	@Before
	public void setUp(){
		try {
			inputProperties = new FileInputStream("sbml2sbgnml.properties");
			properties.load(inputProperties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		sbmlFileNameInput = properties.getProperty("sbml2sbgnml.unittest_file.path");
		
		try { 
			sbmlDocument = SBML2SBGNMLUtil.getSBMLDocument(sbmlFileNameInput);
			converter = new SBML2SBGNML_GSOC2017(sbmlDocument);

//			sbmlModel = sbmlDocument.getModel();
//			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
//			listOfLayouts = sbmlLayoutModel.getListOfLayouts();			
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		// these files are used for testConvertToSBGNML()
		String examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.layout");
		testFiles.add(examplesDirectory + "CompartmentGlyph_example.xml");
		testFiles.add(examplesDirectory + "Complete_Example.xml");
		testFiles.add(examplesDirectory + "GeneralGlyph_Example.xml");
		testFiles.add(examplesDirectory + "ReactionGlyph_Example.xml");
		testFiles.add(examplesDirectory + "SpeciesGlyph_Example.xml");
		testFiles.add(examplesDirectory + "TextGlyph_Example.xml");	
		
//		for (Layout layout : converter.sOutput.listOfLayouts){
//			sbgnObject = new Sbgn();
//			map = new Map();
//			sbgnObject.setMap(map);		
//
//			if (layout.isSetListOfCompartmentGlyphs()){
//				numCompartmentGlyphs = layout.getNumCompartmentGlyphs();
//				listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();
//			}			
//			if (layout.isSetListOfSpeciesGlyphs()){
//				numSpeciesGlyphs = layout.getNumSpeciesGlyphs();
//				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
//			}
//			if (layout.isSetListOfAdditionalGraphicalObjects()){
//				numAdditionalGraphicalObject = layout.getAdditionalGraphicalObjectCount();
//				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
//			}			
//			if (layout.isSetListOfTextGlyphs()){
//				numTextGlyphs = layout.getNumTextGlyphs();
//				listOfTextGlyphs = layout.getListOfTextGlyphs();
//			}			
//			if (layout.isSetListOfReactionGlyphs()){
//				numReactionGlyphs = layout.getNumReactionGlyphs();
//				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
//			}			
//		}
	}
	
	@Test
	public void testConvertToSBGNML() {	

		
		for (String sbmlFileName: testFiles){
			//System.out.println(sbmlFileName);
			String sbmlFileNameInput = sbmlFileName;
			String sbgnmlFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
			File file = new File(sbmlFileNameInput);
			assertTrue(file.exists());
				
			System.out.println("convertExampleFile "+ sbmlFileNameInput);
			SBMLDocument sbmlDocument = SBML2SBGNMLUtil.getSBMLDocument(sbmlFileNameInput);
			converter = new SBML2SBGNML_GSOC2017(sbmlDocument);
			Sbgn sbgnObject = converter.convertToSBGNML(sbmlDocument);	
			file = new File(sbgnmlFileNameOutput);
			try {
				SbgnUtil.writeToFile(sbgnObject, file);
			} catch (JAXBException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			numOfFilesConverted += 1;					

		}
	
		assertEquals("Not all models were successfully converted", numOfFilesConverted, testFiles.size());
		
	}

	
	@Test
	public void testCreateBBox(){
		GraphicalObject sbmlGlyph = converter.sOutput.listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		converter.sUtil.createBBox(sbmlGlyph, sbgnGlyph);
		
		BoundingBox boundingBox = sbmlGlyph.getBoundingBox();
		Dimensions dimensions = boundingBox.getDimensions();
		Point position = boundingBox.getPosition();
		
		Bbox bbox = sbgnGlyph.getBbox();
		
		assertEquals((float) position.getX(), bbox.getX(), 0.01);
		assertEquals((float) position.getY(), bbox.getY(), 0.01);
		assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
		assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);
		
	}
	
	@Test
	public void testSetBBoxDimensions(){
		GraphicalObject sbmlGlyph = converter.sOutput.listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		converter.sUtil.createBBox(sbmlGlyph, sbgnGlyph);
		converter.sUtil.setBBoxDimensions(sbgnGlyph, 0, 1, 0, 1);
				
		Bbox bbox = sbgnGlyph.getBbox();
		
		assertEquals(0, bbox.getX(), 0.01);
		assertEquals(0, bbox.getY(), 0.01);
		assertEquals(1, bbox.getW(), 0.01);
		assertEquals(1, bbox.getH(), 0.01);
		
	}	
		
	@Test
	public void testCreateGlyphsFomCompartmentGlyphs(){
		converter.createFromCompartmentGlyphs(converter.sOutput.sbgnObject, converter.sOutput.listOfCompartmentGlyphs);
		int numOfGlyphs = converter.sOutput.sbgnObject.getMap().getGlyph().size();
		
		assertEquals(converter.sOutput.listOfCompartmentGlyphs.size(), numOfGlyphs);
		
		for (Glyph sbgnGlyph : converter.sOutput.sbgnObject.getMap().getGlyph()){
			CompartmentGlyph sbmlGlyph = converter.sOutput.listOfCompartmentGlyphs.get(sbgnGlyph.getId());

			BoundingBox boundingBox = sbmlGlyph.getBoundingBox();
			Dimensions dimensions = boundingBox.getDimensions();
			Point position = boundingBox.getPosition();
			
			Bbox bbox = sbgnGlyph.getBbox();			
			
			assertEquals((float) position.getX(), bbox.getX(), 0.01);
			assertEquals((float) position.getY(), bbox.getY(), 0.01);
			assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
			assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);			
		}
	}
		
	@Test
	public void testCreateGlyphsFromReactionGlyphs(){
		converter.createFromReactionGlyphs(converter.sOutput.sbgnObject, converter.sOutput.listOfReactionGlyphs);
		int numOfGlyphs = converter.sOutput.sbgnObject.getMap().getArcgroup().size();
		
		assertEquals(converter.sOutput.listOfReactionGlyphs.size(), numOfGlyphs);
	}	
	
	@Test
	public void testCreateGlyphFromReactionGlyph(){
		ReactionGlyph sbmlGlyph = converter.sOutput.listOfReactionGlyphs.get(0);
		converter.createFromOneReactionGlyph(converter.sOutput.sbgnObject, sbmlGlyph);
		
		converter.sOutput.sbgnObject.getMap().getGlyph();
		converter.sOutput.sbgnObject.getMap().getArc();
		
		// process node created
		if (sbmlGlyph.isSetCurve()) {
			int numOfGlyphs = converter.sOutput.sbgnObject.getMap().getArcgroup().size();
			assertEquals(1, numOfGlyphs);
		}
		
		// same number of Arcs as number of SpeciesReference
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs = sbmlGlyph.getListOfSpeciesReferenceGlyphs();
		int numOfArcs = converter.sOutput.sbgnObject.getMap().getArc().size();
		assertEquals(numOfArcs, listOfSpeciesReferenceGlyphs.size());
		
		for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
			String clazz = converter.sUtil.searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
			// todo: there's a problem here, can't figure out how to find the arc corresponding to a speciesReferenceGlyph
		}
		
		//todo: test arc coordinates
		
	}	
	
	@Test
	public void testCreateOneArc(){
		fail("not implemented");
	}
	
	@Test
	public void testCreateOneProcessNode(){
		fail("not implemented");
	}	
	
	@Test
	public void testCreateGlyphsFromSpeciesGlyphs(){
		converter.createFromSpeciesGlyphs(converter.sOutput.sbgnObject, converter.sOutput.listOfSpeciesGlyphs);
		int numOfGlyphs = converter.sOutput.sbgnObject.getMap().getGlyph().size();
		
		assertEquals(converter.sOutput.listOfSpeciesGlyphs.size(), numOfGlyphs);
		
		for (Glyph sbgnGlyph : converter.sOutput.sbgnObject.getMap().getGlyph()){
			SpeciesGlyph sbmlGlyph = converter.sOutput.listOfSpeciesGlyphs.get(sbgnGlyph.getId());

			BoundingBox boundingBox = sbmlGlyph.getBoundingBox();
			Dimensions dimensions = boundingBox.getDimensions();
			Point position = boundingBox.getPosition();
			
			Bbox bbox = sbgnGlyph.getBbox();			
			
			assertEquals((float) position.getX(), bbox.getX(), 0.01);
			assertEquals((float) position.getY(), bbox.getY(), 0.01);
			assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
			assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);			
		}		
	}
	
	@Test
	public void testCreateLabelsFromTextGlyphs(){
		converter.createFromSpeciesGlyphs(converter.sOutput.sbgnObject, converter.sOutput.listOfSpeciesGlyphs);
		converter.createLabelsFromTextGlyphs(converter.sOutput.sbgnObject, converter.sOutput.listOfTextGlyphs);
		
		for (TextGlyph sbmlGlyph : converter.sOutput.listOfTextGlyphs){
			if (sbmlGlyph.isSetGraphicalObject()) {
				String speciesId = sbmlGlyph.getGraphicalObjectInstance().getId();
				
				List<Glyph> listOfGlyphs = converter.sOutput.sbgnObject.getMap().getGlyph();
				int indexOfSpeciesGlyph = converter.sUtil.searchForIndex(listOfGlyphs, speciesId);
				Glyph sbgnGlyph = listOfGlyphs.get(indexOfSpeciesGlyph);
				
				if (sbmlGlyph.isSetText()) {
					//System.out.println(sbgnGlyph.getLabel().getText());
					assertEquals(sbgnGlyph.getLabel().getText(), sbmlGlyph.getText());
					// todo: fix order of args
				}				
				
			}
		}
	}
	
	@Test
	public void testCreateGlyphsFromGeneralGlyphs(){
		fail("not implemented");
	}
		
	@Test
	public void testCreateGlyphFromGeneralGlyph(){
		fail("not implemented");
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
		image.setHref("multimer-1.png");
		
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
	

	public void runExample_01(){
		String sbmlFileNameOutput;
		File outputFile;

		
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
		SBMLWriter sbmlWriter = new SBMLWriter();
	
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
		
		renderer = new SBGNML2SBMLRender();
		//renderer.example_01();	
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(renderer.model);
		
		renderer.layout.setDimensions(renderer.dimensions);
		sbmlWriter = new SBMLWriter();
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	public static Model runExample_02(){
		String sbmlFileNameOutput;
		String sbmlFileNameInput;
		File outputFile;

		List<String> testFiles = new ArrayList<String>();
		
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
		SBMLWriter sbmlWriter = new SBMLWriter();
	
		try {
			inputProperties = new FileInputStream("config_unittest.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
		
		sbmlFileNameOutput = examplesDirectory + "Render_example_02.xml";
		outputFile = new File(sbmlFileNameOutput);		
		
		sbmlFileNameInput = examplesDirectory + "Render_example_localRenderOnly.xml";
		sbmlDocument = renderer.getSBMLDocument(sbmlFileNameInput);
		
		renderer = new SBGNML2SBMLRender(sbmlDocument.getModel());
		Model newModel = null;
		newModel = renderer.example_02();	
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(newModel);

		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		return newModel;
	}
	public void runExample_03(){
		String sbmlFileNameOutput;
		File outputFile;

		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
		SBMLWriter sbmlWriter = new SBMLWriter();
	
		try {
			inputProperties = new FileInputStream("config_unittest.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
		
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
	}	
	
	
}
