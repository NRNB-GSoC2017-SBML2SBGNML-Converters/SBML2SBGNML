package org.sbfc.converter.models;

import java.io.File;
import org.sbfc.converter.models.StringModel;

public class AModel extends StringModel {

  @Override
  public String[] getExtensions() { return new String[] { ".a" }; }

  @Override
  public boolean isCorrectType(File f) { return true; }

  @Override
  public String getURI() { return "text/a"; }
}
