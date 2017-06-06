/*
 PROCESSINGJS.COM HEADER ANIMATION
 MIT License - Hyper-Metrix.com/F1LT3R
 Native Processing compatible 
 */


// define a javascript function which will be used in processing
interface JavaScript
{
  public void drawMathML( String mathml );// should take the String reactionIdentifier
}

// definition of what makes a object draggable
interface Draggable 
{
  // tell if the mouse is over the object by looking at geometrical conditions
  // TODO : has to take the scale factor into consideration
  public boolean hasMouseOver(int mouseX, int mouseY);

  // authorize the object to be dragged
  public boolean canBeDragged();

  // modify object coordinates (enabling another drawing afterwards) taken from the SBGN-ML bounding box
  public void setCoordinates(float arg1, float arg2, float arg3, float arg4);

  // Take mouseX and mouseY and calculate appropriate coordinates for dragging
  public void draggingShape(float mouseX, float mouseY);
}

interface Portable
{
  // function which draws 2 ports either vertically or horizontally
  public void drawPorts();
}


interface Clonable
{
  // define and draw the marker on a clonable shape, the idea is to draw another shape in black color
  public void drawCloneMarker(float x, float y, float w, float h);
}

// a shape which supports a label
abstract class LabelShape extends Shape {

  public String[] labelTable;
  private float xUpperLab;
  private float yLowerLab;

  // constructor
  public LabelShape(String[] texts) {
    super(); 
    labelTable = texts;
  }

  // draw a label in the center of the given rectangle w/o caring about the length of the label.
  public void drawLabelOverShape(String[] label, float xUpperShape, float yUpperShape, float widthShape, float heightShape) {

    String labelString = "";
    // initialize variables
    float xc = 0;
    float yc = 0;

    for (int i=0; i<label.length; i++) {

      // build the label as a string
      labelString += label[i];

      // once the last line read...
      if ( i+1 == label.length ) {
        // calculate center coordinates x,y
        xc = xUpperShape + widthShape/2;
        // two slight modifications are needed to y-center correctly the labels, these modifiers may depend of the font
        yc = yUpperShape + heightShape/2 + ALIGN_Y_LABEL_MODIFIER - i*ALIGN_Y_LABEL_MULTIPLIER;
      }
    }

    // specify a font and its size
    textFont(LUCIDA, 12);
    textAlign(CENTER);
    // and print the label
    text(labelString, xc, yc);
  }

  // draw a clone marker, the function is written here because it appears that most labelable shapes can be clonable
  // given parameters corresponds to the shape
  void drawCloneMarker(float xUp, float yUp, float width, float height) {
    // run over the width
    for ( int i=int(xUp); i<int(xUp+width); i++ ) {
      // run over a percentage of y
      for ( int j=int( yUp+height*(1-CLONE_PERC) ); j<int( yUp+height ); j++ ) {
        // take the color of the pixel...
        color c = get( i, j );
        // ... and replace it if it is shape background color
        if ( c == EPN_BACK_COLOR || c == MULT_SHADOW_COLOR ) {
          set( i, j, CLONE_BACK_COLOR );
        }
      }
    }
  }
}



abstract class Shape implements Draggable {

  // ports length
  public int portLength = 3;

  // tell if a shape is drawn or not
  private boolean drawn;

  // an annotation for the shape
  private String annoShape = "";

  // use to tell is a shape can support mathML or not
  public boolean canSupportMathml = false;

  // a color object for the shape?

  public Shape() {

    // In a first place, a shape is not drawn
    drawn = false;
  }

  // draw the shape
  public void drawShape() {
  }

  // useful if we want to draw the shape again and again
  public void hasToBeDrawnAgain() {
    drawn = true;
  }

  // called when a shape has been drawn
  void hasBeenDrawn() {
    drawn = false;
  }

  // tell if the shape is drawn or not
  public boolean isDrawn() {
    return drawn;
  }

  // tell if the mouse is over the object by looking at geometrical conditions
  public boolean hasMouseOver(int mouseX, int mouseY) {
    return false;
  }

  // authorize the object to be dragged
  public boolean canBeDragged() {
    return false;
  }

  // modify object coordinates (enabling another drawing afterwards) taken from the SBGN-ML bounding box
  public void setCoordinates(float arg1, float arg2, float arg3, float arg4) {
    // do nothing here, has to be overloaded
  }

  // Take mouseX and mouseY and calculate appropriate coordinates for dragging
  public void draggingShape(float mouseX, float mouseY) {
    // do nothing here, has to be overloaded
  }

  // stroke function ?

  // change color function ?

  // getter for the annotation
  public String getAnnotation() {
    if ( annoShape == "" ) {
      return "No annotation available for this object";
    }
    return annoShape;
  }

  // setter for the annotation
  public void setAnnotation(String anno) {
    annoShape = anno;
  }
  
  // will have to be overloaded by funtions for mathML
  public String getMathml() { return null;};
}

