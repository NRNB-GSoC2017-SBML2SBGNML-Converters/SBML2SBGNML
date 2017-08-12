package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

public class SBGNWrapperPoint extends org.sbml.jsbml.ext.layout.Point {
	org.sbml.jsbml.ext.layout.Point basePoint1;
	org.sbml.jsbml.ext.layout.Point basePoint2;
	org.sbml.jsbml.ext.layout.Point targetPoint;
		
//	SBGNWrapperPoint(org.sbgn.bindings.Point basePoint1, org.sbgn.bindings.Point basePoint2, org.sbgn.bindings.Point targetPoint){
//		this.basePoint1 = basePoint1;
//		this.basePoint2 = basePoint2;
//		this.targetPoint = targetPoint;
//	}
	
	SBGNWrapperPoint(float x, float y){
		this.targetPoint = new org.sbml.jsbml.ext.layout.Point(x, y);
	}
	
	public void addbasePoint(List<org.sbgn.bindings.Point> points){
		if (points.size() == 2){
			this.basePoint1 = new org.sbml.jsbml.ext.layout.Point(points.get(0).getX(), points.get(0).getY());
			this.basePoint2 = new org.sbml.jsbml.ext.layout.Point(points.get(1).getX(), points.get(1).getY());
		} else if (points.size() > 2) {
			this.basePoint1 = new org.sbml.jsbml.ext.layout.Point(points.get(0).getX(), points.get(0).getY());
			this.basePoint2 = new org.sbml.jsbml.ext.layout.Point(points.get(1).getX(), points.get(1).getY());			
		}
	}
}
