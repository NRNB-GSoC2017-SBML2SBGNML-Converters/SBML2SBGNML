package org.sbfc.converter.models;

import java.io.File;
import org.sbfc.converter.models.StringModel;

public class CModel extends StringModel {

  @Override
  public String[] getExtensions() { return new String[] { ".c" }; }

  @Override
  public boolean isCorrectType(File f) { return true; }

  @Override
  public String getURI() { return "text/c"; }
}
