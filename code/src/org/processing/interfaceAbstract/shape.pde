/*
  PROCESSINGJS.COM HEADER ANIMATION
 MIT License - Hyper-Metrix.com/F1LT3R
 Native Processing compatible 
 */


// Rectangle included basic rectangular shape but also with rounded (macromolecule and nucleic acid feature) or chopped corners (complex).
class Rectangle extends LabelShape implements Draggable, Clonable {

  // coordinates of the left-upper point, width and height
  float x, y, w, h;
  // number of round corners: 0, 2 or 4; 5 is the option for complex
  int c;
  // width of the corner deformation
  float arcRadius = 9.0;
  // tell if the shape is a multimer or not. 0 is default, no multimer. 1 is a multimer.
  int mult = 0;
  // s is the switch value for the multimer shade
  int s = 3;
  // tell the number of ports for the shapes
  //  int p = 0;
  // tell if is a clone or not
  int isClone = 0;

  // original constructor
  Rectangle (int roundCornerNumber, int multimer, int portsNumber, String[] labelTable, float xUpper, float yUpper, float width, float height) {
    super(labelTable);
    // upper left corner coordinates
    x = xUpper;
    y = yUpper;
    w = width;
    h = height;
    // if c=2 we get a nucleic acid feature shape; if c=4 a macromolecule shape; a mere rectangle otherwise.
    if ( roundCornerNumber == 2 || roundCornerNumber == 4 || roundCornerNumber == 5 ) {
      c = roundCornerNumber;
    }
    else {
      c = 0;
    }
    // a rectangle cannot be a multimer in SBGN
    if ( c != 0 ) {
      mult = multimer;
    }
    // 2 is the value for process nodes ports, maybe it will be possible to have port elsewhere
    //    if (portsNumber == 2) {
    //      p = portsNumber;
    //    }
  }

  // constructor with annotations and cloning
  Rectangle (int roundCornerNumber, int multimer, int portsNumber, int clone, String[] labelTable, float xUpper, float yUpper, float width1, float height1, String anno) {
    this(roundCornerNumber, multimer, portsNumber, labelTable, xUpper, yUpper, width1, height1);
    isClone = clone;
    this.setAnnotation(anno);
  }

  void drawShape() {

    if (mult == 1) {
      // make switch re-effective
      s = CLONE_SWITCH;
      // shape stroke color
      stroke(EPN_STROKE_COLOR);
      // shade coloring			
      fill(MULT_SHADOW_COLOR);

      switchShape();
    }

    // setting the shape stroke color
    stroke(EPN_STROKE_COLOR);

    // including coloring in the draw function
    fill(EPN_BACK_COLOR);

    // make switch ineffective
    s = 0;
    // draw a normal shape
    switchShape();

    // add a clone marker if it a clone
    if (isClone==1) {
      drawCloneMarker( x, y, w, h );
    }

    // add a clone marker for multimer if shadow and shape have the same background color
    if (isClone==1 && mult==1) {
      drawCloneMarker( x+CLONE_SWITCH, y+CLONE_SWITCH, w, h );
    }

    // draw the label of the shape (has to be label (private))
    fill(EPN_LAB_FONT_COLOR);
    drawLabelOverShape(labelTable, x, y, w, h);
  }

  void switchShape() {
    x = x + s;
    y = y + s;
    switch(c) {
    case 2 :
      beginShape();
      vertex( x, y );
      vertex( x+w, y );
      vertex( x+w, y+h-arcRadius );
      bezierVertex( x+w, y+h, x+w-arcRadius, y+h, x+w-arcRadius, y+h );
      vertex( x+arcRadius, y+h );
      bezierVertex( x, y+h, x, y+h-arcRadius, x, y+h-arcRadius );
      vertex( x, y );
      endShape();
      break;
    case 4 :
      beginShape();
      vertex( x+arcRadius, y );
      vertex( x+w-arcRadius, y );
      bezierVertex( x+w, y, x+w, y+arcRadius, x+w, y+arcRadius );
      vertex( x+w, y+h-arcRadius );
      bezierVertex( x+w, y+h, x+w-arcRadius, y+h, x+w-arcRadius, y+h );
      vertex( x+arcRadius, y+h );
      bezierVertex( x, y+h, x, y+h-arcRadius, x, y+h-arcRadius );
      vertex( x, y+arcRadius );
      bezierVertex( x, y, x+arcRadius, y, x+arcRadius, y );
      endShape();
      break;
    case 5 :
      beginShape();
      vertex( x+arcRadius, y );
      vertex( x+w-arcRadius, y );
      vertex( x+w, y+arcRadius );
      vertex( x+w, y+h-arcRadius );
      vertex( x+w-arcRadius, y+h );
      vertex( x+arcRadius, y+h );
      vertex( x, y+h-arcRadius );
      vertex( x, y+arcRadius );
      vertex( x+arcRadius, y );
      endShape();
      break;
    default :
      rect( x, y, w, h );
      break;
    }
    x = x - s;
    y = y - s;
  }


  boolean hasMouseOver(int mouseX, int mouseY) {
    // looking at the surface of the rectangle
    if ( (mouseX-x) < w && (mouseX-x) > 0 && (mouseY-y) > 0 && (mouseY-y) < h ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // yes, a rectangle can generally be dragged (for the moment...)
    return true;
  }

  // gather a collection of setters for the Rectangle object
  void setCoordinates(float argX, float argY, float argW, float argH) {
    x = argX;
    y = argY;
    w = argW;
    h = argH;
  }

  void draggingShape(float mouseX, float mouseY) {
    // the mouse has to point the center of the rectangle
    x = mouseX-w/2;
    y = mouseY-h/2;
  }
}


// class designed for units of information which are rectangles with specific colors
class UnitOfInfo extends Rectangle {

