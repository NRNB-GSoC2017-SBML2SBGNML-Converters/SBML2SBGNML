/*
  PROCESSINGJS.COM HEADER ANIMATION
 MIT License - Hyper-Metrix.com/F1LT3R
 Native Processing compatible 
 */

// the following variables has to be put in main by java
/////////////////// GLOBAL VARIABLES ////////////////////

// frame rate
static int FRAME_RATE = 20;

// scale factor, increase size by x %
static float SCALE_FACTOR = 0.0;

// strokeweight of the shapes/compartement
static float GLOBAL_STROKE_THICKNESS = 2.0;
static float COMP_STROKE_THICKNESS = 10.0;

// ***************** COLORS, RGB format ***************
// BACKGROUND COLORS //
// entire processing drawing
color GLOBAL_COLOR_BACKGROUND = color( 230, 230, 230 );
// compartment
color COMP_BACK_COLOR = color( 255, 255, 255 );
// entity pool nodes
color EPN_BACK_COLOR = color( 100, 100, 100 );
// auxiliary units:unit of information and state of information
color AUXI_BACK_COLOR = color( 120, 23, 46 );
// process nodes (if color can be changed)
color PROC_BACK_COLOR = color( 255, 255, 255 );
// reference nodes (tag, submap)
color TAG_BACK_COLOR = color( 255, 255, 255 );
// logical operator
color LOGICAL_BACK_COLOR = color( 255, 255, 255 );
// clones
color CLONE_BACK_COLOR = color( 0, 0, 0 );
// multimer shadow color
color MULT_SHADOW_COLOR = color( 1, 210, 0 );

// SHAPE STROKE COLORS //
// compartment
color COMP_STROKE_COLOR = color( 200, 200, 200 );
// EPNS shapes
color EPN_STROKE_COLOR = color( 153, 153, 153 );
// Auxiliary units shapes
color AUXI_STROKE_COLOR = color( 23, 123, 78 );
// Process nodes shapes
color PROC_STROKE_COLOR = color( 0, 0, 0 );
// Logical operators
color LOGICAL_STROKE_COLOR = color( 0, 0, 0 );
// Reference nodes

// Connecting arcs
color ARC_STROKE_COLOR = color( 0, 0, 0 );

// LABEL FONT COLORS //
// character stroke color for label in EPNS
color EPN_LAB_FONT_COLOR = color( 30, 210, 100 );
// character stroke color for label in auxiliary units
color AUXI_LAB_FONT_COLOR = color ( 234, 23, 23 );
// character stroke color for logical operators
color LOGICAL_LAB_FONT_COLOR = color( 2, 123, 234 );
// character stroke color for label in tags and submaps
color REF_LAB_FONT_COLOR = color( 200, 200, 200 );
// character stroke color for label in compartment
color COMP_LAB_FONT_COLOR = color( 200, 200, 200 );
// character stroke dolor for label in process nodes especially phenotype
color PROC_LAB_FONT_COLOR = color( 0, 0, 0 );

// MISC
// surface percentage (based on total height) of black covering for clones, <0.5
static float CLONE_PERC = 0.25;
// clone x and y switch
static int CLONE_SWITCH = 3;
// diameter, width of arcs
static float ARC_SHAPE_WIDTH = 16.0;
// displaying window width, automatically determined
//static int WINDOW_WIDTH = 1000;
// displaying window height, automatically determined
//static int WINDOW_HEIGHT = 1300;
// needed to align texts in a glyph
static float ALIGN_Y_LABEL_MODIFIER = 5.0;
// align switch for labels with than one line
static float ALIGN_Y_LABEL_MULTIPLIER = 9.0;

// FONT
// Font files must be located in the data directory.
static PFont LUCIDA;
static PFont DEJAVU_BOLD;

////////////////////////////////////////////////////


// the following two commands are for calling javascript function inside processing.js code
// sketch-global object that will point to the on-page javascript
JavaScript javascript = null;

// a method for binding the page's javascript environment to the in-sketch jsinterface object
void setJavaScript(JavaScript js) { 
  javascript = js;
}



// Shape selection switch
int sel = 0;

void setup()
{
  // Frame rate
  frameRate(FRAME_RATE);

  // Stroke/line/border thickness
  strokeWeight(2);

  // load font defined through global variables
  LUCIDA = loadFont("LucidaBright-12.vlw");
  LUCIDA = loadFont("DejaVuLGCSerif-Bold-12.vlw");

  // size of the window (width/height)
  size( WINDOW_WIDTH, WINDOW_HEIGHT );

  // set color background
  background(GLOBAL_COLOR_BACKGROUND);

  // put the loop on in order to use onClick functions
  //  noLoop();
}

// Set the dragging test
boolean dragging = false;

// if the mouse is dragged...
void mouseDragged() {
  dragging = true;
}


// If the mouse is released...
void mouseReleased() {
  dragging = false;
  // we erase the memory of the selected shape
  sel = 0;
}

// if the mouse is over...
void mouseOver() {
}

// if the mouse is out...
void mouseOut() {
}

// tell if the shapes are drawn or not
static int drawing = 0;

void draw()
{

  // Beware when scaled... mouseX and mouseY are not.
  //scale(2.0);


  // loop over the shapes array
  for (int index=0; index<COUNT; index = index + 1) {

    // if the shapes are not drawn, drawing takes place once
    if ( drawing == 0 ) {
      // draw the shape
      SHAPES[index].drawShape();
    }

    // access annotation like this:
    //      println(SHAPES[index].getAnnotation());

    if (SHAPES[index].canSupportMathml) {
//      println("    HERE = "+SHAPES[index].getMathml());

      if ( javascript != null ) {
        javascript.drawMathML(SHAPES[index].getMathml());
        println("javascript != null");
      }
    }
  }

  // all the shapes are drawn
  drawing = 1;
}















