package org.sbfc.converter.sbmlsbml;

import java.util.HashMap;
import java.util.Map;

import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;
import org.sbml.libsbml.ConversionProperties;
import org.sbml.libsbml.LayoutModelPlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;

/**
 * Converts a document (including layout and render) from L2 to L3V1
 * 
 * <p>The conversion code is taken from <a href="http://sbml.org/Software/libSBML">libSBML</a>, from the example file
 * SimpleLayoutConverter.java.</p>
 * 
 * @author rodrigue
 * @author Frank Bergmann
 */
public class SBMLLayoutL2ToL3 extends GeneralConverter {

  /**
   * 
   */
  private static boolean isLibSBMLAvailable = false;

  static {
    try {
      //System.loadLibrary("sbmlj");
    	System.load("C:/Program Files/SBML/libSBML-5.15.0-libxml2-x64/bindings/java/sbmlj.dll"); 

      Class.forName("org.sbml.libsbml.libsbml");

      isLibSBMLAvailable = true;
      

    } catch (SecurityException e) {
      // System.out.println("SecurityException exception catch : Could not load libsbml library.");
      throw e;
    } catch (UnsatisfiedLinkError e) { 
      // always sending an exception so that the SBFC framework know there is a problem and the actual exception message can be displayed
      // System.out.println("UnsatisfiedLinkError exception catch : Could not load libsbml library.");
      // System.out.println("You need to install libsbml before being able to use the SBML2SBML converter.");
      throw new RuntimeException("You need to install libsbml before being able to use the SBML2SBML converter.", e);
    } catch (ClassNotFoundException e) {
      // e.printStackTrace();
      // System.out.println("ClassNotFoundException exception catch : Could not load libsbml class file.");
    } catch (RuntimeException e) {
      e.printStackTrace();
      System.out
      .println("Could not load libsbml.\n "
          + "Control that the libsbmlj.jar that you are using is synchronized with your current libSBML installation.");

    }
  }

  @Override
  public GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException {

    if (! (model instanceof SBMLModel)) {
      return null;
    }
    inputModel = model;
    SBMLModel sbmlModel = (SBMLModel) model;

    String currentSBML;
    try {
      currentSBML = sbmlModel.modelToString();

    } catch (WriteModelException e1) {
      e1.printStackTrace();
      return null;
    }
    
    //System.out.println("currentSBML=\n"+currentSBML);

    if (isLibSBMLAvailable) {
    	//System.out.println("isLibSBMLAvailable");
      // Code using libSBML directly
      SBMLReader libSBMLReader = new SBMLReader();
      //org.sbml.libsbml.SBMLDocument ldoc = libSBMLReader.readSBMLFromString(currentSBML);
      org.sbml.libsbml.SBMLDocument ldoc = libSBMLReader.readSBMLFromFile("C:/Users/HY/Documents/SBML2SBGN/sbfc_new/examples/Complete_Example_level2.xml");

      
      System.out.println("libsbml version = " + org.sbml.libsbml.libsbml.getLibSBMLDottedVersion());

      convertToL3(ldoc);

      SBMLWriter libSBMLWriter = new SBMLWriter();

      String targetSBML = libSBMLWriter.writeSBMLToString(ldoc);
      System.out.println("targetSBML=\n"+targetSBML);

      // System.out.println("SBML2SBML : converted model : \n" + targetSBML.substring(0, 150));

      SBMLModel targetModel = new SBMLModel();

      try {
        targetModel.modelFromString(targetSBML);

        return targetModel;

      } catch (ReadModelException e) {
        e.printStackTrace();
        return null;
      }
    }

    // we are here because libSBML was not available
    throw new ConversionException("LibSBML need to be installed on your system and configured to work with SBFC for this converter to work.");
  }

  @Override
  public String getResultExtension() {
      return "-L3V1.xml";
  }

  @Override
  public String getName() {
      return "SBML2SBML";
  }
  
  @Override
  public String getDescription() {
      return "Converts an SBML model format to another with different Level/Version";
  }

  @Override
  public String getHtmlDescription() {
      return "Converts an SBML model format to another with different Level/Version";
  }

  /**
   * Converts a document (including layout and render) from L2 to L3V1
   * 
   * @param doc the SBML document that will be converted
   * @throws ConversionException if there is a problem during conversion
   */
  private static void convertToL3(org.sbml.libsbml.SBMLDocument doc) throws ConversionException
  {
    // code taken from libSBML:  examples/java/layout/SimpleLayoutConverter.java
    
    if (doc == null || doc.getModel() == null) return;

    String layoutNsUri = "http://www.sbml.org/sbml/level3/version1/layout/version1";
    String renderNsUri = "http://www.sbml.org/sbml/level3/version1/render/version1";

    LayoutModelPlugin plugin = (LayoutModelPlugin) doc.getModel().getPlugin("layout");

    // bail if we don't have layout
    if (plugin == null) return;

    // convert document
    ConversionProperties prop = new ConversionProperties(new SBMLNamespaces(3, 1));
    prop.addOption("strict", false);
    prop.addOption("setLevelAndVersion", true);
    prop.addOption("ignorePackages", true);

    if (doc.convert(prop) != libsbml.LIBSBML_OPERATION_SUCCESS)
    {
      System.err.println("Conversion failed!");
      doc.printErrors();
      
      throw new ConversionException("The LibSBML conversion failed, it detected '" + doc.getNumErrors(libsbmlConstants.LIBSBML_SEV_ERROR) + "' potential problems.");
    }

    // add new layout namespace and set required flag
    SBasePlugin docPlugin = doc.getPlugin("layout");

    // if we don't have layout there is nothing to do
    if (docPlugin == null) return;

    docPlugin.setElementNamespace(layoutNsUri);

    doc.getSBMLNamespaces().addPackageNamespace("layout", 1);
    doc.setPackageRequired("layout", false);

    // add enable render if needed
    SBasePlugin rdocPlugin = doc.getPlugin("render");

    if (rdocPlugin != null)
    {
      doc.getSBMLNamespaces().addPackageNamespace("render", 1);
    }
    else
    {
      doc.enablePackage(renderNsUri, "render", true);
    }
    doc.setPackageRequired("render", false);

  }
  
	public static void main(String[] args) throws ReadModelException, ConversionException, WriteModelException {
		
		GeneralConverter converter = new SBMLLayoutL2ToL3();
		GeneralModel inputModel = new SBMLModel();
		Map<String, String> options = new HashMap<String, String>();
		
		//inputModel.setModelFromFile("C:/Users/HY/Documents/SBML2SBGN/sbfc_new/examples/Complete_Example_level2.xml");
		SBMLReader libSBMLReader = new SBMLReader();
		SBMLDocument SBMLDocument = libSBMLReader.readSBMLFromFile("C:/Users/HY/Documents/SBML2SBGN/sbfc_new/examples/Complete_Example_level2.xml");
		String string = SBMLDocument.toSBML();
		//System.out.println("modelToString=\n"+string);
		
		inputModel.setModelFromString(string) ;
		
		options.put("sbml.target.level", "3");
		options.put("sbml.target.version", "1");
		converter.setOptions(options);
		
		//System.out.println("modelToString=\n"+inputModel.modelToString());
		
		GeneralModel covertedModel = converter.convert(inputModel);

		//System.out.println("modelToString=\n"+covertedModel.modelToString());
		
		if (covertedModel == null){
			throw new WriteModelException();
		}
		covertedModel.modelToFile("C:/Users/HY/Documents/SBML2SBGN/sbfc_new/examples/Complete_Example_level2_level3.xml");
	
	} 

}

  