  UnitOfInfo( String[] labelTable, float xUpper, float yUpper, float width1, float height1, String anno ) {
    super( 0, 0, 0, 0, labelTable, xUpper, yUpper, width1, height1, anno );
  }

  void drawShape() {

    // rectangular shape
    fill(AUXI_BACK_COLOR);
    stroke(AUXI_STROKE_COLOR);
    rect( x, y, w, h );

    // draw the label of the shape (has to be label (private))
    fill(AUXI_LAB_FONT_COLOR);
    drawLabelOverShape(labelTable, x, y, w, h);
  }
}


class Compartment extends Rectangle {

  // left-upper coordinates for label and width, height of its bounding box
  float xLab;
  float yLab;
  float wLab;
  float hLab;

  // a compartment with an annotation, a label and its bounding box
  Compartment (String[] labelTable, float xUpperLabel, float yUpperLabel, float widthBbox, float heightBbox, float xUpper, float yUpper, float width, float height, String anno) {
    this(labelTable, xUpperLabel, yUpperLabel, widthBbox, heightBbox, xUpper, yUpper, width, height);
    this.setAnnotation(anno);
  }

  // a rectangle with a thick border
  Compartment (String[] labelTable, float xUpperLabel, float yUpperLabel, float widthBbox, float heightBbox, float xUpper, float yUpper, float width, float height) {
    // a rectangle with 4 rounded corners, no multimer, no ports
    super(4, 0, 0, labelTable, xUpper, yUpper, width, height);

    xLab = xUpperLabel;
    yLab = yUpperLabel;
    wLab = widthBbox;
    hLab = heightBbox;
  }

  void drawShape() {

    // color of the compartement background
    fill(COMP_BACK_COLOR);

    // make switch ineffective
    s = 0;

    // increase the stroke weight for this compartment
    strokeWeight(COMP_STROKE_THICKNESS);

    // color of the stroke
    stroke(COMP_STROKE_COLOR);

    // use bezier curves to draw the compartment shape
    // height percentage for the compartment curves
    float hpcc = 0.05;
    beginShape();
    vertex( x, y+h*hpcc );
    bezierVertex( x, y+h*hpcc, x+w*0.10, y, x+w*0.20, y );
    vertex( x+w*0.80, y );
    bezierVertex( x+w*0.80, y, x+w*0.90, y, x+w, y+h*hpcc );
    vertex( x+w, y+h*(1-hpcc) );
    bezierVertex( x+w, y+h*(1-0.05), x+w*0.90, y+h, x+w*0.80, y+h );
    vertex( x+w*0.20, y+h );
    bezierVertex( x+w*0.20, y+h, x+w*0.10, y+h, x, y+h*(1-hpcc) );
    vertex( x, y+h*hpcc );
    endShape();

    // set back the stroke value
    strokeWeight(GLOBAL_STROKE_THICKNESS);

    // draw the label of the shape (has to be label (private))
    fill(COMP_LAB_FONT_COLOR);
    drawLabelOverShape( labelTable, xLab, yLab, wLab, hLab );
  }
}


// this extends is only for convience but a logical operator is not an EPN.
class LogicalOperator extends SimpleChemical implements Portable {

  // 2 ports coordinates
  float xp1, yp1, xp2, yp2;

  // a constructor with an annotation
  LogicalOperator(String[] labelTable, float xUpper, float yUpper, float radius, float useLess, float xPort1, float yPort1, float xPort2, float yPort2, String anno) {
    this(labelTable, xUpper, yUpper, radius, useLess, xPort1, yPort1, xPort2, yPort2);
    this.setAnnotation(anno);
  }

  LogicalOperator(String[] labelTable, float xUpper, float yUpper, float radius, float useLess, float xPort1, float yPort1, float xPort2, float yPort2) {
    // 0: no multimer, the label given will be either AND, OR, NOT.
    super(0, labelTable, xUpper, yUpper, radius, useLess);
    xp1 = xPort1;
    yp1 = yPort1;
    xp2 = xPort2;
    yp2 = yPort2;
  }

  public void drawShape() {
    // coloring the circle and its stroke
    fill( LOGICAL_BACK_COLOR );
    stroke( LOGICAL_STROKE_COLOR );
    ellipse ( x, y, w, w );

    fill( LOGICAL_LAB_FONT_COLOR );
    drawLabelOverShape(labelTable, x-w/2, y-w/2, w, w);

    drawPorts();
  }

  void drawPorts() {
    // draw ports: for the moment only vertical and horizontal ports can be drawn
    // check if ports are displayed vertically
    if ( xp1==xp2 ) {
      // Admit that the first port (.1) is up
      line( xp1, yp1, x, y-w/2 );
      // Admit that the second port (.2) is down
      line( xp2, yp2, x, y+w/2 );
    } 
    else {
      // these lines are only for horizontal ports
      // Admit that the first port (.1) is on the left side
      line( xp1, yp1, x-w/2, y );
      // Admit that the second port (.2) is on the right side
      line( xp2, yp2, x+w/2, y );
    }
  }
}


class SimpleChemical extends LabelShape implements Draggable, Clonable {

