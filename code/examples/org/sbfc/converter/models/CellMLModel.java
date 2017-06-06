package org.sbfc.converter.models;

import java.io.File;

/**
 * A {@link StringModel} representing a CellML model (http://www.cellml.org).
 * 
 * @author rodrigue
 *
 */
public class CellMLModel extends StringModel {

  @Override
  public String[] getExtensions() {
    return new String[] { ".cellml", ".xml" };
  }

  @Override
  public boolean isCorrectType(File f) {
    return true;
  }

  @Override
  public String getURI() {
    return "http://identifiers.org/combine.specifications/cellml";
  }

}
