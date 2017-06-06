/*
 * $Id: MiriamAnnotation.java 379 2015-07-07 15:33:22Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/utils/sbml/sbmlannotation/MiriamAnnotation.java $
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

package org.sbfc.converter.utils.sbml.sbmlannotation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Holds the uri and id of a Miriam annotation found in an SBML file.
 * 
 * @author rodrigue
 *
 */
public class MiriamAnnotation {

	String uri;
	String identifiers_org_uri;
	String id;
	
	
	
	public MiriamAnnotation(String id, String uri) {
		this.id = id;
		this.uri = uri;
	}
	
	public MiriamAnnotation(String id, String uri, String identifiers_orgURI) {
		this(id, uri);
		this.identifiers_org_uri = identifiers_orgURI;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the full annotation URI, including the identifier part.
	 * 
	 * @return the full annotation URI, including the identifier part.
	 */
	public String getFullURI() {
		
		if (uri.startsWith("http://identifiers.org")) 
		{
			return uri + id;
		}
		else if (uri.startsWith("urn:")) 
		{
			try 
			{
				return uri + ":" + URLEncoder.encode(id, "UTF-8");
			}
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * Returns the identifiers_org_uri
	 *
	 * @return the identifiers_org_uri
	 */
	public String getIdentifiers_orgURI() {
		return identifiers_org_uri;
	}

	/**
	 * Sets the identifiers_org_uri.
	 *
	 * @param identifiers_org_uri the identifiers_org_uri to set
	 */
	public void setIdentifiers_orgURI(String identifiers_org_uri) {
		this.identifiers_org_uri = identifiers_org_uri;
	}
}
