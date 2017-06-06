/**
 * Interface defining the specifications that each Model must implement.
 */
public interface GeneralModel {
  /**
   * Set the Model from a file in the file system.
   * @param fileName path to the file containing the model
   * @throws ReadModelException
   */
  public void setModelFromFile(String fileName) throws ReadModelException;
  
  /**
   * Set the model from a String.
   * @param modelString Model
   * @throws ReadModelException
   */
  public void setModelFromString(String modelString) throws ReadModelException;
  
  /**
   * Write the Model into a new file.
   * @param fileName path at which the new file will be created
   * @throws WriteModelException
   */
  public void modelToFile(String fileName) throws WriteModelException;
  
  /**
   * Return the Model as a String.
   * @return Model
   * @throws WriteModelException
   */
  public String modelToString() throws WriteModelException;
  
  /**
   * Return an array of model file type extension (ex: [.xml, .sbml] for SBML, [.owl] for BIOPAX)
   * The first is the preferred extension.
   *
   * @return file type extensions
   */
  public String[] getExtensions();
  
  /**
   * This method is used to distinguish between converters with the same file extension.
   * For example, a file ending with .xml could be either an SBML model, or some other XML file type. 
   * <p>
   * Implementers should perform a quick heuristic, not a full validation. For example
   * XML file types may examine the root element to determine if it has the correct name or namespace.
   * <p>
   * Implementers should <b>only</b> return false if they are <b>certain</b> that the file
   * type is wrong. If the correctness could not be determined for sure, the method should 
   * always return true. 
   * @return false if the file is not the correct type to be used with this GeneralModel
   */
  public boolean isCorrectType(File f);

  /**
   * Return a URI for the model
   * e.g. MIME types: image/png, application/matlab, text/xpp
   * e.g. COMBINE spec ids: http://identifiers.org/combine.specifications/sbml
   * 
   * @return the model URI
   */
  public String getURI();
}