  // center coordinate and width aka diameter of the circle
  float x, y, w;
  // equals 0 (default) if is not a multimer, 1 otherwise
  int multimer = 0;
  // tell if the species is a clone
  int isClone = 0;

  // constructor
  SimpleChemical ( int multimerizable, String[] texts, float xUpper, float yUpper, float diam, float useLess ) {
    super(texts);
    // center coordinates and width
    x = xUpper + diam/2;
    y = yUpper + diam/2;
    w = diam;
    multimer = multimerizable;
  }

  // constructor with an annotation and clone
  SimpleChemical (int multimerizable, int clone, String[] texts, float xUpper, float yUpper, float radius, float useLess, String anno) {
    this(multimerizable, texts, xUpper, yUpper, radius, useLess);
    this.setAnnotation(anno);
    isClone = clone;
  }

  void drawShape() {

    // Draw a shade second shape first
    if (multimer == 1) {
      fill( MULT_SHADOW_COLOR );
      stroke(EPN_STROKE_COLOR);
      ellipse ( x+CLONE_SWITCH, y+CLONE_SWITCH, w, w );
    }


    // coloring the circle
    fill( EPN_BACK_COLOR );
    stroke(EPN_STROKE_COLOR);

    ellipse ( x, y, w, w );

    // draw a clone marker on the normal shape
    if ( isClone == 1 ) {
      drawCloneMarker();
    }

    // draw a clone marker on the shade shape
    if ( multimer == 1 && isClone == 1 ) {
      drawCloneMarkerShadow();
      println("drawCloneMarkerShadow");
    }

    fill( EPN_LAB_FONT_COLOR );
    drawLabelOverShape(labelTable, x-w/2, y-w/2, w, w);
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // looking at the surface of the circle
    if ( (dist(x, y, mouseX, mouseY) < w) ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // a circle can be dragged
    return true;
  }

  void setCoordinates(float xCenter, float yCenter, float diam, float noUse) {
    // set x, y coordinates of the center
    x = xCenter;
    y = yCenter;
    // set circle radius
    w = diam;
  }

  void draggingShape(float mouseX, float mouseY) {
    // the mouse has to point the center of the circle
    x = mouseX;
    y = mouseY;
  }

  // call the function drawCloneMarker(,,,) defined in the LabelShape class
  void drawCloneMarker() {

    float wr = sqrt( sq(w/2) - sq( w*( 1-CLONE_PERC-0.5 ) ) );

    float xUpper = x - wr;
    float yUpper = y - w/2;

    drawCloneMarker( xUpper, yUpper, wr*2, w );
  }

  // the same method as drawCloneMarker but designed for clone shadow
  void drawCloneMarkerShadow() {

    // reduced width, the cloning percentage should not exceed 50% of the area
    float wr = sqrt( sq(w/2) - sq( w*( 1-CLONE_PERC-0.5 ) ) );

    float xUpperClone = x - wr + CLONE_SWITCH;
    float yUpperClone = y - w/2 + CLONE_SWITCH;

    drawCloneMarker( xUpperClone, yUpperClone, wr*2, w );
  }
}


class Sink extends Shape implements Draggable {

  // coordinates of the center of the circle, and width aka circle diameter
  float x, y, w;

  // constructor with an annotation
  Sink(float xUpper, float yUpper, float diam, float useLess, String anno) {
    this(xUpper, yUpper, diam);
    this.setAnnotation(anno);
  }

  // constructor
  Sink(float xUpper, float yUpper, float diam) {
    super();
    // set center coordinates and width
    x = xUpper+diam/2;
    y = yUpper+diam/2;
    w = diam;
  }

  void drawShape() {

    // coloring the circle
    fill( EPN_BACK_COLOR );
    stroke( EPN_STROKE_COLOR );

    ellipse ( x, y, w, w );
    line ( x+w/2, y-w/2, x-w/2, y+w/2 );
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // looking at the surface of the circle
    if ( (dist(x, y, mouseX, mouseY) < w) ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // a circle can be dragged
    return true;
  }

  void setCoordinates(float xCenter, float yCenter, float radius, float noUse) {
    // set x, y coordinates of the center
    x = xCenter;
    y = yCenter;
    // set circle width
    w = radius*2;
  }

  void draggingShape(float mouseX, float mouseY) {
    // the mouse has to point the center of the circle
    x = mouseX;
    y = mouseY;
  }
}


class UnspecifiedEntity extends LabelShape implements Draggable, Clonable {

  // coordinates of the center of the ellipse, then width and height
  float x, y, r1, r2;
  // tell if the species is a clone or not
  int isClone = 0;

  // constructor with an annotation and a possibility of clone marker
  UnspecifiedEntity(int clone, String[] texts, float xUpper, float yUpper, float radX, float radY, String anno) {
    this(texts, xUpper, yUpper, radX, radY);
    this.setAnnotation(anno);
    isClone = clone;
  }

  UnspecifiedEntity (String[] texts, float xUpper, float yUpper, float radX, float radY) {
    super(texts);
    // center coordinates and radiuses
    x = xUpper+radX/2;
    y = yUpper+radY/2;
    r1 = radX;
    r2 = radY;
  }

  void drawShape() {

    // coloring the circle
    fill( EPN_BACK_COLOR );

    ellipse ( x, y, r1, r2 );


    if ( isClone == 1 ) {
      drawCloneMarker();
    }

    // color of the label text
    fill( EPN_LAB_FONT_COLOR );

    // the function takes coordinates of the upper-left point of the bounding box
    drawLabelOverShape(labelTable, x-r1/2, y-r2/2, r1, r2);

    // Drawing 2 ports
    line( x-r1/2-3, y, x-r1/2, y );
    line( x+r1/2, y, x+r1/2+3, y );
  }

