package org.sbfc.converter.sbml2sbgnml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBGNModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbfc.converter.utils.sbgn.SBGNUtils;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.xml.sax.SAXException;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbgn.bindings.Sbgn;

public class SBML2SBGNML_temp extends GeneralConverter { 
	
	private static Logger logger = Logger.getLogger(SBML2SBGNML_temp.class);
	
	private Model sbmlModel;
	private static SBGNUtils sbu = new SBGNUtils("sbgnml");
	
	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
		
		BasicConfigurator.configure();
		
		if (args.length < 1 || args.length > 3) {
			System.out.println("usage: java org.sbfc.converter.sbml2sbgnml.SBML2SBGN <SBML filename> [SBGNML milestone for output file]");
			return;
		}

		String sbmlFileNameInput = args[0];
		
		SBML2SBGNML_temp sbml = new SBML2SBGNML_temp();
				
		SBMLDocument sbmlDocument = sbml.getSBMLDocument(sbmlFileNameInput);

		if (sbmlDocument == null) {
			System.exit(1);
		}
		
		String outputFile = sbmlFileNameInput + "_SBGN-ML" ;
		
		// visualize JTree
		try {		
			sbml.visualizeJTree(sbmlDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		Sbgn sbgnObject = sbml.convertSBGNML(sbmlDocument);	
		
		File f = new File(outputFile);
		try {
			SbgnUtil.writeToFile(sbgnObject, f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		System.out.println("output file at: " + outputFile);

	}
	
	public class JSBMLvisualizer extends JFrame {
		public JSBMLvisualizer(SBase tree) {
			super("SBML Structure Visualization");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			getContentPane().add(new JScrollPane(new JTree(tree)));
			pack();
			setAlwaysOnTop(true);
			setLocationRelativeTo(null);
			setVisible(true);			
		}
	}	
	
	public void visualizeJTree(SBMLDocument sbmlDocument) throws 
	ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// hierarchy displayed by the Java JTree object
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new JSBMLvisualizer(sbmlDocument);		
		return;
	}
	
	public Sbgn convertSBGNML(SBMLDocument sbmlDocument) throws SBMLException {

		Sbgn sbgnObject = null;
		Map map = null;
		
		LayoutModelPlugin sbmlLayoutModel = null;
		ListOf<Layout> listOfLayouts = null;
		HashMap<String, Sbgn> listOfSbgnObjects = new HashMap<String, Sbgn>();
		
		int numAdditionalGraphicalObject = 0;
		int numCompartmentGlyphs = 0;
		int numReactionGlyphs = 0;
		int numSpeciesGlyphs = 0;
		int numTextGlyphs = 0;			
		
		Dimensions layoutDimensions = null;
		ListOf<GraphicalObject> listOfAdditionalGraphicalObjects;
		ListOf<CompartmentGlyph> listOfCompartmentGlyphs;
		ListOf<ReactionGlyph> listOfReactionGlyphs;
		ListOf<SpeciesGlyph> listOfSpeciesGlyphs;
		ListOf<TextGlyph> listOfTextGlyphs;		

		try { 
			sbmlModel = sbmlDocument.getModel();
		} catch(Exception e) {
			throw new SBMLException("SBML2SBGN: Input file is not a regular SBML file.");
		}
		
		//System.out.println(sbmlModel.isSetPlugin("layout"));
		if (sbmlModel.isSetPlugin("layout")){
			sbmlLayoutModel = (LayoutModelPlugin) sbmlModel.getExtension("layout");
		}

		if (sbmlModel.isSetPlugin("layout")){
			listOfLayouts = sbmlLayoutModel.getListOfLayouts();
		}
		
		System.out.println("listOfLayouts size=" + Integer.toString(sbmlLayoutModel.getLayoutCount()));
		//System.out.println(sbmlLayoutModel.toString());
		
		
		for (Layout layout : listOfLayouts){
			sbgnObject = new Sbgn();
			map = new Map();
			sbgnObject.setMap(map);		
			
			listOfSbgnObjects.put(layout.getId(), sbgnObject);
			
			if (layout.isSetDimensions()){
				layoutDimensions = layout.getDimensions();
				System.out.println(layoutDimensions.toString());
			}
			
			if (layout.isSetListOfAdditionalGraphicalObjects()){
				numAdditionalGraphicalObject = layout.getAdditionalGraphicalObjectCount();
				listOfAdditionalGraphicalObjects = layout.getListOfAdditionalGraphicalObjects();
				//System.out.println(listOfAdditionalGraphicalObjects.toString());
			}
			if (layout.isSetListOfCompartmentGlyphs()){
				numCompartmentGlyphs = layout.getNumCompartmentGlyphs();
				listOfCompartmentGlyphs = layout.getListOfCompartmentGlyphs();
				//System.out.println(listOfCompartmentGlyphs.toString());
				System.out.println("numCompartmentGlyphs = " + Integer.toString(numCompartmentGlyphs));
				createSbgnCompartmentGlyphs(sbgnObject, listOfCompartmentGlyphs);
				
			}			
			if (layout.isSetListOfReactionGlyphs()){
				numReactionGlyphs = layout.getNumReactionGlyphs();
				listOfReactionGlyphs = layout.getListOfReactionGlyphs();
				//System.out.println(listOfReactionGlyphs.toString());
			}			
			if (layout.isSetListOfSpeciesGlyphs()){
				numSpeciesGlyphs = layout.getNumSpeciesGlyphs();
				listOfSpeciesGlyphs = layout.getListOfSpeciesGlyphs();
				//System.out.println(listOfSpeciesGlyphs.toString());
			}
			if (layout.isSetListOfTextGlyphs()){
				numTextGlyphs = layout.getNumTextGlyphs();
				listOfTextGlyphs = layout.getListOfTextGlyphs();
				//System.out.println(listOfTextGlyphs.toString());
			}			
			
		}
		
		// return one of the sbgnObjects?
		return sbgnObject;		
	}
	
	public void createSbgnCompartmentGlyphs(Sbgn sbgnObject, ListOf<CompartmentGlyph> listOfCompartmentGlyphs) {
		Glyph sbgnCompartmentGlyph;
		Label label;
		
		for (CompartmentGlyph compartmentGlyph : listOfCompartmentGlyphs){
			sbgnCompartmentGlyph = new Glyph();
			sbgnCompartmentGlyph.setId(compartmentGlyph.getId());
			sbgnCompartmentGlyph.setClazz("compartment");
			
			sbgnObject.getMap().getGlyph().add(sbgnCompartmentGlyph);		
			
			label = new Label();
			label.setText(compartmentGlyph.getId());
			sbgnCompartmentGlyph.setLabel(label);
			
			createSbgnBBox(compartmentGlyph, sbgnCompartmentGlyph);
			
			// create Auxiliary items
		}

	}
	
	public void createSbgnBBox(CompartmentGlyph compartmentGlyph, Glyph glyph) {
		Bbox bbox = new Bbox();
		
		BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
		Dimensions dimensions = boundingBox.getDimensions();
		Point position = boundingBox.getPosition();

		// can't set depth
		double depth = dimensions.getDepth();
		double height = dimensions.getHeight();
		double width = dimensions.getWidth();	
		
		// one of the values will be lost
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();
		
		bbox.setX((float) x);
		bbox.setY((float) y);
		bbox.setH((float) height);
		bbox.setW((float) width);
		
		glyph.setBbox(bbox);
		
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
	
	@Override
	public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

		try {
			inputModel = model;
			SBMLDocument sbmlDoc = ((SBMLModel) model).getSBMLDocument();
			Sbgn sbgnObj = convertSBGNML(sbmlDoc);
			SBGNModel outputModel = new SBGNModel(sbgnObj);
			return outputModel;
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new ConversionException(e.getMessage());
		}
	}


	@Override
	public String getResultExtension() {
		return ".sbgn";
	}
	
	@Override
	public String getName() {
		return "SBML2SBGNML";
	}
	
	@Override
	public String getDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}

	@Override
	public String getHtmlDescription() {
		return "It converts a model format from SBML to SBGN-ML";
	}
}
