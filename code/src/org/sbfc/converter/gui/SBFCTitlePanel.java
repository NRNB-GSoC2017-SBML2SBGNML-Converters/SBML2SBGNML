/*
 * $Id: SBFCTitlePanel.java 612 2016-02-01 16:43:45Z pdp10 $
 * $URL: svn+ssh://pdp10@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/gui/SBFCTitlePanel.java $
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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import org.sbfc.converter.ConverterGUI;


/**
 * The Class SBFC Title Panel.
 * @author Piero Dalle Pezze
 */
public class SBFCTitlePanel extends JPanel {

	private static final long serialVersionUID = -3374200663848185309L;

	/**
     * Constructor.
     */
    public SBFCTitlePanel() {
		super();
		initComponents();
    }

    /**
     * This method is called from within the constructor to initialise the form.
     */
    private void initComponents() {

	setLayout(new BorderLayout());

	ImageIcon logo = new ImageIcon(
		ClassLoader.getSystemResource("org/sbfc/converter/gui/SBFC_logo-small.png"));
	JPanel logoPanel = new JPanel();
	logoPanel.add(new JLabel("", logo, JLabel.CENTER));
	logoPanel.setBorder(BorderFactory.createEmptyBorder(32, 2, 2, 2));
	add(logoPanel, BorderLayout.WEST);

	JTextPane textPane = new JTextPane();
	textPane.setEditable(false);
	textPane.setOpaque(false);
	
// Not safe to use Desktop.browse as it works 1 time out of 100.. So maybe we can skip this.
// To have this we need to define a class which uses the correct browser by OS. 
//	textPane.setEditorKit(JEditorPane
//		.createEditorKitForContentType("text/html"));
//	textPane.setText("<a href='" + ConverterGUI.WEBSITE + "'>"
//		+ ConverterGUI.WEBSITE + "</a>");

//	textPane.addHyperlinkListener(new HyperlinkListener() {
//	    @Override
//	    public void hyperlinkUpdate(HyperlinkEvent hle) {
//		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle
//			.getEventType())) {
//		    try {
//			 Desktop.browse(hle.getURL().toURI());
//		    } catch (URISyntaxException e) {
//		    }
//		}
//	    }
//	});

	add(textPane, BorderLayout.CENTER);
	try {
	    Document doc = textPane.getDocument();
	    Style styleTitle = textPane.addStyle("Title", null);
	    StyleConstants.setFontSize(styleTitle, 14);
	    StyleConstants.setBold(styleTitle, true);
	    Style styleText = textPane.addStyle("Text", null);
	    StyleConstants.setFontSize(styleText, 12);	    
	 
	    doc.insertString(
			    doc.getLength(),
			    "\n\n" + ConverterGUI.APPLICATION_NAME + " (" + ConverterGUI.APPLICATION_SHORTNAME + ") " + ConverterGUI.VERSION + "\n\n",
			    styleTitle);	    
	    
	    doc.insertString(
		    doc.getLength(),
		          "Website: " + ConverterGUI.WEBSITE + "\n\n"
			    + "Copyright \u00a9 2010-2016 jointly by the following organizations:\n" 
			    + "  1. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK\n"
			    + "  2. The Babraham Institute, Cambridge, UK\n"
			    + "  3. Department of Bioinformatics, BiGCaT, Maastricht University, NL\n\n"
			    + "This library is free software; you can redistribute it and/or modify it\n"
			    + "under the terms of the GNU Lesser General Public License as published by\n"
			    + "the Free Software Foundation. A copy of the license agreement is provided\n"
			    + "in the file named LICENSE.txt included with this software distribution\n"
			    + "and also available online at http://sbfc.sf.net/mediawiki/index.php/License .",
		    styleText);

	} catch (BadLocationException e) {
		// send message to status bar?
	}

    }

}



