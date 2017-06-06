package org.sbfc.converter.example;

import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.AModel;
import org.sbfc.converter.models.CModel;
import org.sbfc.converter.models.GeneralModel;
import org.sbfc.converter.example.A2B;
import org.sbfc.converter.example.B2C;

/** Convert model format A to model format C re-using the converter 
 *  A2B and B2C. 
 */
public class A2C extends GeneralConverter {

  /** Constructor A2C. */
  public A2C() { super(); }

  /** Convert model format A to model format C. */
  public CModel cExport(AModel aModel) 
  throws ReadModelException, ConversionException {
    A2B a2b = new A2B();
    B2C b2c = new B2C();
    // concatenate the conversion as a transitive relationship
    return (CModel) b2c.convert(a2b.convert(aModel));
  }

  @Override
  public GeneralModel convert(GeneralModel model) 
  throws ConversionException, ReadModelException {
    // assume model is of type AModel
    try { return cExport((AModel)model); }
    catch (ReadModelException e) { throw e; }
    catch (ConversionException e) { throw e; }
  }
  
  @Override
  public String getResultExtension() { return ".c"; }
  
  @Override
  public String getName() { return "A2C"; }
  
  @Override
  public String getDescription() { 
    return "It converts a model format from A to C"; 
  }

  @Override
  public String getHtmlDescription() { 
    return "It converts a model format from A to C"; 
  }
}