  boolean canBeDragged() {
    // a circle can be dragged
    return true;
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // looking at the surface of a circle having r1 as radius
    if ( (dist(x, y, mouseX, mouseY) < r2)) {
      return true;
    }
    return false;
  }

  void setCoordinates(float xCenter, float yCenter, float radius1, float radius2) {
    // set x, y coordinates of the center and radiuses
    x = xCenter;
    y = yCenter;
    r1 = radius1;
    r2 = radius2;
  }

  void draggingShape(float mouseX, float mouseY) {
    // the mouse has to point the center of the circle
    x = mouseX;
    y = mouseY;
  }

  void drawCloneMarker() {

    // change coordinates
    float xUp = x - r1/2;
    float yUp = y - r2/2;

    drawCloneMarker( xUp, yUp, r1, r2 );
  }
}


class StateVariable extends UnspecifiedEntity {

  StateVariable( String[] texts, float xUpper, float yUpper, float diaX, float diaY, String anno ) {
    // text for state variable do not support several lines
    super( 0, texts, xUpper, yUpper, diaX, diaY, anno );
  } 

  void drawShape() {

    // draw an elliptical shape
    fill(AUXI_BACK_COLOR);
    stroke(AUXI_STROKE_COLOR);
    ellipse( x, y, r1, r2 );

    // add the content of the label
    fill(AUXI_LAB_FONT_COLOR);
    drawLabelOverShape( labelTable, x-r1/2, y-r2/2, r1, r2 );
  }
}


class Tag extends LabelShape implements Draggable {

  // values for rescaling the shape; x, y are for the upper-left corner
  float x, y, w, h;
  // direction can take values of 0 or 1. If 0 the tag will be oriented right, if 1 left.
  int direction = 0;
  // length of the arrow of the tag
  int depth = 25;
  // tell if ports are needed for the tag (e.g. if it is a terminal there is no need to have ports)
  private int needPort = 0;

  // constructor with an annotation
  Tag (int port, String[] texts, float upperX, float upperY, float width, float height, int dir, String anno) {
    this(port, texts, upperX, upperY, width, height, dir);
    this.setAnnotation(anno);
  }

  // constructor
  Tag (int port, String[] texts, float upperX, float upperY, float width, float height, int dir) {
    super(texts);
    x = upperX;
    y = upperY;
    w = width;
    h = height;
    if (dir == 1 || dir == 0) {
      direction = dir;
    }
    needPort = port;
  }

  void drawShape() {

    // Set the shape color (default white otherwise)
    fill( TAG_BACK_COLOR );

    beginShape();
    // tag pointing the left direction
    if (direction == 1) {
      vertex( x, y+h/2 );
      vertex( x+depth, y );
      vertex( x+w, y );
      vertex( x+w, y+h );
      vertex( x+depth, y+h );
      vertex( x, y+h/2 );

      // otherwise the right direction by default
    } 
    else {
      vertex( x, y );
      vertex( x+w-depth, y );
      vertex( x+w, y+h/2 );
      vertex( x+w-depth, y+h );
      vertex( x, y+h );
      vertex( x, y );
    }
    endShape();

    if (needPort == 1) {
      if (direction == 1) {
        drawLeftPort( x, y, w, h );
      } 
      else {
        drawRightPort( x, y, w, h );
      }
    }

    fill( REF_LAB_FONT_COLOR );
    drawLabelOverShape( labelTable, x, y, w, h );
  }

  // this port is drawn only if the shape is right oriented
  void drawRightPort(float x, float y, float w, float h) {

    line( x+w, y+h/2, x+w+portLength, y+h/2 );
  }

  // this port is drawn only if the shape is left oriented
  void drawLeftPort(float x, float y, float w, float h) {

    line( x, y+h/2, x-portLength, y+h/2 );
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // the surface is defined as the inner rectangle
    if ( (mouseX-x) < w && (mouseX-x) > 0 && (mouseY-y) > 0 && (mouseY-y) < h ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // yes, a tag can be dragged
    return true;
  }

  void setCoordinates(float upperX, float upperY, float width, float height) {
    x = upperX;
    y = upperY;
    w = width;
    h = height;
  }

  void draggingShape(float mouseX, float mouseY) {
    // like the rectangular shape
    x = mouseX-w/2;
    y = mouseY-h/2;
  }
}


class Phenotype extends PerturbingAgent {

  // an constructor with an annotation
  Phenotype(String[] texts, float upperX, float upperY, float width, float height, String anno) {
    this(texts, upperX, upperY, width, height);
    this.setAnnotation(anno);
  }

  Phenotype(String[] texts, float upperX, float upperY, float width, float height) {
    super( texts, upperX, upperY, width, height );
  }

  void drawShape() {

    // Set the shape color (default white otherwise)
    fill( PROC_BACK_COLOR );

    // use vertexes
    beginShape();
    vertex( x+depth, y );
    vertex( x+w-depth, y );
    vertex( x+w, y+h/2 );
    vertex( x+w-depth, y+h );
    vertex( x+depth, y+h );
    vertex( x, y+h/2 );
    vertex( x+depth, y );
    endShape();

    fill( PROC_LAB_FONT_COLOR );
    drawLabelOverShape( labelTable, x, y, w, h );
  }
}


class PerturbingAgent extends LabelShape implements Draggable, Clonable {

