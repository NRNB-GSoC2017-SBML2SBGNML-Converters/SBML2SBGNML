package org.sbfc.converter.sbml2sbgnml;

public class SBMLWrapperPoint extends org.sbml.jsbml.ext.layout.Point {
	
	org.sbgn.bindings.Point basePoint1;
	org.sbgn.bindings.Point basePoint2;
	org.sbml.jsbml.ext.layout.Point targetPoint;
	
	/**
	 * A SBMLWrapperPoint stores a CubicBezier
	 * @param targetPoint
	 * @param basePoint1
	 * @param basePoint2
	 */
	SBMLWrapperPoint(org.sbml.jsbml.ext.layout.Point targetPoint, org.sbml.jsbml.ext.layout.Point basePoint1, org.sbml.jsbml.ext.layout.Point basePoint2){
		this.basePoint1 = new org.sbgn.bindings.Point();
		// TODO: losing precision
		this.basePoint1.setX((float) basePoint1.getX()); 
		this.basePoint1.setY((float) basePoint1.getY());
				
		this.basePoint2 = new org.sbgn.bindings.Point();
		this.basePoint2.setX((float) basePoint2.getX()); 
		this.basePoint2.setY((float) basePoint2.getY());
		
		this.targetPoint = targetPoint;
	}
}
