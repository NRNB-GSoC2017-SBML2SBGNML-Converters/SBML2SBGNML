//package org.sbfc.converter.sbgnml2sbml;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//import javax.xml.bind.JAXBException;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.sbgn.SbgnUtil;
//import org.sbgn.bindings.Bbox;
//import org.sbgn.bindings.Glyph;
//import org.sbgn.bindings.Map;
//import org.sbgn.bindings.Sbgn;
//import org.sbml.jsbml.Compartment;
//import org.sbml.jsbml.ListOf;
//import org.sbml.jsbml.Model;
//import org.sbml.jsbml.SBMLDocument;
//import org.sbml.jsbml.SBMLWriter;
//import org.sbml.jsbml.Species;
//import org.sbml.jsbml.ext.layout.BoundingBox;
//import org.sbml.jsbml.ext.layout.CompartmentGlyph;
//import org.sbml.jsbml.ext.layout.Dimensions;
//import org.sbml.jsbml.ext.layout.Layout;
//import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
//import org.sbml.jsbml.ext.layout.Point;
//import org.sbml.jsbml.ext.layout.ReactionGlyph;
//import org.sbml.jsbml.ext.layout.SpeciesGlyph;
//import org.sbml.jsbml.ext.layout.TextGlyph;
//import org.sbml.jsbml.ext.render.Ellipse;
//import org.sbml.jsbml.ext.render.FontFamily;
//import org.sbml.jsbml.ext.render.HTextAnchor;
//import org.sbml.jsbml.ext.render.Image;
//import org.sbml.jsbml.ext.render.LineEnding;
//import org.sbml.jsbml.ext.render.LocalRenderInformation;
//import org.sbml.jsbml.ext.render.LocalStyle;
//import org.sbml.jsbml.ext.render.Rectangle;
//import org.sbml.jsbml.ext.render.RenderConstants;
//import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
//import org.sbml.jsbml.ext.render.RenderGroup;
//import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
//import org.sbml.jsbml.ext.render.Text;
//import org.sbml.jsbml.ext.render.VTextAnchor;
//
//public class TestConverter {
//	String sbgnFileNameInput;
//	String sbmlFileNameOutput;
//	File inputFile;
//	File outputFile;
//	List<String> testFiles = new ArrayList<String>();
//	int numOfFilesConverted = 0;
//	
//	Properties properties = new Properties();	
//	InputStream inputProperties;	
//	
//	Sbgn sbgnObject = null;
//	Map map;
//	SBGNML2SBML_GSOC2017 converter;
//	SBMLDocument sbmlDocument;
//		
//	@Before
//	public void setUp(){
//		try {
//			inputProperties = new FileInputStream("config_unittest.properties");
//			properties.load(inputProperties);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		sbgnFileNameInput = properties.getProperty("sbgnml2sbml.unittest_file.path");
//		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
//		
//		// these files are used for testConvertToSBML() only
//		testFiles.add(examplesDirectory + "adh.sbgn");
//		testFiles.add(examplesDirectory + "compartments.sbgn");
//		testFiles.add(examplesDirectory + "glycolysis.sbgn");
//		testFiles.add(examplesDirectory + "multimer.sbgn");
//		testFiles.add(examplesDirectory + "compartmentOrder1.sbgn");
//		testFiles.add(examplesDirectory + "compartmentOrder2.sbgn");
//		testFiles.add(examplesDirectory + "multimer2.sbgn");
//		testFiles.add(examplesDirectory + "protein_degradation.sbgn");
//		testFiles.add(examplesDirectory + "reversible-verticalpn.sbgn");	
//		//testFiles.add(examplesDirectory + "or-simple.sbgn");
//		
//		inputFile = new File(sbgnFileNameInput);
//		
//		try {
//			sbgnObject = SbgnUtil.readFromFile(inputFile);
//			map = sbgnObject.getMap();
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			fail("Input file is not a regular SBGN-ML file.");
//		}
//
//		converter = new SBGNML2SBML_GSOC2017(map);		
//	}
//	
//	@Test
//	public void testConvertToSBML() {
//		
//		for (String sbgnFileName: testFiles){
//			sbgnFileNameInput = sbgnFileName;
//			sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
//			inputFile = new File(sbgnFileNameInput);
//			outputFile = new File(sbmlFileNameOutput);
//			assertTrue(inputFile.exists());
//			
//			try {
//				sbgnObject = SbgnUtil.readFromFile(inputFile);
//				map = sbgnObject.getMap();
//			} catch (JAXBException e) {
//				e.printStackTrace();
//				fail("Input file is not a regular SBGN-ML file.");
//			}
//
//			converter = new SBGNML2SBML_GSOC2017(map);				
//			
//			converter.convertToSBML();		
//			sbmlDocument = new SBMLDocument(3, 1);
//			sbmlDocument.setModel(converter.model);
//			Dimensions dimensions = new Dimensions(converter.dimensionX, converter.dimensionY, converter.dimensionZ, 3, 1);
//			converter.layout.setDimensions(dimensions);
//			SBMLWriter sbmlWriter = new SBMLWriter();
//			try {
//				sbmlWriter.writeSBML(sbmlDocument, outputFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//				fail(e.getMessage());
//			}	
//			numOfFilesConverted++;
//		}
//		
//		//System.out.println(numOfFilesConverted);
//		assertEquals("Not all models were successfully converted", numOfFilesConverted, testFiles.size());
//
//	}
//	
//	@Test
//	public void testCreateCompartments(){
//		converter.createCompartments();
//
//		ListOf<Compartment> listOfCompartments = converter.model.getListOfCompartments();
//		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = converter.layout.getListOfCompartmentGlyphs();
//		
//		List<Glyph> listOfGyphs = converter.map.getGlyph();
//		int numOfCompartments = 0;
//		
//		for (Glyph glyph: listOfGyphs){
//			String clazz = glyph.getClazz();
//			if (clazz.equals("compartment")) {
//				numOfCompartments++;
//				
//				String id = glyph.getId();
//				Compartment compartment = listOfCompartments.get(id);
//				CompartmentGlyph compartmentGlyph = listOfCompartmentGlyphs.get(id + "_Glyph");
//				
//				//System.out.println(compartment.getName());
//				assertEquals(glyph.getLabel().getText(), compartment.getName());
//				
//				Bbox bbox = glyph.getBbox();
//				
//				BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
//				Dimensions dimensions = boundingBox.getDimensions();
//				Point position = boundingBox.getPosition();
//				
//				assertEquals((float) position.getX(), bbox.getX(), 0.01);
//				assertEquals((float) position.getY(), bbox.getY(), 0.01);
//				assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
//				assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);					
//			}			
//		}
//
//		assertEquals(numOfCompartments, listOfCompartments.size());
//		assertEquals(numOfCompartments, listOfCompartmentGlyphs.size());
//	}
//	
//	@Test
//	public void testCreateSpecies(){
//		converter.createSpecies();
//		
//		ListOf<Species> listOfSpecies = converter.model.getListOfSpecies();
//		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.layout.getListOfSpeciesGlyphs();			
//		
//		List<Glyph> listOfGyphs = converter.map.getGlyph();
//		int numOfEntities = 0;
//		
//		for (Glyph glyph: listOfGyphs){
//			String clazz = glyph.getClazz();
//			if (converter.isEntityPoolNode(clazz)) {
//				numOfEntities++;
//				
//				String id = glyph.getId();
//				Species species = listOfSpecies.get(id);
//				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get(id + "_Glyph");	
//				
//				if (glyph.getLabel().getText() != null){
//					//System.out.println(species.getName());
//					assertEquals(glyph.getLabel().getText(), species.getName());
//				}
//				
//				Bbox bbox = glyph.getBbox();
//				
//				BoundingBox boundingBox = speciesGlyph.getBoundingBox();
//				Dimensions dimensions = boundingBox.getDimensions();
//				Point position = boundingBox.getPosition();
//				
//				assertEquals((float) position.getX(), bbox.getX(), 0.01);
//				assertEquals((float) position.getY(), bbox.getY(), 0.01);
//				assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
//				assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);					
//			}
//		}
//		
//		assertEquals(numOfEntities, listOfSpecies.size());
//		assertEquals(numOfEntities, listOfSpeciesGlyphs.size());		
//	}
//	
//	@Test
//	public void testCreateTextGlyph(){
//		converter.createSpecies();
//		
//		ListOf<Species> listOfSpecies = converter.model.getListOfSpecies();
//		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.layout.getListOfSpeciesGlyphs();			
//		
//		List<Glyph> listOfGyphs = converter.map.getGlyph();
//		
//		for (Glyph glyph: listOfGyphs){
//			String clazz = glyph.getClazz();
//			if (converter.isEntityPoolNode(clazz)) {
//				String id = glyph.getId();
//				Species species = listOfSpecies.get(id);
//				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get(id + "_Glyph");					
//				
//				ListOf<TextGlyph> listOfTextGlyphs = converter.layout.getListOfTextGlyphs();
//				TextGlyph textGlyph = listOfTextGlyphs.get(id + "_TextGlyph");
//				
//				//System.out.println(textGlyph.getOriginOfText());
//				assertEquals(glyph.getLabel().getText(), textGlyph.getOriginOfTextInstance().getName());
//			}	
//		}
//	}
//	
//	@Test
//	public void testCreateReactions(){
//		fail("not implemented");
//	}
//
//	@Test
//	public void testCreateSpeciesReferenceCurve(){
//		fail("not implemented");
//	}
//	
//	@Test
//	public void testCreateSpeciesReferenceGlyph(){
//		fail("not implemented");
//	}
//	
//	
//	public void example_01() {
//		RenderGroup renderGroup;
//		LocalStyle localStyle;
//		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
//		
//		
//		String styleId = "LocalStyle_01";
//		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
//		initializeDefaultRenderGroup(renderGroup);
//		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
//		localRenderInformation.addLocalStyle(localStyle);
//		localStyle.getRoleList().add(styleId);
//		//localStyle.getTypeList();
//		
//		String speciesId = "Species_01";
//		String speciesName = "Protein";
//		Species species = new Species(speciesId, speciesName, 3, 1);
//		model.getListOfSpecies().add(species);
//		
//		String compartmentId = "Compartment_01";
//		Compartment compartment = new Compartment(compartmentId);
//		model.getListOfCompartments().add(compartment);
//		species.setCompartment(compartment);
//		
//		SpeciesGlyph speciesGlyph = layout.createSpeciesGlyph("SpeciesGlyph_01");
//		speciesGlyph.setSpecies(species);
//		BoundingBox boundingBox = new BoundingBox();
//		speciesGlyph.setBoundingBox(boundingBox);
//		Point point = new Point(330, 230, 0, 3, 1);
//		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
//		boundingBox.setPosition(point);
//		boundingBox.setDimensions(dimension);
//		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
//		renderGraphicalObjectPlugin.setObjectRole(styleId);		
//		//localStyle.getIDList();
//				
//		Rectangle rectangle = createRectangle(0, 0, 90, 100, 10, 10, true, true, true, true, false, false);
//		Ellipse ellipse = createEllipse(90, 50.0, 10.0, false, false, true);
//		Text text1 = createText(-10, -9.6, true, true);
//		text1.setFontFamily(FontFamily.MONOSPACE);
//		text1.setTextAnchor(HTextAnchor.MIDDLE);
//		text1.setVTextAnchor(VTextAnchor.MIDDLE);
//		text1.setName("Protein");
//		Text text2 = createText(-5, -9.6, false, true);
//		text2.setFontFamily(FontFamily.MONOSPACE);
//		text2.setTextAnchor(HTextAnchor.END);
//		text2.setVTextAnchor(VTextAnchor.MIDDLE);	
//		text2.setName("P");
//				
//		renderGroup.addElement(rectangle);
//		renderGroup.addElement(ellipse);
//		renderGroup.addElement(text1);
//		renderGroup.addElement(text2);
//		//localRenderInformation.getListOfLineEndings();
//		
//		Dimensions dimensions = new Dimensions(450, 400, 0, 3, 1);
//		this.dimensions = dimensions;
//		
//	}
//	
//	public Model example_02() {
//		Model newModel = new Model(3, 1);
//		
//		newModel.setListOfCompartments(model.getListOfCompartments());
//		newModel.setListOfSpecies(model.getListOfSpecies());
//		newModel.getListOfSpecies().remove("ATP");
//		newModel.getListOfSpecies().remove("ADP");
//		newModel.setListOfReactions(model.getListOfReactions());
//		newModel.getListOfReactions().remove("Dephosphorylation");
//		newModel.getListOfReactions().get("Phosphorylation").getListOfReactants().remove("SpeciesReference_ATP");
//		newModel.getListOfReactions().get("Phosphorylation").getListOfProducts().remove("SpeciesReference_ADP");
//		
//		LayoutModelPlugin plugin = (LayoutModelPlugin) newModel.getPlugin("layout");
//		Layout newLayout = plugin.createLayout();
//		
//		newLayout.setDimensions(layout.getDimensions());
//		newLayout.setListOfSpeciesGlyphs(layout.getListOfSpeciesGlyphs());
//		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ATP");
//		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ADP");
//		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_P");
//
//		newLayout.setListOfReactionGlyphs(layout.getListOfReactionGlyphs());
//		newLayout.getListOfReactionGlyphs().remove("ReactionGlyph_Dephosphorylation");
//		ReactionGlyph reactionGlyph = newLayout.getListOfReactionGlyphs().get(0);
//		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ATP");
//		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ADP");
//		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_P");
//
//		newLayout.setListOfTextGlyphs(layout.getListOfTextGlyphs());
//		newLayout.getListOfTextGlyphs().remove("TextGlyph_ATP");
//		newLayout.getListOfTextGlyphs().remove("TextGlyph_ADP");
//		newLayout.getListOfTextGlyphs().remove("TextGlyph_P");
//				
//		RenderLayoutPlugin newRenderLayoutPlugin = (RenderLayoutPlugin) newLayout.getPlugin(RenderConstants.shortLabel);
//		//LocalRenderInformation newLocalRenderInformation = new LocalRenderInformation("LocalRenderInformation_01");
//		//newRenderLayoutPlugin.addLocalRenderInformation(newLocalRenderInformation);			
//		
//		newRenderLayoutPlugin.setListOfLocalRenderInformation(renderLayoutPlugin.getListOfLocalRenderInformation());
//		LocalRenderInformation newLocalRenderInformation = newRenderLayoutPlugin.getListOfLocalRenderInformation().get(0);
//		if (newModel.isSetPlugin("render")) {System.out.println("[" + "NO" + "] " + newLocalRenderInformation.toString());}
//		
//		//...... not done yet
//		
//		return newModel;
//	}	
//	
//	
//	public void example_03() {
//		RenderGroup renderGroup;
//		LocalStyle localStyle;
//		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
//		
//		
//		String styleId = "LocalStyle_01";
//		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
//		initializeDefaultRenderGroup(renderGroup);
//		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
//		localRenderInformation.addLocalStyle(localStyle);
//		localStyle.getRoleList().add(styleId);
//		//localStyle.getTypeList();
//		
//		String speciesId = "Species_01";
//		String speciesName = "Protein";
//		Species species = new Species(speciesId, speciesName, 3, 1);
//		model.getListOfSpecies().add(species);
//		
//		String compartmentId = "Compartment_01";
//		Compartment compartment = new Compartment(compartmentId);
//		model.getListOfCompartments().add(compartment);
//		species.setCompartment(compartment);
//		
//		SpeciesGlyph speciesGlyph = layout.createSpeciesGlyph("SpeciesGlyph_01");
//		speciesGlyph.setSpecies(species);
//		BoundingBox boundingBox = new BoundingBox();
//		speciesGlyph.setBoundingBox(boundingBox);
//		Point point = new Point(330, 230, 0, 3, 1);
//		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
//		boundingBox.setPosition(point);
//		boundingBox.setDimensions(dimension);
//		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
//		renderGraphicalObjectPlugin.setObjectRole(styleId);		
//		//localStyle.getIDList();
//				
//		//Rectangle rectangle = createRectangle(0, 0, 90, 100, 10, 10, true, true, true, true, false, false);
//		Image image = new Image("Image_01");
//		image.setX(0);
//		image.setX(0);
//		image.setWidth(90);
//		image.setHeight(100);
//		image.setAbsoluteX(false);
//		image.setAbsoluteY(false);		
//		image.setAbsoluteWidth(false);
//		image.setAbsoluteHeight(false);		
//		image.setHref("multimer-1.png");
//		
//		Ellipse ellipse = createEllipse(90, 50.0, 10.0, false, false, true);
//		Text text1 = createText(-10, -9.6, true, true);
//		text1.setFontFamily(FontFamily.MONOSPACE);
//		text1.setTextAnchor(HTextAnchor.MIDDLE);
//		text1.setVTextAnchor(VTextAnchor.MIDDLE);
//		text1.setName("Protein");
//		Text text2 = createText(-5, -9.6, false, true);
//		text2.setFontFamily(FontFamily.MONOSPACE);
//		text2.setTextAnchor(HTextAnchor.END);
//		text2.setVTextAnchor(VTextAnchor.MIDDLE);	
//		text2.setName("P");
//				
//		renderGroup.addElement(image);
//		renderGroup.addElement(ellipse);
//		renderGroup.addElement(text1);
//		renderGroup.addElement(text2);
//		//localRenderInformation.getListOfLineEndings();
//		
//		Dimensions dimensions = new Dimensions(450, 400, 0, 3, 1);
//		this.dimensions = dimensions;
//		
//	}		
//	
//
//	public void runExample_01(){
//		String sbmlFileNameOutput;
//		File outputFile;
//
//		
//		Properties properties = new Properties();	
//		InputStream inputProperties;	
//		SBMLDocument sbmlDocument;
//		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
//		SBMLWriter sbmlWriter = new SBMLWriter();
//	
//		try {
//			inputProperties = new FileInputStream("config_unittest.properties");
//			properties.load(inputProperties);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
//		
//		//testFiles.add(examplesDirectory + "Render_example_01.xml");
//		sbmlFileNameOutput = examplesDirectory + "Render_example_01.xml";
//		outputFile = new File(sbmlFileNameOutput);	
//		
//		renderer = new SBGNML2SBMLRender();
//		//renderer.example_01();	
//		
//		sbmlDocument = new SBMLDocument(3, 1);
//		sbmlDocument.setModel(renderer.model);
//		
//		renderer.layout.setDimensions(renderer.dimensions);
//		sbmlWriter = new SBMLWriter();
//		try {
//			sbmlWriter.writeSBML(sbmlDocument, outputFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}				
//	}
//	public static Model runExample_02(){
//		String sbmlFileNameOutput;
//		String sbmlFileNameInput;
//		File outputFile;
//
//		List<String> testFiles = new ArrayList<String>();
//		
//		Properties properties = new Properties();	
//		InputStream inputProperties;	
//		SBMLDocument sbmlDocument;
//		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
//		SBMLWriter sbmlWriter = new SBMLWriter();
//	
//		try {
//			inputProperties = new FileInputStream("config_unittest.properties");
//			properties.load(inputProperties);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
//		
//		sbmlFileNameOutput = examplesDirectory + "Render_example_02.xml";
//		outputFile = new File(sbmlFileNameOutput);		
//		
//		sbmlFileNameInput = examplesDirectory + "Render_example_localRenderOnly.xml";
//		sbmlDocument = renderer.getSBMLDocument(sbmlFileNameInput);
//		
//		renderer = new SBGNML2SBMLRender(sbmlDocument.getModel());
//		Model newModel = null;
//		newModel = renderer.example_02();	
//		
//		sbmlDocument = new SBMLDocument(3, 1);
//		sbmlDocument.setModel(newModel);
//
//		try {
//			sbmlWriter.writeSBML(sbmlDocument, outputFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}	
//		
//		return newModel;
//	}
//	public void runExample_03(){
//		String sbmlFileNameOutput;
//		File outputFile;
//
//		Properties properties = new Properties();	
//		InputStream inputProperties;	
//		SBMLDocument sbmlDocument;
//		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
//		SBMLWriter sbmlWriter = new SBMLWriter();
//	
//		try {
//			inputProperties = new FileInputStream("config_unittest.properties");
//			properties.load(inputProperties);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
//		
//		sbmlFileNameOutput = examplesDirectory + "Render_example_03.xml";
//		outputFile = new File(sbmlFileNameOutput);	
//		
//		renderer = new SBGNML2SBMLRender();
//		//renderer.example_03();	
//		
//		sbmlDocument = new SBMLDocument(3, 1);
//		sbmlDocument.setModel(renderer.model);
//		
//		renderer.layout.setDimensions(renderer.dimensions);
//		try {
//			sbmlWriter.writeSBML(sbmlDocument, outputFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//	}	
//	
//
//	public Model example_04(Sbgn sbgnObject, ListOf<LineEnding> listOfLineEndings) {
//
//		Map map = sbgnObject.getMap();		
//		
//		converter = new SBGNML2SBML_GSOC2017(map);
//		converter.convertToSBML();
//		
//		converter.debugMode = 1;
////		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperCompartmentGlyphs().size());
////		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperReactionGlyphs().size());
////		converter.printHelper("example_04", converter.sWrapperModel.getListOfWrapperSpeciesGlyphs().size());
//		converter.printHelper("example_04 listOfLineEndings",listOfLineEndings.size());
//		converter.debugMode = 0;
//		
//		displayReactionGlyphInfo();
//		
//		this.listOfLineEndings = listOfLineEndings;
//		createDefaultCompartment(converter.sWrapperModel.getModel());
//		renderGeneralGlyphs(converter.sWrapperModel);
//		
//		return converter.model;
//	}
//		
//	public static void runExample_04(Model newModel){
//		String sbmlFileNameOutput;
//		String sbmlFileNameInput;
//		Sbgn sbgn;
//		
//		Properties properties = new Properties();	
//		InputStream inputProperties;	
//		SBGNML2SBMLRender renderer = new SBGNML2SBMLRender();
//		
//		// temp
//		RenderLayoutPlugin renderLayoutPlugin = (RenderLayoutPlugin) ((LayoutModelPlugin) newModel.getPlugin("layout")).getLayout(0).getPlugin(RenderConstants.shortLabel);
//		LocalRenderInformation localRenderInformation = renderLayoutPlugin.getListOfLocalRenderInformation().get(0);
//		ListOf<LineEnding> listOfLineEndings = localRenderInformation.getListOfLineEndings();
//		
//		try {
//			inputProperties = new FileInputStream("config_unittest.properties");
//			properties.load(inputProperties);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
//
//		sbmlFileNameOutput = examplesDirectory + "Render_example_04.xml";
//		sbmlFileNameInput = examplesDirectory + "or-simple.sbgn";
//
//		sbgn = SBGNML2SBML_GSOC2017.readSbgnFile(sbmlFileNameInput);
//		
//		renderer = new SBGNML2SBMLRender();
//		renderer.model = renderer.example_04(sbgn, listOfLineEndings);	
//		
//		SBGNML2SBML_GSOC2017.writeSbgnFile(sbmlFileNameOutput, renderer.model);
//
//	}
//	
//	public static void main(String[] args) {
//
//		// with species reference
//		// with image
//		// use general glyph
//		// from sbgn
//		// show 2 versions of render
//		
//		// wrappers
//		// edit points
//		// bounding box for reaction glyphs
//		
//		Model model = runExample_02();
//		runExample_04(model);
//	}	
//}
