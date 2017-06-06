package org.sbfc.converter.models;

import java.io.File;
import org.sbfc.converter.models.StringModel;

public class BModel extends StringModel {

  @Override
  public String[] getExtensions() { return new String[] { ".b" }; }

  @Override
  public boolean isCorrectType(File f) { return true; }

  @Override
  public String getURI() { return "text/b"; }
}
