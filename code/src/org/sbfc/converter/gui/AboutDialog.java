/*
 * $Id: AboutDialog.java 612 2016-02-01 16:43:45Z pdp10 $
 * $URL: svn+ssh://pdp10@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/gui/AboutDialog.java $
 *
 * ==========================================================================
 * This file is part of The System Biology Format Converter (SBFC).
 * Please visit <http://sbfc.sf.net> to have more information about
 * SBFC. 
 * 
 * Copyright (c) 2010-2016 jointly by the following organizations:
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
package org.sbfc.converter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.sbfc.converter.ConverterGUI;


public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 621035395889088992L;

	/**
     * Instantiates a new about dialog.
	 * @param application The SBFC application.
     */
        public AboutDialog(ConverterGUI application) {    	
    	super(application);
        setTitle("About SBFC...");  
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        add(new SBFCTitlePanel(),BorderLayout.CENTER);

        JPanel aboutPanel = new JPanel();
        
        JButton closeButton = new JButton("Close");
        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        aboutPanel.add(closeButton);
        
        container.add(aboutPanel,BorderLayout.SOUTH);
        
        setSize(610,320);
        setLocationRelativeTo(application);
        setResizable(false);
        setVisible(true);
    }
        
    public static void main(String[] args) {
    	new AboutDialog(null);
    }
    
}