  // values for rescaling the shape; x, y are for the upper-left corner
  float x, y, w, h;
  // define the recess degree in the shape
  float depth = 25.0;
  // tell if it is a clone
  int isClone = 0;

  // constructor with an annotation and cloning
  PerturbingAgent(int clone, String[] texts, float upperX, float upperY, float width, float height, String anno) {
    this(texts, upperX, upperY, width, height);
    this.setAnnotation(anno);
    isClone = clone;
  }

  // constructor
  PerturbingAgent(String[] texts, float upperX, float upperY, float width, float height) {
    super(texts);
    x = upperX;
    y = upperY;
    w = width;
    h = height;
  }

  void drawShape() {

    // Set the shape color (default white otherwise)
    fill( EPN_BACK_COLOR );

    // use vertexes
    beginShape();
    // the drawing begins by default in the upper left corner
    vertex(x, y);
    // x+25 : cannot be a percentage of the width
    vertex(x+25, y+h/2);
    // bottom-left corner
    vertex(x, y+h);
    // bottom-right corner
    vertex(x+w, y+h);
    // x+w-25 : cannot be a percentage of the width
    vertex(x+w-25, y+h/2);
    // upper-right corner
    vertex(x+w, y);
    // we repeat the first corner to have the stroke drawn
    vertex(x, y);
    endShape();

    if ( isClone == 1 ) {
      drawCloneMarker( x, y, w, h );
    }

    // draw the label
    fill( EPN_LAB_FONT_COLOR );
    drawLabelOverShape( labelTable, x, y, w, h );
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // the surface is defined as rectangular
    if ( (mouseX-x) < w && (mouseX-x) > 0 && (mouseY-y) > 0 && (mouseY-y) < h ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // yes, a perturbing agent can be dragged
    return true;
  }

  void setCoordinates(float upperX, float upperY, float width, float height) {
    x = upperX;
    y = upperY;
    w = width;
    h = height;
  }

  void draggingShape(float mouseX, float mouseY) {
    // like the rectangular shape
    x = mouseX-w/2;
    y = mouseY-h/2;
  }
}

class SquareNode extends Shape implements Draggable, Portable {

  // type of the process; 0 is process; 1 is omitted process; 2 is uncertain process
  int type = 0;
  // x, y are for the upper-left corner
  float x, y, l;
  // coordinates of points designed for both ports of the process
  float xp1, yp1, xp2, yp2;
  // the content mathML related to the squareNode aka process node and its kinetic law.
  private String mathml = "";

  // constructor with annotation and content mathML
  SquareNode( int processType, float xUpper, float yUpper, float lengthSide, float useLess, float xPort1, float yPort1, float xPort2, float yPort2, String mathML, String anno ) {
    this( processType, xUpper, yUpper, lengthSide, useLess, xPort1, yPort1, xPort2, yPort2, anno );
    mathml = mathML;
  }

  // constructor with annotation
  SquareNode( int processType, float xUpper, float yUpper, float lengthSide, float useLess, float xPort1, float yPort1, float xPort2, float yPort2, String anno ) {
    this( processType, xUpper, yUpper, lengthSide, useLess, xPort1, yPort1, xPort2, yPort2 );
    this.setAnnotation(anno);
  }

  // constructor
  SquareNode ( int processType, float xUpper, float yUpper, float lengthSide, float useLess, float xPort1, float yPort1, float xPort2, float yPort2 ) {
    x = xUpper;
    y = yUpper;
    l = lengthSide;
    if ( processType < 3 ) {
      type = processType;
    }
    xp1 = xPort1;
    yp1 = yPort1;
    xp2 = xPort2;
    yp2 = yPort2;
    // this type of shape can support mathml
    canSupportMathml = true;
  }

  void drawShape() {

    // Set the shape color (default white otherwise)
    fill( PROC_BACK_COLOR );
    stroke( PROC_STROKE_COLOR );

    rect( x, y, l, l );

    // omitted process shape
    if ( type == 1 ) {
      line( x+l/4, y+l/4, x+l/2, y+3*l/4 );
      line( x+l/2, y+l/4, x+3*l/4, y+3*l/4 );
    } 
    else {
      if ( type == 2 ) {
        fill( color(0, 0, 0) );
        // constants -2 and +5 will have to be set globally
        text( "?", x+l/2-2, y+l/2+5 );
      }
    }

    // draw 2 ports
    drawPorts();
  }

  public void drawPorts() {
    // draw ports: for the moment only vertical and horizontal ports can be drawn
    // check if ports are displayed vertically
    if ( xp1==xp2 ) {
      // Admit that the first port (.1) is up
      line( xp1, yp1, x+l/2, y );
      // Admit that the second port (.2) is down
      line( xp2, yp2, x+l/2, y+l );
    } 
    else {
      // these two lines are only for horizontal ports
      // Admit that the first port (.1) is on the left side
      line( xp1, yp1, x, y+l/2 );
      // Admit that the second port (.2) is on the right side
      line( xp2, yp2, x+l, y+l/2 );
    }
  }

  boolean hasMouseOver(int mouseX, int mouseY) {
    // the surface is defined as a square
    if ( (mouseX-x) < l && (mouseX-x) > 0 && (mouseY-y) > 0 && (mouseY-y) < l ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // yes, processes can be dragged
    return true;
  }

  void setCoordinates(float upperX, float upperY, float length, float useLess) {
    x = upperX;
    y = upperY;
    l = length;
  }

  void draggingShape(float mouseX, float mouseY) {
    // like rectangular shape
    x = mouseX-l/2;
    y = mouseY-l/2;
  }

  // a getter for the mathml attribute
  public String getMathml() {
    // return a message in case of missing mathML
    if (mathml == "") {
      return ("No mathML available");
    }
    return mathml;
  }
}


class CircularNode extends Shape implements Draggable, Portable {

