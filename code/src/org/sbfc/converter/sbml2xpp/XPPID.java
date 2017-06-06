/*
 * $Id: XPPID.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2xpp/XPPID.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2015 jointly by the following organizations:
 * 1. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 2. The Babraham Institute, Cambridge, UK
 * 3. Department of Bioinformatics, BiGCaT, Maastricht University
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as 
 * <http://sbfc.sf.net/mediawiki/index.php/License>.
 * 
 * ==========================================================================
 * 
 */
package org.sbfc.converter.sbml2xpp;

import java.util.HashMap;

/**
 * Checks and Converts the SBML elements id's into compliant XPP id. 
 * 
 * @author rodrigue
 * @author Kedar Nath Natarajan
 */
public class XPPID {

	private static HashMap<String, XPPID> xppIdMap = new HashMap<String, XPPID>();
	private static HashMap<String, Integer> xppIdReduceMap = new HashMap<String, Integer>();
	private static HashMap<String, XPPID> sbmlIdMap = new HashMap<String, XPPID>();
	
	static {
		sbmlIdMap.put("time", new XPPID("time"));
		sbmlIdMap.put("Time", new XPPID("time"));
		sbmlIdMap.put("floor", new XPPID("floor"));
		// sbmlIdMap.put("T", new XPPID("T"));
		// sbmlIdMap.put("t", new XPPID("t"));
	}

	private String sbmlId;
	private String xppId;
	private String xppIdUpercase;

	public XPPID(String sbmlId) {
		this.sbmlId = sbmlId;
		this.xppId = sbmlId;
		
		if (xppId.equalsIgnoreCase("time")) {
			this.xppId = "t";
		} else if (xppId.equalsIgnoreCase("floor")) {
			this.xppId = "flr";
		} else if (xppId.equalsIgnoreCase("t")) { // xpp reserved keywords
			this.xppId = sbmlId + "_renamed";
		} else if (xppId.equalsIgnoreCase("pi")) { // xpp reserved keywords
			this.xppId = sbmlId + "_ren";
		} // TODO : do the same for all the xpp reserved keyword ??

		this.xppIdUpercase = sbmlId.toUpperCase();
	}

	public XPPID(String sbmlId, String sbmlReactionId) {
		this.sbmlId = sbmlId + "_" + sbmlReactionId;
		this.xppId = sbmlId;
		this.xppIdUpercase = xppId.toUpperCase();
	}

	/**
	 * Checks that the xppId comply to the constraints made by XPP.
	 * 
	 * - 9 character maximum - no differences between upper case and lower case
	 * character in XPP
	 * 
	 * If necessary, xppId is updated, and the current XPPID is added to the map
	 * 
	 */
	public void checkXPPId() {

		if (xppId.length() > 9) {
			xppId = xppId.substring(0, 9);
			xppIdUpercase = xppId.toUpperCase();
		}

		// System.out.println("XPPID : checkXppId : sbmlId = " + sbmlId +
		// ", xppId = " + xppId + ", xppID = " + xppIdUpercase);

		boolean isUnique = false;
		Integer idNb = xppIdReduceMap.get(xppIdUpercase);
		int i = 1;

		if (idNb != null) {
			i = idNb;
		}

		while (!isUnique) {
			XPPID conflictingId = xppIdMap.get(xppIdUpercase);

			if (conflictingId == null) {

				isUnique = true;
				break;
			}
			reduceId(i);
			i++;
		}

		// System.out.println("XPPID : checkXppId : sbmlId = " + sbmlId +
		// ", xppId = " + xppId);

		xppIdUpercase = xppId.toUpperCase();
		xppIdMap.put(xppIdUpercase, this);
		xppIdReduceMap.put(xppIdUpercase, i);
		sbmlIdMap.put(sbmlId, this);
	}

	/**
	 * Checks if Id is unique or else reduces to 7 and adds "_i {1..100} " <br />  
	 * Iterates till id is unique or till ( i < 100 )
	 * @param i : iterates i from 1 to 100 
	 */
	private void reduceId(int i) {

		// System.out.println("XPPID : reduceId : xppId = " + xppId + ", i = " +
		// i);

		if (i < 10) {
			if (xppId.length() > 7) {
				xppId = xppId.substring(0, 7);
			}

			xppId += "_" + i;

		} else if (i < 100) {
			if (xppId.length() > 6) {
				xppId = xppId.substring(0, 6);
			}

			xppId = xppId + "_" + i;

		} else {
			if (xppId.length() > 4) {
				xppId = xppId.substring(0, 4);
			}

			xppId = xppId + "_" + i;
			// id will not be valid if number goes over 10 000 !!
		}

		xppIdUpercase = xppId.toUpperCase();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XPPID) {
			return ((XPPID) obj).getXppId().equalsIgnoreCase(xppId);
		} else if (obj instanceof String) {
			return ((String) obj).equalsIgnoreCase(xppId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return xppIdUpercase.hashCode();
	}

	@Override
	public String toString() {
		return xppId;
	}

	/**
	 * Gets the SBMLid
	 * @return : SBMLid
	 */
	public String getSbmlId() {
		return sbmlId;
	}

	/**
	 * Gets the XPPid
	 * @return : XPPid
	 */
	public String getXppId() {
		return xppId;
	}

	/**
	 * Changes XPPid to Upper Case
	 * @return : XPPid to Upper Case
	 */
	public String getXppIdUpercase() {
		return xppIdUpercase;
	}

	/**
	 * Gets the SBMLid corresponding to XPPid 
	 * 
	 * @param xppId : 
	 * @return : SBMLId
	 */
	public static String getSBMLId(String xppId) {

		XPPID id = xppIdMap.get(xppId.toUpperCase());

		if (id == null) {
			return null;
		}

		return id.getSbmlId();
	}

	/**
	 * Gets the XPPid corresponding to SBMLid
	 * 
	 * @param sbmlId
	 * @return : XPPid 
	 */
	public static String getXPPId(String sbmlId) {

		XPPID id = sbmlIdMap.get(sbmlId);

		if (id == null) {
			return null;
		}

		return id.getXppId();

	}

}
