package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;

/**
 * This class stores meta information of an Sbgn arc
 * @author haoran
 *
 */
public class SWrapperArc {
	public Arc arc;
	public String arcId;
	String sourceTargetType;
	public String arcClazz;
	public String sourceId;
	public String targetId;
	Object source;
	Object target;
	
	public SWrapperArc(Arc arc, String sourceTargetType, String sourceId, String targetId, Object source, Object target){
		this.arc = arc;
		this.arcId = arc.getId();
		this.sourceTargetType = sourceTargetType;
		this.arcClazz = arc.getClazz();
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.source = source;
		this.target = target;
	}
}
