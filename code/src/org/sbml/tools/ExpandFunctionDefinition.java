package org.sbml.tools;

import org.sbml.jsbml.ASTNode;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

/**
 * @author rodrigue
 *
 */
public class ExpandFunctionDefinition {

  /**
   * @param args
   */
  public static void main(String[] args) 
  {
    if (args.length == 0) {
      System.out.println("Need to pass one file to this script.");
      System.exit(1);
    }
    
    String fileName = args[0];
    
    System.load("sbmlj");
    
    SBMLReader reader = new SBMLReader();
    SBMLDocument d = reader.readSBMLFromFile(fileName);
    
    d.expandFunctionDefinitions();
    
    SBMLWriter writer = new SBMLWriter();
    
    writer.writeSBMLToFile(d, fileName);

  }

}