  // type of the process; 0 is association; 1 is dissociation
  int type = 0;
  // x, y are for the center, r is the radius of the circle
  float x, y, r;
  // coordinates of points designed for both ports of the process
  float xp1, yp1, xp2, yp2;
  // the content mathML
  private String mathml = "";

  // constructor with an annotation and content mathML
  CircularNode(int processType, float xUpper, float yUpper, float radiusCircle, float useLess, float xPort1, float yPort1, float xPort2, float yPort2, String mathML, String anno) {
    this( processType, xUpper, yUpper, radiusCircle, useLess, xPort1, yPort1, xPort2, yPort2, anno );
    mathml = mathML;
  }

  // constructor with an annotation
  CircularNode(int processType, float xUpper, float yUpper, float radiusCircle, float useLess, float xPort1, float yPort1, float xPort2, float yPort2, String anno) {
    this( processType, xUpper, yUpper, radiusCircle, useLess, xPort1, yPort1, xPort2, yPort2 );
    this.setAnnotation(anno);
  }

  // constructor
  CircularNode( int processType, float xUpper, float yUpper, float radiusCircle, float useLess, float xPort1, float yPort1, float xPort2, float yPort2 ) {
    r = radiusCircle/2;
    x = xUpper+r;
    y = yUpper+r;
    if ( processType < 2 ) {
      type = processType;
    }
    xp1 = xPort1;
    yp1 = yPort1;
    xp2 = xPort2;
    yp2 = yPort2;
    // this type of shape can support mathML
    canSupportMathml = true;
  }

  void drawShape() {

    stroke(ARC_STROKE_COLOR);
    // association shape
    if ( type == 0 ) {
      // set black color
      fill(color(0, 0, 0));
      // and fill a circle with it
      ellipse( x, y, r*2, r*2 );
    } 
    else {
      // by default we always have an association, here is the shape of the dissociation
      fill(color(255, 255, 255));
      ellipse( x, y, r*2, r*2 );
      // draw the second circle inside the first one, the value of 4 has to be set globally
      ellipse( x, y, r*2-r, r*2-r );
    }

    // draw 2 ports
    drawPorts();
  }

  public void drawPorts() {

    // draw ports: for the moment only vertical and horizontal ports can be drawn
    // check if ports are displayed vertically
    if ( xp1==xp2 ) {
      // Admit that the first port (.1) is up
      line( xp1, yp1, x, y-r );
      // Admit that the second port (.2) is down
      line( xp2, yp2, x, y+r );
    } 
    else {
      // these lines are only for horizontal ports
      // Admit that the first port (.1) is on the left side
      line( xp1, yp1, x-r, y );
      // Admit that the second port (.2) is on the right side
      line( xp2, yp2, x+r, y );
    }
  }


  boolean hasMouseOver(int mouseX, int mouseY) {
    // the surface is defined as a circle
    if ( (dist(x, y, mouseX, mouseY) < r) ) {
      return true;
    }
    return false;
  }

  boolean canBeDragged() {
    // yes, processes can be dragged
    return true;
  }

  void setCoordinates(float upperX, float upperY, float length, float useLess) {
    x = upperX;
    y = upperY;
    r = length/2;
  }

  void draggingShape(float mouseX, float mouseY) {
    // the mouse has to point the center of the circle
    x = mouseX;
    y = mouseY;
  }

  // a getter for the mathml attribute
  public String getMathml() {
    // return a message in case of missing mathML
    if (mathml == "") {
      return ("No mathML available");
    }
    return mathml;
  }
}

//An arc consists of two control points (beginning and end) and none or some anchor points in the middle
class ArcShape extends Shape implements Draggable {

  // the type of arc 0:consumption, 1:production, 2:modulation, 3:stimulation, 4:catalysis, 5:inhibition, 6:necessary stimulation, 7:logic/equivalence arc
  int type = 0;
  // starting point of the whole arc
  float x0 = 0;
  float y0 = 0;
  // starting point of the last arc (could be the same as x0 and y0 if no anchor point are given)
  float x1 = 0;
  float y1 = 0;
  // the final point of the whole arc
  float x2 = 0;
  float y2 = 0;
  // the end shape anchor point
  float xa = 0;
  float ya = 0;
  // this point is meant to replace (x2, y2) when the stroke is too thick, m=modified
  float x2m = 0;
  float y2m = 0;
  // other anchor points can be drawn if there are defined in the SBGN-ML file
  float[] anchors;
  // tell the diameter/width of the end shape of the arc, the value should be 2/3 times the width of a process nodes
  //float ARC_SHAPE_WIDTH = 30.0;
  // the slope of the last segment of the arc
  private float arcSlope = 0;

  // constructor with an annotation
  ArcShape(int typeArc, float cx1, float cy1, float cx2, float cy2, float[] anchorPoints, String anno) {
    this( typeArc, cx1, cy1, cx2, cy2, anchorPoints );
    this.setAnnotation(anno);
  }

