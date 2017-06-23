package org.sbfc.converter.sbgnml2sbml;

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
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

public class TestConverter {
	String sbgnFileNameInput;
	String sbmlFileNameOutput;
	File inputFile;
	File outputFile;
	List<String> testFiles = new ArrayList<String>();
	int numOfFilesConverted = 0;
	
	Properties properties = new Properties();	
	InputStream inputProperties;	
	
	Sbgn sbgnObject = null;
	Map map;
	SBGNML2SBML_GSOC2017 converter;
	SBMLDocument sbmlDocument;
		
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
		sbgnFileNameInput = properties.getProperty("sbgnml2sbml.unittest_file.path");
		String examplesDirectory = properties.getProperty("sbgnml2sbml.examples.path");
		
		// these files are used for testConvertToSBML() only
		testFiles.add(examplesDirectory + "adh.sbgn");
		testFiles.add(examplesDirectory + "compartments.sbgn");
		testFiles.add(examplesDirectory + "glycolysis.sbgn");
		testFiles.add(examplesDirectory + "multimer.sbgn");
		testFiles.add(examplesDirectory + "compartmentOrder1.sbgn");
		testFiles.add(examplesDirectory + "compartmentOrder2.sbgn");
		testFiles.add(examplesDirectory + "multimer2.sbgn");
		testFiles.add(examplesDirectory + "protein_degradation.sbgn");
		testFiles.add(examplesDirectory + "reversible-verticalpn.sbgn");	
		//testFiles.add(examplesDirectory + "or-simple.sbgn");
		
		inputFile = new File(sbgnFileNameInput);
		
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
			map = sbgnObject.getMap();
		} catch (JAXBException e) {
			e.printStackTrace();
			fail("Input file is not a regular SBGN-ML file.");
		}

		converter = new SBGNML2SBML_GSOC2017(map);		
	}
	
	@Test
	public void testConvertToSBML() {
		
		for (String sbgnFileName: testFiles){
			sbgnFileNameInput = sbgnFileName;
			sbmlFileNameOutput = sbgnFileNameInput.replaceAll("\\.sbgn", "_SBML.xml");
			inputFile = new File(sbgnFileNameInput);
			outputFile = new File(sbmlFileNameOutput);
			assertTrue(inputFile.exists());
			
			try {
				sbgnObject = SbgnUtil.readFromFile(inputFile);
				map = sbgnObject.getMap();
			} catch (JAXBException e) {
				e.printStackTrace();
				fail("Input file is not a regular SBGN-ML file.");
			}

			converter = new SBGNML2SBML_GSOC2017(map);				
			
			converter.convertToSBML();		
			sbmlDocument = new SBMLDocument(3, 1);
			sbmlDocument.setModel(converter.model);
			Dimensions dimensions = new Dimensions(converter.dimensionX, converter.dimensionY, converter.dimensionZ, 3, 1);
			converter.layout.setDimensions(dimensions);
			SBMLWriter sbmlWriter = new SBMLWriter();
			try {
				sbmlWriter.writeSBML(sbmlDocument, outputFile);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}	
			numOfFilesConverted++;
		}
		
		//System.out.println(numOfFilesConverted);
		assertEquals("Not all models were successfully converted", numOfFilesConverted, testFiles.size());

	}
	
	@Test
	public void testCreateCompartments(){
		converter.createCompartments();

		ListOf<Compartment> listOfCompartments = converter.model.getListOfCompartments();
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = converter.layout.getListOfCompartmentGlyphs();
		
		List<Glyph> listOfGyphs = converter.map.getGlyph();
		int numOfCompartments = 0;
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (clazz.equals("compartment")) {
				numOfCompartments++;
				
				String id = glyph.getId();
				Compartment compartment = listOfCompartments.get(id);
				CompartmentGlyph compartmentGlyph = listOfCompartmentGlyphs.get(id + "_Glyph");
				
				//System.out.println(compartment.getName());
				assertEquals(glyph.getLabel().getText(), compartment.getName());
				
				Bbox bbox = glyph.getBbox();
				
				BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
				Dimensions dimensions = boundingBox.getDimensions();
				Point position = boundingBox.getPosition();
				
				assertEquals((float) position.getX(), bbox.getX(), 0.01);
				assertEquals((float) position.getY(), bbox.getY(), 0.01);
				assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
				assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);					
			}			
		}

		assertEquals(numOfCompartments, listOfCompartments.size());
		assertEquals(numOfCompartments, listOfCompartmentGlyphs.size());
	}
	
	@Test
	public void testCreateSpecies(){
		converter.createSpecies();
		
		ListOf<Species> listOfSpecies = converter.model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.layout.getListOfSpeciesGlyphs();			
		
		List<Glyph> listOfGyphs = converter.map.getGlyph();
		int numOfEntities = 0;
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (converter.isEntityPoolNode(clazz)) {
				numOfEntities++;
				
				String id = glyph.getId();
				Species species = listOfSpecies.get(id);
				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get(id + "_Glyph");	
				
				if (glyph.getLabel().getText() != null){
					//System.out.println(species.getName());
					assertEquals(glyph.getLabel().getText(), species.getName());
				}
				
				Bbox bbox = glyph.getBbox();
				
				BoundingBox boundingBox = speciesGlyph.getBoundingBox();
				Dimensions dimensions = boundingBox.getDimensions();
				Point position = boundingBox.getPosition();
				
				assertEquals((float) position.getX(), bbox.getX(), 0.01);
				assertEquals((float) position.getY(), bbox.getY(), 0.01);
				assertEquals((float) dimensions.getWidth(), bbox.getW(), 0.01);
				assertEquals((float) dimensions.getHeight(), bbox.getH(), 0.01);					
			}
		}
		
		assertEquals(numOfEntities, listOfSpecies.size());
		assertEquals(numOfEntities, listOfSpeciesGlyphs.size());		
	}
	
	@Test
	public void testCreateTextGlyph(){
		converter.createSpecies();
		
		ListOf<Species> listOfSpecies = converter.model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.layout.getListOfSpeciesGlyphs();			
		
		List<Glyph> listOfGyphs = converter.map.getGlyph();
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (converter.isEntityPoolNode(clazz)) {
				String id = glyph.getId();
				Species species = listOfSpecies.get(id);
				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get(id + "_Glyph");					
				
				ListOf<TextGlyph> listOfTextGlyphs = converter.layout.getListOfTextGlyphs();
				TextGlyph textGlyph = listOfTextGlyphs.get(id + "_TextGlyph");
				
				//System.out.println(textGlyph.getOriginOfText());
				assertEquals(glyph.getLabel().getText(), textGlyph.getOriginOfTextInstance().getName());
			}	
		}
	}
	
	@Test
	public void testCreateReactions(){
		fail("not implemented");
	}

	@Test
	public void testCreateSpeciesReferenceCurve(){
		fail("not implemented");
	}
	
	@Test
	public void testCreateSpeciesReferenceGlyph(){
		fail("not implemented");
	}
	
}
