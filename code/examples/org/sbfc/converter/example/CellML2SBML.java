package org.sbfc.converter.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.models.SBMLModel;

/** 
 * This converter will use an external program to
 * do the conversion. As an example, we will convert from CellML to SBML using
 * antimony (http://antimony.sourceforge.net/).
 * 
 * <p>By using this template, you can easily add a new converter that
 * call an external program.
 */
public class CellML2SBML extends GeneralConverter {

  /**
   * Path to the antimony sbtranslate binary.
   */
  public final static String PROGRAM = "/path/to/antimony/bin/sbtranslate";
  
  /** Creates a new instance. */
  public CellML2SBML() { super(); }

  @Override
  public GeneralModel convert(GeneralModel model) 
  throws ConversionException, ReadModelException 
  {
    try {
      //
      // 1. Dealing with the input format
      //
      // We first need to save the model into a temporary file so that the
      // external program can read it.
      // GeneralModel does not store the original file location and it can
      // also be initialize directly from String.
      
      // We could check here that the input format correspond to the CellML URI.
      
      // creating the temporary file
      File inputFile = File.createTempFile("cellml-", ".xml");

      // writing the model to the temporary file
      model.modelToFile(inputFile.getAbsolutePath()); 

      //
      // 2. Running the external program
      //
      
      // creating a second temporary file for the output
      File outputFile = File.createTempFile("sbml-", ".xml");
      
      // using the Runtime exec method to run the external program:
      Process p = Runtime.getRuntime().exec(PROGRAM + " -o sbml-comp -outfile " 
          + outputFile.getAbsolutePath() + " " + inputFile.getAbsolutePath());
      // waiting for the program to finish.
      p.waitFor();
      
      // read the output messages from the command
      BufferedReader stdInput = new BufferedReader(new
          InputStreamReader(p.getInputStream()));
      String line;

      while ((line = stdInput.readLine()) != null) {
        // You might want to read the process output to check that the conversion went fine
        // and if not, you can throw an exception with an appropriate error message.
        System.out.println("Output: " + line);
      }

      // read the error messages from the command
      BufferedReader stdError = new BufferedReader(new
          InputStreamReader(p.getErrorStream()));

      while ((line = stdError.readLine()) != null) {
        // You might want to read the process error output to check that the conversion went fine
        // and if not, you can throw an exception with an appropriate error message.
        System.out.println("Errors: " + line);
        if (line.startsWith("Segmentation fault")) {
          throw new ConversionException("Encountered a Segmentation fault while running antomony !!");
        }
      }
      
      //
      // 3. Returning the output format
      //
      
      // creating a new empty SBMLModel
      GeneralModel outputModel = new SBMLModel();
      
      // reading the output file into the SBMLModel
      outputModel.setModelFromFile(outputFile.getAbsolutePath());
      
      return outputModel;
    }
    catch (IOException e) {
      throw new ReadModelException(e);
    } catch (WriteModelException e) {
      throw new ReadModelException(e);
    } catch (InterruptedException e) {
      throw new ReadModelException(e);
    } 
  }
  
  @Override
  public String getResultExtension() { return ".xml"; }
  
  @Override
  public String getName() { return "CellML2SBML"; }
  
  @Override
  public String getDescription() { 
	    return "Convert a CellML model to SBML"; 
  }

  @Override
  public String getHtmlDescription() { 
    return "Convert a CellML model to SBML"; 
  }
}
