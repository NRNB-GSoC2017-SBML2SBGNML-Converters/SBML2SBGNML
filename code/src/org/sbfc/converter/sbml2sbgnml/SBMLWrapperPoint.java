package org.sbfc.converter.sbml2sbgnml;

public class SBMLWrapperPoint extends org.sbml.jsbml.ext.layout.Point {
//	org.sbgn.bindings.Point basePoint1;
//	org.sbgn.bindings.Point basePoint2;
//	org.sbgn.bindings.Point targetPoint;
	
	org.sbgn.bindings.Point basePoint1;
	org.sbgn.bindings.Point basePoint2;
	org.sbml.jsbml.ext.layout.Point targetPoint;
	
	SBMLWrapperPoint(org.sbml.jsbml.ext.layout.Point basePoint1, org.sbml.jsbml.ext.layout.Point basePoint2, org.sbml.jsbml.ext.layout.Point targetPoint){
		this.basePoint1 = new org.sbgn.bindings.Point();
		// todo: losing precision
		this.basePoint1.setX((float) basePoint1.getX()); 
		this.basePoint1.setY((float) basePoint1.getY());
				
		this.basePoint2 = new org.sbgn.bindings.Point();
		this.basePoint2.setX((float) basePoint2.getX()); 
		this.basePoint2.setY((float) basePoint2.getY());
		
		this.targetPoint = targetPoint;
	}
}
