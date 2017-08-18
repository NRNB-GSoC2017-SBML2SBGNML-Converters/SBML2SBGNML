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
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Arcgroup;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.LineEnding;
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
			inputProperties = new FileInputStream("sbgnml2sbml.properties");
			properties.load(inputProperties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		sbgnFileNameInput = properties.getProperty("sbgnml2sbml.unittest_file.path");
			
		// these files are used for testConvertToSBML()
		String examplesDirectory = properties.getProperty("sbgnml2sbml.integrationtest_files.pd");
		testFiles.add(examplesDirectory + "adh.sbgn");
		testFiles.add(examplesDirectory + "compartments.sbgn");
		testFiles.add(examplesDirectory + "glycolysis.sbgn");
		testFiles.add(examplesDirectory + "multimer.sbgn");
		testFiles.add(examplesDirectory + "compartmentOrder1.sbgn");
		testFiles.add(examplesDirectory + "compartmentOrder2.sbgn");
		testFiles.add(examplesDirectory + "multimer2.sbgn");
		testFiles.add(examplesDirectory + "protein_degradation.sbgn");
		testFiles.add(examplesDirectory + "reversible-verticalpn.sbgn");	
		testFiles.add(examplesDirectory + "or-simple.sbgn");
		testFiles.add(examplesDirectory + "and.sbgn");
		testFiles.add(examplesDirectory + "annotation.sbgn");
		testFiles.add(examplesDirectory + "bool-expr-pd.sbgn");
		testFiles.add(examplesDirectory + "clone-marker.sbgn");
		testFiles.add(examplesDirectory + "edgerouting.sbgn");
		testFiles.add(examplesDirectory + "insulin-like_growth_factor_signaling.sbgn");
		testFiles.add(examplesDirectory + "labeledCloneMarker.sbgn");
		testFiles.add(examplesDirectory + "mapk_cascade.sbgn");
		testFiles.add(examplesDirectory + "neuronal_muscle_signalling.sbgn");
		testFiles.add(examplesDirectory + "states.sbgn");
		testFiles.add(examplesDirectory + "statesType2.sbgn");
		testFiles.add(examplesDirectory + "stoichiometry.sbgn");
		testFiles.add(examplesDirectory + "submap.sbgn");
		testFiles.add(examplesDirectory + "utf8_test_case_with_byte_order_mark.sbgn");
		testFiles.add(examplesDirectory + "utf8_test_case_without_byte_order_mark.sbgn");
		testFiles.add(examplesDirectory + "SBGN-PD_all.sbgn");		
		testFiles.add(examplesDirectory + "activated_stat1alpha_induction_of_the_irf1_gene.sbgn");
		examplesDirectory = properties.getProperty("sbgnml2sbml.integrationtest_files.af");
		testFiles.add(examplesDirectory + "activity-nodes.sbgn");
		testFiles.add(examplesDirectory + "auxiliary-units.sbgn");
		testFiles.add(examplesDirectory + "compartment_extended.sbgn");
		testFiles.add(examplesDirectory + "compartment.sbgn");
		testFiles.add(examplesDirectory + "delay.sbgn");
		testFiles.add(examplesDirectory + "modulation.sbgn");
		testFiles.add(examplesDirectory + "submap_expanded.sbgn");
		testFiles.add(examplesDirectory + "submap.sbgn");
		testFiles.add(examplesDirectory + "two_edges_between_two_activities.sbgn");
		examplesDirectory = properties.getProperty("sbgnml2sbml.integrationtest_files.roundtrip");
		testFiles.add(examplesDirectory + "e_coli_core_metabolism.sbgn");
		
		inputFile = new File(sbgnFileNameInput);
		
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
			map = sbgnObject.getMap();
		} catch (JAXBException e) {
			e.printStackTrace();
			fail("Input file is not a regular SBGN-ML file.");
		}

		converter = new SBGNML2SBML_GSOC2017(map);		
		converter.storeTemplateRenderInformation();
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
			converter.fileName = sbgnFileName;
			converter.storeTemplateRenderInformation();
			
			converter.convertToSBML();		
			sbmlDocument = new SBMLDocument(3, 1);
			sbmlDocument.setModel(converter.sWrapperModel.model);
			Dimensions dimensions = new Dimensions(converter.sOutput.dimensionX, converter.sOutput.dimensionY, converter.sOutput.dimensionZ, 3, 1);
			converter.sOutput.layout.setDimensions(dimensions);
			SBMLWriter sbmlWriter = new SBMLWriter();
			
			try {
				sbmlWriter.writeSBML(sbmlDocument, outputFile);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}	
			numOfFilesConverted++;
		}
		
		assertEquals("Not all models were successfully converted", numOfFilesConverted, testFiles.size());

	}
	
	@Test
	public void testCreateCompartments(){
		List<Glyph> listOfGlyphs = converter.sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = converter.sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = converter.sWrapperModel.map.getArcgroup();

		converter.addGlyphsToSWrapperModel(listOfGlyphs, listOfArcgroups);
		
		converter.createCompartments();

		ListOf<Compartment> listOfCompartments = converter.sWrapperModel.model.getListOfCompartments();
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs = converter.sOutput.layout.getListOfCompartmentGlyphs();
		
		List<Glyph> listOfGyphs = converter.sWrapperModel.map.getGlyph();
		int numOfCompartments = 0;
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (clazz.equals("compartment")) {
				numOfCompartments++;
				
				String id = glyph.getId();
				Compartment compartment = listOfCompartments.get(id);
				CompartmentGlyph compartmentGlyph = listOfCompartmentGlyphs.get("CompartmentGlyph_"+id);
				
				if (glyph.getLabel() == null){
					System.out.println("glyph.getLabel() == null");
				}
				if (compartment == null){
					System.out.println("compartment == null");
					System.out.println("id="+id);
					System.out.println("listOfCompartments="+listOfCompartments.size());
				}
				
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
		List<Glyph> listOfGlyphs = converter.sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = converter.sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = converter.sWrapperModel.map.getArcgroup();

		converter.addGlyphsToSWrapperModel(listOfGlyphs, listOfArcgroups);
		
		converter.createSpecies();
		
		ListOf<Species> listOfSpecies = converter.sWrapperModel.model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.sOutput.layout.getListOfSpeciesGlyphs();			
		
		List<Glyph> listOfGyphs = converter.sWrapperModel.map.getGlyph();
		int numOfEntities = 0;
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (converter.sUtil.isEntityPoolNode(clazz) || converter.sUtil.isLogicOperator(clazz)) {
				numOfEntities++;
				
				String id = glyph.getId();
				Species species = listOfSpecies.get(id);
				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get("SpeciesGlyph_"+id );	
				
				if (glyph.getLabel() != null && glyph.getLabel().getText() != null){
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
		List<Glyph> listOfGlyphs = converter.sWrapperModel.map.getGlyph();
		List<Arc> listOfArcs = converter.sWrapperModel.map.getArc();
		List<Arcgroup> listOfArcgroups = converter.sWrapperModel.map.getArcgroup();

		converter.addGlyphsToSWrapperModel(listOfGlyphs, listOfArcgroups);
		
		converter.createSpecies();
		
		ListOf<Species> listOfSpecies = converter.sWrapperModel.model.getListOfSpecies();
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs = converter.sOutput.layout.getListOfSpeciesGlyphs();			
		
		List<Glyph> listOfGyphs = converter.sWrapperModel.map.getGlyph();
		
		for (Glyph glyph: listOfGyphs){
			String clazz = glyph.getClazz();
			if (converter.sUtil.isEntityPoolNode(clazz)) {
				String id = glyph.getId();
				Species species = listOfSpecies.get(id);
				SpeciesGlyph speciesGlyph = listOfSpeciesGlyphs.get("SpeciesGlyph_"+id);					
				
				ListOf<TextGlyph> listOfTextGlyphs = converter.sOutput.layout.getListOfTextGlyphs();
				TextGlyph textGlyph = listOfTextGlyphs.get( "TextGlyph_"+id );
				
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
