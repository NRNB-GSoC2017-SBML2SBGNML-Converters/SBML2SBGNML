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
import org.sbfc.converter.sbgnml2sbml.SBGNML2SBMLOutput;
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

	List<String> testFiles = new ArrayList<String>();

	@Before
	public void setUp(){
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		testFiles.add(examplesDirectory + "CompartmentGlyph_Example_level2_level3.xml");	
		testFiles.add(examplesDirectory + "Complete_Example_level2_level3.xml");	
		testFiles.add(examplesDirectory + "ReactionGlyph_Example_level2+id_level3.xml");	
		testFiles.add(examplesDirectory + "SpeciesGlyph_Example_level2_level3.xml");	
		testFiles.add(examplesDirectory + "TextGlyph_Example_level2_level3.xml");	
		

		examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.qual");
		testFiles.add(examplesDirectory + "4.1 Simple Logical Regulatory Graph.xml");	
		
		examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.roundtrip");
		testFiles.add(examplesDirectory + "e_coli_core_metabolism.sbml.xml");
	}
	
	@Test
	public void testConvertToSBGNML() {	
	
		int numOfFilesConverted = 0;
		
		for (String sbmlFileName: testFiles){
			//System.out.println(sbmlFileName);
			String sbmlFileNameInput = sbmlFileName;
			String sbgnmlFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
			File file = new File(sbmlFileNameInput);
			assertTrue(file.exists());
				
			System.out.println("convertExampleFile "+ sbmlFileNameInput);
			SBMLDocument sbmlDocument = SBML2SBGNMLUtil.getSBMLDocument(sbmlFileNameInput);
			SBML2SBGNML_GSOC2017 converter = new SBML2SBGNML_GSOC2017();
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
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		
		GraphicalObject sbmlGlyph = sOutput.listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		SBML2SBGNMLUtil.createBBox(sbmlGlyph, sbgnGlyph);
		
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
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		
		GraphicalObject sbmlGlyph = sOutput.listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		SBML2SBGNMLUtil.createBBox(sbmlGlyph, sbgnGlyph);
		SBML2SBGNMLUtil.setBBoxDimensions(sbgnGlyph, 0, 1, 0, 1);
				
		Bbox bbox = sbgnGlyph.getBbox();
		
		assertEquals(0, bbox.getX(), 0.01);
		assertEquals(0, bbox.getY(), 0.01);
		assertEquals(1, bbox.getW(), 0.01);
		assertEquals(1, bbox.getH(), 0.01);
		
	}	
		
	@Test
	public void testCreateGlyphsFomCompartmentGlyphs(){
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		converter.createFromCompartmentGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfCompartmentGlyphs);
		int numOfGlyphs = sOutput.sbgnObject.getMap().getGlyph().size();
		
		assertEquals(sOutput.listOfCompartmentGlyphs.size(), numOfGlyphs);
		
		for (Glyph sbgnGlyph : sOutput.sbgnObject.getMap().getGlyph()){
			CompartmentGlyph sbmlGlyph = sOutput.listOfCompartmentGlyphs.get(sbgnGlyph.getId());

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
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		converter.createFromReactionGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfReactionGlyphs);
		int numOfGlyphs = sOutput.sbgnObject.getMap().getArcgroup().size();
		
		assertEquals(sOutput.listOfReactionGlyphs.size(), numOfGlyphs);
	}	
	
	@Test
	public void testCreateGlyphFromReactionGlyph(){
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		ReactionGlyph sbmlGlyph = sOutput.listOfReactionGlyphs.get(0);
		converter.createFromOneReactionGlyph(sOutput, sWrapperMap, sOutput.sbgnObject, sbmlGlyph);
		
		sOutput.sbgnObject.getMap().getGlyph();
		sOutput.sbgnObject.getMap().getArc();
		
		// process node created
		if (sbmlGlyph.isSetCurve()) {
			int numOfGlyphs = sOutput.sbgnObject.getMap().getArcgroup().size();
			assertEquals(1, numOfGlyphs);
		}
		
		// same number of Arcs as number of SpeciesReference
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs = sbmlGlyph.getListOfSpeciesReferenceGlyphs();
		int numOfArcs = sOutput.sbgnObject.getMap().getArc().size();
		assertEquals(numOfArcs, listOfSpeciesReferenceGlyphs.size());
		
		for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
			String clazz = SBML2SBGNMLUtil.searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
			// TODO: need to find the arc corresponding to a speciesReferenceGlyph, and test them
		}
		
		//TODO: test arc coordinates
		
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
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		converter.createFromSpeciesGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfSpeciesGlyphs);
		int numOfGlyphs = sOutput.sbgnObject.getMap().getGlyph().size();
		
		assertEquals(sOutput.listOfSpeciesGlyphs.size(), numOfGlyphs);
		
		for (Glyph sbgnGlyph : sOutput.sbgnObject.getMap().getGlyph()){
			SpeciesGlyph sbmlGlyph = sOutput.listOfSpeciesGlyphs.get(sbgnGlyph.getId());

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
		String sbmlFileNameInput = null;
		String sbgnmlFileNameOutput = null;
		File file = null;	
				
		Properties properties = new Properties();	
		InputStream inputProperties;
		
		SBMLDocument sbmlDocument = null;
		SBML2SBGNML_GSOC2017 converter = null;
		
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
		
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		converter = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		SWrapperMap sWrapperMap = new SWrapperMap(sOutput.map, sOutput.sbmlModel);
		
		converter.createFromSpeciesGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfSpeciesGlyphs);
		converter.createLabelsFromTextGlyphs(sOutput, sWrapperMap, sOutput.sbgnObject, sOutput.listOfTextGlyphs);
		
		for (TextGlyph sbmlGlyph : sOutput.listOfTextGlyphs){
			if (sbmlGlyph.isSetGraphicalObject()) {
				String speciesId = sbmlGlyph.getGraphicalObjectInstance().getId();
				
				List<Glyph> listOfGlyphs = sOutput.sbgnObject.getMap().getGlyph();
				int indexOfSpeciesGlyph = SBML2SBGNMLUtil.searchForIndex(listOfGlyphs, speciesId);
				Glyph sbgnGlyph = listOfGlyphs.get(indexOfSpeciesGlyph);
				
				if (sbmlGlyph.isSetText()) {
					assertEquals(sbgnGlyph.getLabel().getText(), sbmlGlyph.getText());
					// TODO: fix order of args, the first arg is the Expected value, the second is the Actual
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
	

	@Test
	/**
	 * This is an example to create an SBML Model with layout and render.
	 */
	public void example_01() {
		String sbmlFileNameOutput;
		File outputFile;

		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBGNML2SBMLRender renderer;
		SBMLWriter sbmlWriter = new SBMLWriter();
	
		try {
			inputProperties = new FileInputStream("sbml2sbgnml.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.render");
		
		sbmlFileNameOutput = examplesDirectory + "Render_example_01.xml";
		outputFile = new File(sbmlFileNameOutput);	
		
		SBGNML2SBMLOutput sOutput = new SBGNML2SBMLOutput(3, 1);
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;

		RenderGroup renderGroup;
		LocalStyle localStyle;
		String styleId = "LocalStyle_01";
		renderGroup = new RenderGroup(3, 1);
		SBGNML2SBMLRender.initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, 3, 1, renderGroup);

		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		//localStyle.getTypeList();
		
		String speciesId = "Species_01";
		String speciesName = "Protein";
		Species species = new Species(speciesId, speciesName, 3, 1);
		sOutput.model.getListOfSpecies().add(species);

		String compartmentId = "Compartment_01";
		Compartment compartment = new Compartment(compartmentId);
		sOutput.model.getListOfCompartments().add(compartment);
		species.setCompartment(compartment);
		
		SpeciesGlyph speciesGlyph =  new SpeciesGlyph("SpeciesGlyph_01", 3,1);
		sOutput.layout.addSpeciesGlyph(speciesGlyph);
		speciesGlyph.setSpecies(species);
		BoundingBox boundingBox = new BoundingBox(3, 1);
		speciesGlyph.setBoundingBox(boundingBox);
		Point point = new Point(330, 230, 0, 3, 1);
		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
		boundingBox.setPosition(point);
		boundingBox.setDimensions(dimension);
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		//localStyle.getIDList();
				
		Rectangle rectangle = SBGNML2SBMLRender.createRectangle(0, 0, 90, 100, 10, 10, true, true, true, true, false, false);
		Ellipse ellipse = SBGNML2SBMLRender.createEllipse(90, 50.0, 10.0, false, false, true);
		Text text1 = SBGNML2SBMLRender.createText(-10, -9.6, true, true);
		text1.setFontFamily(FontFamily.MONOSPACE);
		text1.setTextAnchor(HTextAnchor.MIDDLE);
		text1.setVTextAnchor(VTextAnchor.MIDDLE);
		text1.setName("Protein");
		Text text2 = SBGNML2SBMLRender.createText(-5, -9.6, false, true);
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
		sOutput.layout.setDimensions(dimensions);
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(sOutput.model);

		sbmlWriter = new SBMLWriter();
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	@Test
	/**
	 * This is an example to create an SBML Model with layout by transferring objects from an existing Model
	 */
	public void example_02() {
		String sbmlFileNameOutput;
		String sbmlFileNameInput;
		File outputFile;

		List<String> testFiles = new ArrayList<String>();
		
		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBMLWriter sbmlWriter = new SBMLWriter();
	
		try {
			inputProperties = new FileInputStream("sbml2sbgnml.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.render");
		
		sbmlFileNameOutput = examplesDirectory + "Render_example_02.xml";
		outputFile = new File(sbmlFileNameOutput);		
		
		sbmlFileNameInput = examplesDirectory + "Render_example_localRenderOnly.xml";
		sbmlDocument = SBGNML2SBMLOutput.getSBMLDocument(sbmlFileNameInput);
		SBML2SBGNML_GSOC2017 SBML2SBGNML = new SBML2SBGNML_GSOC2017();
		SBML2SBGNMLOutput sOutput = new SBML2SBGNMLOutput(sbmlDocument);
		
		Model newModel = new Model(3, 1);
		newModel.setNamespace("http://www.sbml.org/sbml/level3/version1/core");
		
		newModel.setListOfCompartments(sOutput.sbmlModel.getListOfCompartments());
		newModel.setListOfSpecies(sOutput.sbmlModel.getListOfSpecies());
		newModel.getListOfSpecies().remove("ATP");
		newModel.getListOfSpecies().remove("ADP");
		newModel.setListOfReactions(sOutput.sbmlModel.getListOfReactions());
		newModel.getListOfReactions().remove("Dephosphorylation");
		newModel.getListOfReactions().get("Phosphorylation").getListOfReactants().remove("SpeciesReference_ATP");
		newModel.getListOfReactions().get("Phosphorylation").getListOfProducts().remove("SpeciesReference_ADP");
		
		LayoutModelPlugin plugin = (LayoutModelPlugin) newModel.getPlugin("layout");
		Layout newLayout = plugin.createLayout();
		
		newLayout.setDimensions(sOutput.layout.getDimensions());
		newLayout.setListOfSpeciesGlyphs(sOutput.layout.getListOfSpeciesGlyphs());
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ATP");
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_ADP");
		newLayout.getListOfSpeciesGlyphs().remove("SpeciesGlyph_P");

		newLayout.setListOfReactionGlyphs(sOutput.layout.getListOfReactionGlyphs());
		newLayout.getListOfReactionGlyphs().remove("ReactionGlyph_Dephosphorylation");
		ReactionGlyph reactionGlyph = newLayout.getListOfReactionGlyphs().get(0);
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ATP");
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_ADP");
		reactionGlyph.getListOfSpeciesReferenceGlyphs().remove("SpeciesReferenceGlyph_P");

		newLayout.setListOfTextGlyphs(sOutput.layout.getListOfTextGlyphs());
		newLayout.getListOfTextGlyphs().remove("TextGlyph_ATP");
		newLayout.getListOfTextGlyphs().remove("TextGlyph_ADP");
		newLayout.getListOfTextGlyphs().remove("TextGlyph_P");
						
		//TODO: not done yet
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(newModel);

		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}	
	
	@Test
	/**
	 * This is an example to create an SBML Model with layout and render, where the rendered style uses an Image
	 */
	public void example_03() {
		String sbmlFileNameOutput;
		File outputFile;

		Properties properties = new Properties();	
		InputStream inputProperties;	
		SBMLDocument sbmlDocument;
		SBMLWriter sbmlWriter = new SBMLWriter();
	
		try {
			inputProperties = new FileInputStream("sbml2sbgnml.properties");
			properties.load(inputProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String examplesDirectory = properties.getProperty("sbml2sbgnml.integrationtest_files.render");
		
		sbmlFileNameOutput = examplesDirectory + "Render_example_03.xml";
		outputFile = new File(sbmlFileNameOutput);	

		SBGNML2SBMLOutput sOutput = new SBGNML2SBMLOutput(3, 1);
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;

		RenderGroup renderGroup;
		LocalStyle localStyle;

		String styleId = "LocalStyle_01";
		renderGroup = new RenderGroup(3, 1);
		SBGNML2SBMLRender.initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, 3, 1, renderGroup);
		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		//localStyle.getTypeList();
		
		String speciesId = "Species_01";
		String speciesName = "Protein";
		Species species = new Species(speciesId, speciesName, 3, 1);
		sOutput.model.getListOfSpecies().add(species);
		
		String compartmentId = "Compartment_01";
		Compartment compartment = new Compartment(compartmentId);
		sOutput.model.getListOfCompartments().add(compartment);
		species.setCompartment(compartment);
		
		SpeciesGlyph speciesGlyph =  new SpeciesGlyph("SpeciesGlyph_01", 3, 1);
		sOutput.layout.addSpeciesGlyph(speciesGlyph);
		speciesGlyph.setSpecies(species);
		BoundingBox boundingBox = new BoundingBox();
		speciesGlyph.setBoundingBox(boundingBox);
		Point point = new Point(330, 230, 0, 3, 1);
		Dimensions dimension = new Dimensions(93, 40, 0, 3, 1);
		boundingBox.setPosition(point);
		boundingBox.setDimensions(dimension);
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) speciesGlyph.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		//localStyle.getIDList();
				
		Image image = new Image("Image_01");
		image.setX(0);
		image.setX(0);
		image.setWidth(90);
		image.setHeight(100);
		image.setAbsoluteX(false);
		image.setAbsoluteY(false);		
		image.setAbsoluteWidth(false);
		image.setAbsoluteHeight(false);		
		image.setHref("complex-glyph.png");
		
		Ellipse ellipse = SBGNML2SBMLRender.createEllipse(90, 50.0, 10.0, false, false, true);
		Text text1 = SBGNML2SBMLRender.createText(-10, -9.6, true, true);
		text1.setFontFamily(FontFamily.MONOSPACE);
		text1.setTextAnchor(HTextAnchor.MIDDLE);
		text1.setVTextAnchor(VTextAnchor.MIDDLE);
		text1.setName("Protein");
		Text text2 = SBGNML2SBMLRender.createText(-5, -9.6, false, true);
		text2.setFontFamily(FontFamily.MONOSPACE);
		text2.setTextAnchor(HTextAnchor.END);
		text2.setVTextAnchor(VTextAnchor.MIDDLE);	
		text2.setName("P");
				
		renderGroup.addElement(image);
		renderGroup.addElement(ellipse);
		renderGroup.addElement(text1);
		renderGroup.addElement(text2);
		
		Dimensions dimensions = new Dimensions(450, 400, 0, 3, 1);
		sOutput.layout.setDimensions(dimensions);
		
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(sOutput.model);

		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}		
		
}
