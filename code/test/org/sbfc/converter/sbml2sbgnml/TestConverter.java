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
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
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

public class TestConverter {
	String sbmlFileNameInput = null;
	String sbgnmlFileNameOutput = null;
	File file = null;	
	List<String> testFiles = new ArrayList<String>();
	int numOfFilesConverted = 0;
	
	Properties properties = new Properties();	
	InputStream inputProperties;

	SBMLDocument sbmlDocument = null;
	Model sbmlModel = null;
	Sbgn sbgnObject = null;
	Map map = null;
	SBML2SBGNML_GSOC2017 converter = new SBML2SBGNML_GSOC2017();	
	LayoutModelPlugin sbmlLayoutModel = null;
	ListOf<Layout> listOfLayouts = null;
	
	int numAdditionalGraphicalObject = 0;
	int numCompartmentGlyphs = 0;
	int numReactionGlyphs = 0;
	int numSpeciesGlyphs = 0;
	int numTextGlyphs = 0;	
	ListOf<GraphicalObject> listOfAdditionalGraphicalObjects;
	ListOf<CompartmentGlyph> listOfCompartmentGlyphs;
	ListOf<ReactionGlyph> listOfReactionGlyphs;
	ListOf<SpeciesGlyph> listOfSpeciesGlyphs;
	ListOf<TextGlyph> listOfTextGlyphs;			
	
