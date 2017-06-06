/** 
 * Abstract class defining the specifications that each Converter must implement. 
 */
public abstract class GeneralConverter {
  /**
   * The input model to be converted.
   */
  protected GeneralModel inputModel = null;  

  /**
   * The options for the converter. Each option is defined as a pair (name, value). 
   * For instance, for the converter SBML2SBML, one option is ("sbml.target.level", "3").
   */
  protected Map<String, String> options;
  
  /**
   * Method to convert a GeneralModel into another.
   * @param model
   * @return GeneralModel
   */
  public abstract GeneralModel convert(GeneralModel model) throws ConversionException, ReadModelException;
  
  /**
   * Return the extension of the Result file.
   * @return String
   */
  public abstract String getResultExtension();
  
  /**
   * Set the converter options.
   * @param options
   */
  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  /** 
   * Return the input model.
   * @return the input model
   */
  public GeneralModel getInputModel() {
    return inputModel;
  }
  
  /**
   * Return the converter name as it should be displayed.
   * @return the name
   */
  public abstract String getName();
  
  /**
   * Return the converter description.
   * @return the description
   */
  public abstract String getDescription();

  /**
   * Return the converter description in HTML format.
   * @return the HTML description
   */
  public abstract String getHtmlDescription();
} 