  ArcShape( int typeArc, float cx1, float cy1, float cx2, float cy2, float[] anchorPoints ) {

    type = typeArc;
    x0 = cx1;
    y0 = cy1;
    x1 = cx1;
    y1 = cy1;
    x2 = cx2;
    y2 = cy2;
    anchors = anchorPoints;

    // change the coordinates of the first control point to the last anchor point if any
    int lenAnc = anchors.length;
    if ( lenAnc != 0 ) {
      x1 = anchors[ lenAnc-2 ];
      y1 = anchors[ lenAnc-1 ];
    }

    // WARNING use the last anchor point coodinates to make the maths in this case. Here we use x1 and y1
    // last segment slope calculation
    // horizontal slope

    if ( y2-y1 == 0 ) {
      arcSlope = 0;
      ya = y2;

      if ( x2-x1 > 0 ) {
        // from left to right arc
        xa = x2 - ARC_SHAPE_WIDTH;
      }
      else {
        // from right to left arc
        xa = x2 + ARC_SHAPE_WIDTH;
      }
      println("sD = "+ARC_SHAPE_WIDTH);
      println("hori if x = "+xa+" "+x2);
      println("hori if y = "+ya+" "+y2);
    }
    else {
      // vertical slope
      if ( x2-x1 == 0 ) {
        // arcSlope = null; // cannot be calculated in this case
        xa = x2;
        if ( y2-y1 > 0 ) {
          // from up to down
          ya = y2 - ARC_SHAPE_WIDTH;
        } 
        else {
          // from down to up
          ya = y2 + ARC_SHAPE_WIDTH;
        }
        println("sD = "+ ARC_SHAPE_WIDTH);
        println("vertical if x = "+xa+" "+x2);
        println("vertical if y = "+ya+" "+y2);
      } 
      else {
        // any other slopes
        arcSlope = (y2-y1)/(x2-x1);

        // using vector distance calculation and slope, coordinate of the end point of the arc is determined.
        // The following expression equals |x2-xa|
        float xaAbs = sqrt(ARC_SHAPE_WIDTH*ARC_SHAPE_WIDTH/(arcSlope*arcSlope+1));
        // if x2-xa>0, slope<0 else slope>0
        if ( x2 > x1 ) {
          xa = x2 - xaAbs;
        } 
        else {
          xa = x2 + xaAbs;
        }
        // using the slope definition based on the second control point of the arc (because anchor points can be set between the first and the second)
        ya = arcSlope*(xa - x2) + y2;
      }
    }

    // following calculations were designed to fix the coordinates x2 and y2 (stroke borders were overlapping otherwise)
    // vertical arc
    if ( x2-xa == 0 ) {
      // upwards
      if ( y2-ya < 0 ) {
        y2m = y2 + 2*GLOBAL_STROKE_THICKNESS;
      } 
      else {
        // downwards
        y2m = y2 - 2*GLOBAL_STROKE_THICKNESS;
      }
      x2m = x2;
    } 
    else {
      // horizontal arc
      if ( y2-ya == 0 ) {
        // to the right
        if ( x2-xa > 0 ) {
          x2m = x2 - 2*GLOBAL_STROKE_THICKNESS;
        } 
        else {
          // to the left
          x2m = x2 + 2*GLOBAL_STROKE_THICKNESS;
        }
        y2m = y2;
      } 
      else {
        // for the other cases in the circle
        float beta = atan(arcSlope);
        if ( x2-xa < 0 ) {
          x2m = cos(beta)*2*GLOBAL_STROKE_THICKNESS + x2;
          y2m = sin(beta)*2*GLOBAL_STROKE_THICKNESS + y2;
        } else {
          x2m = -cos(beta)*2*GLOBAL_STROKE_THICKNESS + x2;
          y2m = -sin(beta)*2*GLOBAL_STROKE_THICKNESS + y2;          
        }
      }
    }
  }


