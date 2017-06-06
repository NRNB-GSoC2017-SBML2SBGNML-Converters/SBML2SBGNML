/*
 * $Id: ConverterSearcher.java 379 2015-07-23 15:33:22Z pdp10 $
 * $URL: svn+ssh://pdp10@svn.code.sf.net/p/sbfc/code/trunk/src/org/util/classlist/ConverterSearcher.java $
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

package org.util.classlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sbfc.converter.GeneralConverter;


/**
 * This class retrieves the list of converters available in SBFC.
 * @author Piero Dalle Pezze
 */
public class ConverterSearcher {
	
	private static String converterSuperPackage = "org.sbfc.converter"; // TODO - in the future, we should probably not use any super package
	
	// TODO - get all the GeneralModel class as well.
	
	/**
	 * Return the container package of the SBFC converters. Currently, this is org.sbfc.converter.
	 * @return the container package of the SBFC converters
	 */
	public static String getConverterSuperPackage() {
		return converterSuperPackage;
	}
	
	/**
	 * Check whether className is a converter
	 * @param className a fully qualified class name including package
	 * @return true if className is a converter
	 */
	private static boolean isConverter(String className) {
		try {
			Class<?> cls = Class.forName(className);
			Object clsInstance = (Object) cls.newInstance();
			if(clsInstance instanceof GeneralConverter) {
				return true;
			}
		} 
		catch (UnsatisfiedLinkError e) { }
		catch (ClassNotFoundException e) { } 
		catch (InstantiationException e) { } 
		catch (IllegalAccessException e) { }
		catch (RuntimeException e) {}
        catch (Throwable e) {}
		return false;
	}
	
	/**
	 * Return the list of converter packages.
	 * @return the list of converter packages
	 */
	public static ArrayList<String> getConverterPackageList() {
		List<String> classes = ClassSearchUtils.searchClassPath(converterSuperPackage);
		ArrayList<String> classPackages = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isConverter(cls)) {
				String clsPackage = cls.substring(0, cls.lastIndexOf("."));
				if(!classPackages.contains(clsPackage)) {
					classPackages.add(clsPackage);
				}
			}
		}
		Collections.sort(classPackages);
		return classPackages;
	}
	
	/**
	 * Return the list of converters including their package.
	 * @return the list of converters including their package
	 */
	public static ArrayList<String> getConverterFullNameList() {
		List<String> classes = ClassSearchUtils.searchClassPath(converterSuperPackage);
		ArrayList<String> classPackages = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isConverter(cls)) {
				classPackages.add(cls);
			}
		}
		Collections.sort(classPackages);
		return classPackages;
	}
	
	/**
	 * Return the list of converter names.
	 * @return the list of converter names
	 */
	public static ArrayList<String> getConverterNameList() {
		List<String> classes = ClassSearchUtils.searchClassPath(converterSuperPackage);
		ArrayList<String> classNames = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isConverter(cls)) {
				String className = cls.substring(cls.lastIndexOf(".")+1);
				if(!classNames.contains(className)) {
					classNames.add(className);
				}
			}
		}
		Collections.sort(classNames);
		return classNames;		
	}
	
	/**
	 * Prints the list of converters available in SBFC.
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> converterNames = getConverterNameList();
		for(int i=0; i<converterNames.size(); i++) {
			System.out.println(converterNames.get(i));
		}
		
//		ArrayList<String> converterFullNames = getConverterFullNameList();
//		for(int i=0; i<converterFullNames.size(); i++) {
//			System.out.println(converterFullNames.get(i));
//		}
//		
//		ArrayList<String> converterPackageNames = getConverterPackageList();
//		for(int i=0; i<converterPackageNames.size(); i++) {
//			System.out.println(converterPackageNames.get(i));
//		}
		
	}
	
}
