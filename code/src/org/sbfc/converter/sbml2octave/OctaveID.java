/*
 * $Id: OctaveID.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2octave/OctaveID.java $
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

/*
 * $Id: OctaveID.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/sbml2octave/OctaveID.java $
 *
 *
 * ==============================================================================
 * Copyright (c) 2010 the copyright is held jointly by the individual
 * authors. See the file AUTHORS for the list of authors
 *
 * This file is part of The System Biology Format Converter (SBFC).
 *
 * SBFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SBFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SBFC.  If not, see<http://www.gnu.org/licenses/>.
 * 
 * ==============================================================================
 * 
 */

package org.sbfc.converter.sbml2octave;

import java.util.HashMap;

/**
 * Checks and Converts the SBML elements id's into Octave equivalent. 
 * 
 * @author rodrigue
 * @author jbpettit
 * 
 */
public class OctaveID {

	private static HashMap<String, OctaveID> octaveIdMap = new HashMap<String, OctaveID>();
	private static HashMap<String, OctaveID> sbmlIdMap = new HashMap<String, OctaveID>();
	
	static {
		sbmlIdMap.put("time", new OctaveID("time"));
		sbmlIdMap.put("Time", new OctaveID("time"));
		sbmlIdMap.put("floor", new OctaveID("floor"));
	}

	private String sbmlId;
	private String octaveId;

	public OctaveID(String sbmlId) {
		this.sbmlId = sbmlId;
		this.octaveId = sbmlId;
		
	}
	
	public OctaveID(String  sbmlId, String octaveId, Boolean diff) {
		this.sbmlId = sbmlId;
		this.octaveId = octaveId;
	}

	public OctaveID(String sbmlId, String sbmlReactionId) {
		this.sbmlId = sbmlId + "_" + sbmlReactionId;
		this.octaveId = sbmlId;
	}

	/**
	 * Checks that the octaveId comply to the constraints made by Octave.
	 * 
	 * - 30 character maximum
	 * 
	 * If necessary, octaveId is updated, and the current OctaveID is added to the map
	 * 
	 */
	public void checkOctaveId() {



		octaveIdMap.put(octaveId, this);
		sbmlIdMap.put(sbmlId, this);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OctaveID) {
			return ((OctaveID) obj).getOctaveId().equalsIgnoreCase(octaveId);
		} else if (obj instanceof String) {
			return ((String) obj).equalsIgnoreCase(octaveId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return octaveId.hashCode();
	}

	@Override
	public String toString() {
		return octaveId;
	}

	/**
	 * Gets the SBMLid
	 * @return : SBMLid
	 */
	public String getSbmlId() {
		return sbmlId;
	}

	/**
	 * Gets the Octaveid
	 * @return : Octaveid
	 */
	public String getOctaveId() {
		return octaveId;
	}
	


	/**
	 * Gets the SBMLid corresponding to Octaveid 
	 * @param octaveId : 
	 * @return : SBMLId
	 */
	public static String getSBMLId(String octaveId) {

		OctaveID id = octaveIdMap.get(octaveId);

		if (id == null) {
			return null;
		}

		return id.getSbmlId();
	}

	/**
	 * Gets the Octaveid corresponding to SBMLid
	 * @param sbmlId
	 * @return : Octaveid 
	 */
	public static String getOctaveId(String sbmlId) {

		OctaveID id = sbmlIdMap.get(sbmlId);

		if (id == null) {
			return null;
		}

		return id.getOctaveId();

	}
	

}
