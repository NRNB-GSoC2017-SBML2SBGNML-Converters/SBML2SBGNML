/*
 * $Id: APMID.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2apm/APMID.java $
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
package org.sbfc.converter.sbml2apm;

import java.util.HashMap;

/**
 * Checks and Converts the SBML elements id's into compliant APM id. 
 * 
 * @author rodrigue
 * @author Kedar Nath Natarajan
 */
public class APMID {

	private static HashMap<String, APMID> apmIdMap = new HashMap<String, APMID>();
	private static HashMap<String, Integer> apmIdReduceMap = new HashMap<String, Integer>();
	private static HashMap<String, APMID> sbmlIdMap = new HashMap<String, APMID>();
	
	static {
		sbmlIdMap.put("time", new APMID("time"));
		sbmlIdMap.put("Time", new APMID("time"));
		sbmlIdMap.put("floor", new APMID("floor"));
		// sbmlIdMap.put("T", new APMID("T"));
		// sbmlIdMap.put("t", new APMID("t"));
	}

	private String sbmlId;
	private String apmId;
	private String apmIdUpercase;

	public APMID(String sbmlId) {
		this.sbmlId = sbmlId;
		this.apmId = sbmlId;
		
		if (apmId.equalsIgnoreCase("time")) {
			this.apmId = "t";
		} else if (apmId.equalsIgnoreCase("floor")) {
			this.apmId = "flr";
		} else if (apmId.equalsIgnoreCase("t")) { // apm reserved keywords
			this.apmId = sbmlId + "_renamed";
		} else if (apmId.equalsIgnoreCase("pi")) { // apm reserved keywords
			this.apmId = sbmlId + "_ren";
		} // TODO : do the same for all the apm reserved keyword ??

		this.apmIdUpercase = sbmlId.toUpperCase();
	}

	public APMID(String sbmlId, String sbmlReactionId) {
		this.sbmlId = sbmlId + "_" + sbmlReactionId;
		this.apmId = sbmlId;
		this.apmIdUpercase = apmId.toUpperCase();
	}

	/**
	 * Checks that the apmId comply to the constraints made by apm.
	 * 
	 * - 9 character maximum - no differences between upper case and lower case
	 * character in APM
	 * 
	 * If necessary, apmId is updated, and the current APMID is added to the map
	 * 
	 */
	public void checkAPMId() {

		if (apmId.length() > 9) {
			apmId = apmId.substring(0, 9);
			apmIdUpercase = apmId.toUpperCase();
		}

		// System.out.println("APMID : checkXppId : sbmlId = " + sbmlId +
		// ", apmId = " + apmId + ", apmID = " + apmIdUpercase);

		boolean isUnique = false;
		Integer idNb = apmIdReduceMap.get(apmIdUpercase);
		int i = 1;

		if (idNb != null) {
			i = idNb;
		}

		while (!isUnique) {
			APMID conflictingId = apmIdMap.get(apmIdUpercase);

			if (conflictingId == null) {

				isUnique = true;
				break;
			}
			reduceId(i);
			i++;
		}

		// System.out.println("APMID : checkXppId : sbmlId = " + sbmlId +
		// ", apmId = " + apmId);

		apmIdUpercase = apmId.toUpperCase();
		apmIdMap.put(apmIdUpercase, this);
		apmIdReduceMap.put(apmIdUpercase, i);
		sbmlIdMap.put(sbmlId, this);
	}

	/**
	 * Checks if Id is unique or else reduces to 7 and adds "_i {1..100} " <br />  
	 * Iterates till id is unique or till ( i < 100 )
	 * @param i : iterates i from 1 to 100 
	 */
	private void reduceId(int i) {

		// System.out.println("APMID : reduceId : apmId = " + apmId + ", i = " +
		// i);

		if (i < 10) {
			if (apmId.length() > 7) {
				apmId = apmId.substring(0, 7);
			}

			apmId += "_" + i;

		} else if (i < 100) {
			if (apmId.length() > 6) {
				apmId = apmId.substring(0, 6);
			}

			apmId = apmId + "_" + i;

		} else {
			if (apmId.length() > 4) {
				apmId = apmId.substring(0, 4);
			}

			apmId = apmId + "_" + i;
			// id will not be valid if number goes over 10 000 !!
		}

		apmIdUpercase = apmId.toUpperCase();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof APMID) {
			return ((APMID) obj).getXppId().equalsIgnoreCase(apmId);
		} else if (obj instanceof String) {
			return ((String) obj).equalsIgnoreCase(apmId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return apmIdUpercase.hashCode();
	}

	@Override
	public String toString() {
		return apmId;
	}

	/**
	 * Gets the SBMLid
	 * @return : SBMLid
	 */
	public String getSbmlId() {
		return sbmlId;
	}

	/**
	 * Gets the APMid
	 * @return : APMid
	 */
	public String getXppId() {
		return apmId;
	}

	/**
	 * Changes APMid to Upper Case
	 * @return : APMid to Upper Case
	 */
	public String getXppIdUpercase() {
		return apmIdUpercase;
	}

	/**
	 * Gets the SBMLid corresponding to APMid 
	 * 
	 * @param apmId : 
	 * @return : SBMLId
	 */
	public static String getSBMLId(String apmId) {

		APMID id = apmIdMap.get(apmId.toUpperCase());

		if (id == null) {
			return null;
		}

		return id.getSbmlId();
	}

	/**
	 * Gets the APMid corresponding to SBMLid
	 * 
	 * @param sbmlId
	 * @return : APMid 
	 */
	public static String getAPMId(String sbmlId) {

		APMID id = sbmlIdMap.get(sbmlId);

		if (id == null) {
			return null;
		}

		return id.getXppId();

	}

}
