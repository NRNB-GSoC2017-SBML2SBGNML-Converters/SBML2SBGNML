package org.sbfc.converter.sbgnml2sbml;

import java.util.List;

/**
 * This class stores meta information of a curved arc, equivalent to a SBML layout CubicBezier
 * @author haoran
 *
 */
public class SBGNWrapperPoint extends org.sbml.jsbml.ext.layout.Point {
	org.sbml.jsbml.ext.layout.Point basePoint1;
	org.sbml.jsbml.ext.layout.Point basePoint2;
	org.sbml.jsbml.ext.layout.Point onePoint;
			
	SBGNWrapperPoint(float x, float y){
		this.onePoint = new org.sbml.jsbml.ext.layout.Point(x, y);
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
