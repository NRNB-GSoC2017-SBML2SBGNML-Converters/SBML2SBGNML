/*
 * $Id: ModelSearcher.java 379 2015-07-23 15:33:22Z pdp10 $
 * $URL: svn+ssh://pdp10@svn.code.sf.net/p/sbfc/code/trunk/src/org/util/classlist/ModelSearcher.java $
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

import org.sbfc.converter.models.GeneralModel;


/**
 * This class retrieves the list of models available in SBFC.
 * @author Piero Dalle Pezze
 */
public class ModelSearcher {
	
	private static String modelSuperPackage = "org.sbfc.converter.models";
	
	/**
	 * Return the container package of the SBFC models. Currently, this is org.sbfc.converter.models.
	 * @return the container package of the SBFC models
	 */
	public static String getModelSuperPackage() {
		return modelSuperPackage;
	}
	
	/**
	 * Check whether className is a model
	 * @param className a fully qualified class name including package
	 * @return true if className is a converter
	 */
	private static boolean isModel(String className) {
		try {
			Class<?> cls = Class.forName(className);
			Object clsInstance = (Object) cls.newInstance();
			if(clsInstance instanceof GeneralModel) {
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
	 * Return the list of model packages.
	 * @return the list of model packages
	 */
	public static ArrayList<String> getModelPackageList() {
		List<String> classes = ClassSearchUtils.searchClassPath(modelSuperPackage);
		ArrayList<String> classPackages = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isModel(cls)) {
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
	 * Return the list of models including their package.
	 * @return the list of models including their package
	 */
	public static ArrayList<String> getModelFullNameList() {
		List<String> classes = ClassSearchUtils.searchClassPath(modelSuperPackage);
		ArrayList<String> classPackages = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isModel(cls)) {
				classPackages.add(cls);
			}
		}
		Collections.sort(classPackages);
		return classPackages;
	}
	
	/**
	 * Return the list of model names.
	 * @return the list of model names
	 */
	public static ArrayList<String> getModelNameList() {
		List<String> classes = ClassSearchUtils.searchClassPath(modelSuperPackage);
		ArrayList<String> classNames = new ArrayList<String>();
		for(int i=0; i<classes.size(); i++) {
			String cls = classes.get(i);
			if(isModel(cls)) {
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
	 * Prints the list of models available in SBFC.
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> modelNames = getModelNameList();
		for(int i=0; i<modelNames.size(); i++) {
			System.out.println(modelNames.get(i));
		}
		
//		ArrayList<String> modelFullNames = getModelFullNameList();
//		for(int i=0; i<modelFullNames.size(); i++) {
//			System.out.println(modelFullNames.get(i));
//		}
//		
//		ArrayList<String> modelPackageNames = getModelPackageList();
//		for(int i=0; i<modelPackageNames.size(); i++) {
//			System.out.println(modelPackageNames.get(i));
//		}
		
	}
	
}
