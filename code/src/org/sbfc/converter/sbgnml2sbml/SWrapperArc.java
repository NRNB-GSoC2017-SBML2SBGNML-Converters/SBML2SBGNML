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
	//sourceTargetType tells whether the Arc Source is a Glyph or a Port, and whether the Arc Target is a Glyph or a Port
	String sourceTargetType;
	public String arcClazz;
	//sourceId is the Glyph id of the Source, even if the Arc points to a Port
	public String sourceId;
	//targetId is the Glyph id of the Target, even if the Arc points to a Port
	public String targetId;
	Object source = null;
	Object target = null;
	
	public SWrapperArc(Arc arc, String sourceTargetType, String sourceId, String targetId, Object source, Object target){
		this.arc = arc;
		this.arcId = arc.getId();
		this.sourceTargetType = sourceTargetType;
		this.arcClazz = arc.getClazz();
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.source = source;
		this.target = target;
		
		//System.out.println("sourceId "+ sourceId + " targetId "+ targetId);
	}
}
