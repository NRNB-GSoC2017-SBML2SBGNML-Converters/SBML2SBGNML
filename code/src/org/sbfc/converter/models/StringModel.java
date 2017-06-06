/*
 * $Id: StringModel.java 551 2015-09-22 13:43:36Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/models/StringModel.java $
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

package org.sbfc.converter.models;

import org.sbfc.converter.exceptions.ReadModelException;
import org.sbfc.converter.exceptions.WriteModelException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public abstract class StringModel implements GeneralModel {
	
	private String model;
	private String fileName;
	
	public void modelToFile(String fileName) throws WriteModelException {
		
		//Printing String to a file
		FileOutputStream file;
		try {
			file = new FileOutputStream (fileName);
			PrintStream printFile = new PrintStream(file);
			printFile.println (model);
			file.close();
		} catch (FileNotFoundException e) {
			throw new WriteModelException(e);
		} catch (IOException e) {
          throw new WriteModelException(e);
		}
		
	}

	public String modelToString() throws WriteModelException {
		return model;
	}
	
	public String getModel() {
		return model;
	}

	/**
	 * Returns the file name that was used to set this {@link GeneralModel} if
	 * it was set using the {@link #setModelFromFile(String)} method, null otherwise.
	 * 
	 * @return the file name that was used to set this {@link GeneralModel} if
	 * it was set using the {@link #setModelFromFile(String)} method, null otherwise.
	 */
	public String getModelFileName() {
		return fileName;
	}
	
	public void setModelFromFile(String fileName) throws ReadModelException {
		try {
			//Reading file and putting it in a String
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String result="";
			String line;
			while ((line = in.readLine()) != null) {
				result += line + "\n";
			}
            in.close();
            this.model = result;
			this.fileName = fileName;
		} catch (IOException e) {
			throw new ReadModelException(e);
		}
	}

	public void setModelFromString(String modelString) throws ReadModelException {
		this.model = modelString;
		this.fileName = null;
	}

}
