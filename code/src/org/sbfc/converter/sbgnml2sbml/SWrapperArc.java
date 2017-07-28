package org.sbfc.converter.sbgnml2sbml;

import org.sbgn.bindings.Arc;

public class SWrapperArc {
	public Arc arc;
	public String arcId;
	String sourceTargetType;
	String arcClazz;
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
