package org.sbfc.converter.example;

import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.BModel;
import org.sbfc.converter.models.CModel;
import org.sbfc.converter.models.GeneralModel;

/** Convert model format B to model format C. */
public class B2C extends GeneralConverter {

  /** Constructor B2C. */
  public B2C() { super(); }

  /** Convert model format B to model format C. */
  public CModel cExport(BModel bModel) 
  throws ReadModelException, ConversionException {
    String cStringModel = "I am a C model!";
    // extract species, reactions, parameters, compartment, rules, 
    // events, and functionDefinitions from aModel
    // and store the converted values in cStringModel 
      
    // if aModel cannot be parsed, throw ReadModelException
    // if aModel cannot be converted to cStringModel, throw ConversionException
    CModel cModel = new CModel();
    cModel.setModelFromString(cStringModel);
    return cModel;
  }

  @Override
  public GeneralModel convert(GeneralModel model) 
  throws ConversionException, ReadModelException {
    // assume model is of type BModel
    try { return cExport((BModel)model); }
    catch (ReadModelException e) { throw e; }
    catch (ConversionException e) { throw e; }
  }
  
  @Override
  public String getResultExtension() { return ".c"; }
  
  @Override
  public String getName() { return "B2C"; }
  
  @Override
  public String getDescription() { 
    return "It converts a model format from B to C"; 
  }

  @Override
  public String getHtmlDescription() { 
    return "It converts a model format from B to C"; 
  }
}