  // draw the shape
  void drawShape() {

    // draw the vertex part of the arc
    noFill();

    // stroke color
    stroke(ARC_STROKE_COLOR);

    beginShape();

    // if we have some anchor points, we draw all them...
    if ( anchors.length != 0 ) {

      // ... from the starting point of the whole arc... 
      vertex( x0, y0 );

      // ... to the last but one anchor point in the anchors array if we have more than one anchor point
      if ( anchors.length > 2 ) {

        // loop over all anchors except the last one
        for ( int i=0; i<anchors.length-2; i+=2 ) {
          vertex( anchors[i], anchors[i+1] );
        }
      }
    }

    // beginning point, port or node usually or last anchor point if any
    vertex( x1, y1 );

    // end point of the arc, beginning of arc final shape
    vertex( xa, ya );

    // draw the final shape
    switch(type) {
    case 0 :
      // the end is straight and so reached the second control point
      vertex( x2, y2 );
      endShape();
      break;
    case 1 :
      endShape();
      fill(0, 0, 0);// black filling
      // using trigonometry to calculate the coordianates of any triangles
      println("sD case 1 = "+ARC_SHAPE_WIDTH);

      float cosinus = ( x2-xa )/ARC_SHAPE_WIDTH;
      float sinus = ( y2-ya )/ARC_SHAPE_WIDTH;
      triangle( -sinus*ARC_SHAPE_WIDTH/4 + xa, ya + cosinus*ARC_SHAPE_WIDTH/4, x2m, y2m, sinus*ARC_SHAPE_WIDTH/4 + xa, ya - cosinus*ARC_SHAPE_WIDTH/4 );
      break;
    case 2 :
      endShape();
      fill(255, 255, 255);//white filling

      // using trigonometry to calculate the coordinates of the end shape
      // sin and cos of the rotation angle
      float cosar = ( x2m-xa )/ARC_SHAPE_WIDTH;
      float sinar = ( y2m-ya )/ARC_SHAPE_WIDTH;
      // sin and cos of the triangle angle
      float cosat = sqrt(2)/2;
      float sinat = sqrt(2)/2;

      // coordinates of the first point
      float xi1 = ( cosar*cosat - sinar*sinat )*ARC_SHAPE_WIDTH/sqrt(2) + xa;
      float yi1 = ( sinar*cosat + cosar*sinat )*ARC_SHAPE_WIDTH/sqrt(2) + ya;

      // coordinates of the second point
      float xi2 = ( cosar*cosat + sinar*sinat )*ARC_SHAPE_WIDTH/sqrt(2) + xa;
      float yi2 = ( sinar*cosat - cosar*sinat )*ARC_SHAPE_WIDTH/sqrt(2) + ya;

      // has to think about this shape

      beginShape();
      vertex( xa, ya );
      vertex( xi1, yi1 );
      vertex( x2m, y2m );
      vertex( xi2, yi2 );
      vertex( xa, ya );
      endShape();

      break;
    case 3 :
      endShape();
      fill(255, 255, 255);// white filling
      // using trigonometry to calculate the coordinates of any triangles
      cosinus = ( x2m-xa )/ARC_SHAPE_WIDTH;
      sinus = ( y2m-ya )/ARC_SHAPE_WIDTH;
      triangle( -sinus*ARC_SHAPE_WIDTH/4 + xa, ya + cosinus*ARC_SHAPE_WIDTH/4, x2m, y2m, sinus*ARC_SHAPE_WIDTH/4 + xa, ya - cosinus*ARC_SHAPE_WIDTH/4 );
      break;
    case 4 :
      endShape();
      fill(255, 255, 255); // white filling
      ellipse( xa+(x2m-xa)/2, ya+(y2m-ya)/2, ARC_SHAPE_WIDTH, ARC_SHAPE_WIDTH );
      break;
    case 5 :
      vertex( x2m, y2m );
      endShape();
      // using trigonometry to calculate the coordinates of the end shape
      // sin and cos of the rotation angle
      cosar = ( x2m-xa )/ARC_SHAPE_WIDTH;
      sinar = ( y2m-ya )/ARC_SHAPE_WIDTH;
      // sin and cos of the triangle angle
      cosat = ARC_SHAPE_WIDTH/sqrt( ARC_SHAPE_WIDTH*ARC_SHAPE_WIDTH + ARC_SHAPE_WIDTH*ARC_SHAPE_WIDTH/4 );
      sinat = ARC_SHAPE_WIDTH/(ARC_SHAPE_WIDTH*2.0);

      // coordinates of the first point
      xi1 = ( cosar*cosat - sinar*sinat )*ARC_SHAPE_WIDTH + xa;
      yi1 = ( sinar*cosat + cosar*sinat )*ARC_SHAPE_WIDTH + ya;

      // coordinates of the second point
      xi2 = ( cosar*cosat + sinar*sinat )*ARC_SHAPE_WIDTH + xa;
      yi2 = ( sinar*cosat - cosar*sinat )*ARC_SHAPE_WIDTH + ya;

      // line for the inhibition
      line( xi1, yi1, xi2, yi2 );
      break;
    case 6 :
      endShape();
      fill( 255, 255, 255 );// white filling

      println("x2 = "+x2m+" y2 = "+y2m);
      println("xa = "+xa+" ya = "+ya);


      cosinus = ( x2m-xa )/ARC_SHAPE_WIDTH;
      sinus = ( y2m-ya )/ARC_SHAPE_WIDTH;
      // basal line, the length of the segment is ( ARC_SHAPE_WIDTH/2.5 )*2
      line( -sinus*ARC_SHAPE_WIDTH/2.5 + xa, ya + cosinus*ARC_SHAPE_WIDTH/2.5, sinus*ARC_SHAPE_WIDTH/2.5 + xa, ya - cosinus*ARC_SHAPE_WIDTH/2.5 );

      // do not forget to print the little tail also
      // draw the little tail, the remaining lines will be hidden by the triangle
      line( xa, ya, x2m, y2m );

      float scaleFactor = 5.0/ARC_SHAPE_WIDTH;
      // coordinates of the intersection point: triangle/line
      float xint = scaleFactor*( x2m-xa ) + xa;
      float yint = scaleFactor*( y2m-ya ) + ya;

      // triangle
      triangle( -sinus*ARC_SHAPE_WIDTH/4 + xint, cosinus*ARC_SHAPE_WIDTH/4 + yint, x2m, y2m, sinus*ARC_SHAPE_WIDTH/4 + xint, yint - cosinus*ARC_SHAPE_WIDTH/4 );

      break;
    case 7 :
      vertex( x2, y2 );
      endShape();
      break;
    default :
      break;
    }
  }


  // tell if the mouse is over the object by looking at geometrical conditions
  boolean hasMouseOver(int mouseX, int mouseY) {
    // we do not deal with it for the moment
    return false;
  }


  // authorize the object to be dragged
  boolean canBeDragged() {
    // not for the moment
    return false;
  }


  // modify object coordinates (enabling another drawing afterwards) taken from the SBGN-ML bounding box
  void setCoordinates(float arg1, float arg2, float arg3, float arg4) {
    // a bit complicated for the moment
  }


  // Take mouseX and mouseY and calculate appropriate coordinates for dragging
  void draggingShape(float mouseX, float mouseY) {
    // not for the moment
  }
}