	@Before
	public void setUp(){
		try {
			inputProperties = new FileInputStream("config_unittest.properties");
			properties.load(inputProperties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		sbmlFileNameInput = properties.getProperty("sbml2sbgnml.unittest_file.path");
		String examplesDirectory = properties.getProperty("sbml2sbgnml.examples.path");
		
		// these files are used for testConvertToSBGNML() only
		testFiles.add(examplesDirectory + "CompartmentGlyph_example.xml");
		testFiles.add(examplesDirectory + "Complete_Example.xml");
		testFiles.add(examplesDirectory + "GeneralGlyph_Example.xml");
		testFiles.add(examplesDirectory + "ReactionGlyph_Example.xml");
		testFiles.add(examplesDirectory + "SpeciesGlyph_Example.xml");
		testFiles.add(examplesDirectory + "TextGlyph_Example.xml");		
		
		try { 
			sbmlDocument = converter.getSBMLDocument(sbmlFileNameInput);
			sbmlModel = sbmlDocument.getModel();
			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
			listOfLayouts = sbmlLayoutModel.getListOfLayouts();			
		} catch(Exception e) {
			fail("Input file is not a regular SBML layout file.");
		}
		
		for (Layout layout : listOfLayouts){
			sbgnObject = new Sbgn();
			map = new Map();
			sbgnObject.setMap(map);		

			if (layout.isSetListOfCompartmentGlyphs()){
				numCompartmentGlyphs = layout.getNumCompartmentGlyphs();
				listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();
			}			
			if (layout.isSetListOfSpeciesGlyphs()){
				numSpeciesGlyphs = layout.getNumSpeciesGlyphs();
				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
			}
			if (layout.isSetListOfAdditionalGraphicalObjects()){
				numAdditionalGraphicalObject = layout.getAdditionalGraphicalObjectCount();
				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
			}			
			if (layout.isSetListOfTextGlyphs()){
				numTextGlyphs = layout.getNumTextGlyphs();
				listOfTextGlyphs = layout.getListOfTextGlyphs();
			}			
			if (layout.isSetListOfReactionGlyphs()){
				numReactionGlyphs = layout.getNumReactionGlyphs();
				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
			}			
		}
	}
	
	@Test
	public void testConvertToSBGNML() {	
		for (String sbmlFileName: testFiles){
			//System.out.println(sbmlFileName);
			sbmlFileNameInput = sbmlFileName;
			sbgnmlFileNameOutput = sbmlFileNameInput.replaceAll(".xml", "_SBGN-ML.sbgn");
			file = new File(sbmlFileNameInput);
			assertTrue(file.exists());
					
			convertExampleFile();			
		}
		
		//System.out.println(numOfFilesConverted);
		assertEquals("Not all models were successfully converted", numOfFilesConverted, testFiles.size());
		
	}
	
	public void convertExampleFile() {
		sbmlDocument = converter.getSBMLDocument(sbmlFileNameInput);
		sbgnObject = converter.convertToSBGNML(sbmlDocument);	
		file = new File(sbgnmlFileNameOutput);
		try {
			SbgnUtil.writeToFile(sbgnObject, file);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		numOfFilesConverted += 1;
	}
	
	@Test
	public void testCreateBBox(){
		GraphicalObject sbmlGlyph = listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		converter.createBBox(sbmlGlyph, sbgnGlyph);
		
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
		GraphicalObject sbmlGlyph = listOfSpeciesGlyphs.get(0);
		Glyph sbgnGlyph = new Glyph();
		
		converter.createBBox(sbmlGlyph, sbgnGlyph);
		converter.setBBoxDimensions(sbgnGlyph, 0, 1, 0, 1);
				
		Bbox bbox = sbgnGlyph.getBbox();
		
		assertEquals(0, bbox.getX(), 0.01);
		assertEquals(0, bbox.getY(), 0.01);
		assertEquals(1, bbox.getW(), 0.01);
		assertEquals(1, bbox.getH(), 0.01);
		
	}	
		
	@Test
	public void testCreateGlyphsFomCompartmentGlyphs(){
		converter.createGlyphsFomCompartmentGlyphs(sbgnObject, listOfCompartmentGlyphs);
		int numOfGlyphs = sbgnObject.getMap().getGlyph().size();
		
		assertEquals(numCompartmentGlyphs, numOfGlyphs);
		//System.out.println(numOfGlyphs);
		
		for (Glyph sbgnGlyph : sbgnObject.getMap().getGlyph()){
			CompartmentGlyph sbmlGlyph = listOfCompartmentGlyphs.get(sbgnGlyph.getId());

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
	public void testCreateGlyphsFromGeneralGlyphs(){
		fail("not implemented");
	}
		
	@Test
	public void testCreateGlyphFromGeneralGlyph(){
		fail("not implemented");
	}
	
	@Test
	public void testCreateGlyphsFromReactionGlyphs(){
		converter.createGlyphsFromReactionGlyphs(sbgnObject, listOfReactionGlyphs);
		int numOfGlyphs = sbgnObject.getMap().getGlyph().size();
		
		assertEquals(numReactionGlyphs, numOfGlyphs);
	}	
	
	@Test
	public void testCreateGlyphFromReactionGlyph(){
		ReactionGlyph sbmlGlyph = listOfReactionGlyphs.get(0);
		converter.createGlyphFromReactionGlyph(sbgnObject, sbmlGlyph);
		
		sbgnObject.getMap().getGlyph();
		sbgnObject.getMap().getArc();
		
		// process node created
		if (sbmlGlyph.isSetCurve()) {
			int numOfGlyphs = sbgnObject.getMap().getGlyph().size();
			assertEquals(1, numOfGlyphs);
		}
		
		// same number of Arcs as number of SpeciesReference
		ListOf<SpeciesReferenceGlyph> listOfSpeciesReferenceGlyphs = sbmlGlyph.getListOfSpeciesReferenceGlyphs();
		int numOfArcs = sbgnObject.getMap().getArc().size();
		assertEquals(numOfArcs, listOfSpeciesReferenceGlyphs.size());
		
		for (SpeciesReferenceGlyph speciesReferenceGlyph : listOfSpeciesReferenceGlyphs){
			String clazz = converter.searchForReactionRole(speciesReferenceGlyph.getSpeciesReferenceRole());
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
		converter.createGlyphsFromSpeciesGlyphs(sbgnObject, listOfSpeciesGlyphs);
		int numOfGlyphs = sbgnObject.getMap().getGlyph().size();
		
		assertEquals(numSpeciesGlyphs, numOfGlyphs);
		
		for (Glyph sbgnGlyph : sbgnObject.getMap().getGlyph()){
			SpeciesGlyph sbmlGlyph = listOfSpeciesGlyphs.get(sbgnGlyph.getId());

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
		converter.createGlyphsFromSpeciesGlyphs(sbgnObject, listOfSpeciesGlyphs);
		converter.createLabelsFromTextGlyphs(sbgnObject, listOfTextGlyphs);
		
		for (TextGlyph sbmlGlyph : listOfTextGlyphs){
			if (sbmlGlyph.isSetGraphicalObject()) {
				String speciesId = sbmlGlyph.getGraphicalObjectInstance().getId();
				
				List<Glyph> listOfGlyphs = sbgnObject.getMap().getGlyph();
				int indexOfSpeciesGlyph = converter.searchForIndex(listOfGlyphs, speciesId);
				Glyph sbgnGlyph = listOfGlyphs.get(indexOfSpeciesGlyph);
				
				if (sbmlGlyph.isSetText()) {
					//System.out.println(sbgnGlyph.getLabel().getText());
					assertEquals(sbgnGlyph.getLabel().getText(), sbmlGlyph.getText());
					// todo: fix order of args
				}				
				
			}
		}
	}
	
}
