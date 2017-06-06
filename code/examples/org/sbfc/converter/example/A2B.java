package org.sbfc.converter.example;

import org.sbfc.converter.exceptions.ConversionException;
import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.GeneralConverter;
import org.sbfc.converter.models.AModel;
import org.sbfc.converter.models.BModel;
import org.sbfc.converter.models.GeneralModel;

/** Convert model format A to model format B. */
public class A2B extends GeneralConverter {

  /** Constructor A2B. */
  public A2B() { super(); }

  /** Convert model format A to model format B. */
  public BModel bExport(AModel aModel) 
  throws ReadModelException, ConversionException {
    String bStringModel = "I am a B model!";
    // extract species, reactions, parameters, compartment, rules, 
    // events, and functionDefinitions from aModel
    // and store the converted values in bStringModel 
      
    // if aModel cannot be parsed, throw ReadModelException
    // if aModel cannot be converted to bStringModel, throw ConversionException
    BModel bModel = new BModel();
    bModel.setModelFromString(bStringModel);
    return bModel;
  }

  @Override
  public GeneralModel convert(GeneralModel model) 
  throws ConversionException, ReadModelException {
    // assume model is of type AModel
    try { return bExport((AModel)model); }
    catch (ReadModelException e) { throw e; }
    catch (ConversionException e) { throw e; }
  }
  
  @Override
  public String getResultExtension() { return ".b"; }
  
  @Override
  public String getName() { return "A2B"; }
  
  @Override
  public String getDescription() { 
    return "It converts a model format from A to B"; 
  }

  @Override
  public String getHtmlDescription() { 
    return "It converts a model format from A to B"; 
  }
}
