/*
 * $Id: ConverterGUI.java 631 2016-03-09 11:52:19Z niko-rodrigue $
 * $URL: svn+ssh://niko-rodrigue@svn.code.sf.net/p/sbfc/code/trunk/src/org/sbfc/converter/ConverterGUI.java $
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

package org.sbfc.converter;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;

import org.sbfc.converter.gui.AboutDialog;
import org.sbfc.converter.gui.StatusPanel;


/**
 * @author rodrigue
 *
 */
public class ConverterGUI extends JFrame {

/**
   * 
   */
  JFileChooser inputFileChooser = new JFileChooser();
  /**
   * 
   */
  JTextField inputFileTextField = new JTextField();
  /**
   * 
   */
  JButton inputFileButton;
  /**
   * 
   */
  JFileChooser outputFileChooser = new JFileChooser();
  /**
   * 
   */
  JTextField outputFileTextField = new JTextField(40);
  
  /**
   * 
   */
  JButton launchButton;

  public static final String APPLICATION_NAME = "Systems Biology Format Converter";
  
  public static final String APPLICATION_SHORTNAME = "SBFC";
  
  public static final String VERSION = "1.3.7";
  
  public static final String WEBSITE = "http://sbfc.sf.net/";
  
  private ConverterGUI application;
  
  private StatusPanel statusPanel;
  
  
  /**
   * 
   */
  public ConverterGUI() {
    super(APPLICATION_NAME);
    application = this;
    init();
  }

  /**
   * 
   */
  private void init() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    final Container jpanel = getContentPane();        
    jpanel.setLayout(new GridBagLayout());
    
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    JMenu filesMenu = new JMenu("Files");
    menuBar.add(filesMenu);
    
    JMenuItem exitMenuItem = new JMenuItem(new AbstractAction("Exit") {      
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        System.exit(0);
      }
    });
    filesMenu.add(exitMenuItem);
    
    JMenuItem aboutMenuItem = new JMenuItem(new AbstractAction("About") {      
      @Override
      public void actionPerformed(ActionEvent e) {
    	  new AboutDialog(application);
      }
    });
    menuBar.add(aboutMenuItem);

    inputFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

    inputFileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
      public boolean accept(File f) {
        return f.getName().toLowerCase().endsWith(".xml")
            || f.isDirectory();
      }

      public String getDescription() {
        return "XML files";
      }
    });

    int rowNumber = 0;
    
    // creates a constraints object
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2); // insets for all components
    c.gridx = 0; // column 0
    c.gridy = rowNumber; // row 0
    c.ipadx = 10; // increases components width by 10 pixels
    c.ipady = 0; // increases components height by 10 pixels
    c.anchor = GridBagConstraints.WEST;
    jpanel.add(new JLabel("Select input file"), c);
    
    c.gridx = 1; // column 1
    c.ipadx = 300; // increases components width by 300 pixels
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    jpanel.add(inputFileTextField, c);

    AbstractAction inputAction = new AbstractAction("...") {  // TODO - reset the output file name if present
      // This method is called when the button is pressed
      public void actionPerformed(ActionEvent evt) {
        // Perform action...
        int r = inputFileChooser.showOpenDialog(new JFrame());
        if (r == JFileChooser.APPROVE_OPTION) {
          String name = inputFileChooser.getSelectedFile().getName();
          inputFileTextField.setText(inputFileChooser.getSelectedFile().getAbsolutePath());
        }
      }
    };

    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 2; // column 2
    c.ipadx = 0; // increases components width by 0 pixels
    inputFileButton = new JButton(inputAction);
    inputFileButton.setToolTipText("Open a file chooser dialog to select the input file");
    jpanel.add(inputFileButton, c);

    rowNumber++;
    
    c.gridx = 0; // column 0
    c.gridy = rowNumber;
    c.ipadx = 10; // increases components width by 10 pixels
    c.ipady = 0; // increases components height by 10 pixels    
    
    jpanel.add(new JLabel("Select the converter to use"), c); // TODO - reset the output file name if present
    
    final ArrayList<String> converterClassNames = new ArrayList<String>();
    final ArrayList<GeneralConverter> converterInstances = new ArrayList<GeneralConverter>();
    final ArrayList<String> converterDisplayNames = new ArrayList<String>();
    
    for (String converterClass : Converter.converterFullNames) {
      String converterClassSimpleName = converterClass.substring(converterClass.lastIndexOf(".") + 1);
      
      if (converterClass.contains("example") || converterClass.contains("GPML")) {
        // we don't want to include the examples and the GPML converters need a different classpath
        // than the default one.
        continue;
      }
        
      try 
      {
        GeneralConverter instance = (GeneralConverter) Class.forName(converterClass).newInstance();
        converterInstances.add(instance);
        converterClassNames.add(converterClassSimpleName);
        addDisplayName(converterDisplayNames, converterClassSimpleName);

        //System.out.println(instance.getName());
        //System.out.println(instance.getDescription());
      }
      catch (InstantiationException e) {}
      catch (IllegalAccessException e) {}
      catch (ClassNotFoundException e) {}
      catch (UnsatisfiedLinkError e) { }
      catch (Throwable e) {}


    }

    // SBML Level and Version SBML2SBML_L2V1
    if (converterClassNames.contains("SBML2SBML"))
    {
      int sbml2sbmlIndex = converterClassNames.indexOf("SBML2SBML");
      converterClassNames.remove(sbml2sbmlIndex);
      GeneralConverter sbml2sbmlConverter = converterInstances.remove(sbml2sbmlIndex);
      
      converterClassNames.add("SBML2SBML_L3V1");
      converterInstances.add(sbml2sbmlConverter);      
      converterClassNames.add("SBML2SBML_L2V4");
      converterInstances.add(sbml2sbmlConverter);
      converterClassNames.add("SBML2SBML_L2V3");
      converterInstances.add(sbml2sbmlConverter);
      converterClassNames.add("SBML2SBML_L2V1");
      converterInstances.add(sbml2sbmlConverter);
      converterClassNames.add("SBML2SBML_L1V2");
      converterInstances.add(sbml2sbmlConverter);
    }
    
    c.gridx = 1; // column 1
    c.ipadx = 0; // increases components width by 10 pixels
    final JList<String> jList = new JList<String>();
    jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jList.setModel(new ListModel<String>() {
      
      @Override
      public void removeListDataListener(ListDataListener l) {}
      
      @Override
      public int getSize() {
        return converterClassNames.size();
      }
      
      @Override
      public String getElementAt(int index) {
        return converterClassNames.get(index);
      }
      
      @Override
      public void addListDataListener(ListDataListener l) { }
    });
    jpanel.add(jList, c);
    
    rowNumber++;
        
    // Output file row
    c.gridx = 0; // column 0
    c.gridy = rowNumber;    
    jpanel.add(new JLabel("Select output file"), c);
    
    c.gridx = 1; // column 1
    c.ipadx = 300; // increases components width by 300 pixels
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    jpanel.add(outputFileTextField, c);

    AbstractAction outputAction = new AbstractAction("...") {
      // This method is called when the button is pressed
      public void actionPerformed(ActionEvent evt) {
        // Perform action...
        int r = outputFileChooser.showOpenDialog(new JFrame());
        if (r == JFileChooser.APPROVE_OPTION) {
          String name = outputFileChooser.getSelectedFile().getName();
          outputFileTextField.setText(outputFileChooser.getSelectedFile().getAbsolutePath());
        }
      }
    };

    c.fill = GridBagConstraints.NONE;
    c.gridx = 2; // column 2
    c.ipadx = 0; // increases components width by 0 pixels
    c.weightx = 0;
    JButton outputFileButton = new JButton(outputAction);
    outputFileButton.setToolTipText("Open a file chooser dialog to select the output file");
    jpanel.add(outputFileButton, c);

    rowNumber++;
    
    // open output file row
    c.gridx = 0; // column 0
    c.gridy = rowNumber; // row 4    
    jpanel.add(new JLabel("Open output file"), c);
    
    c.gridx = 1; // column 1
    final JCheckBox openOutputCheckBox = new JCheckBox();
    openOutputCheckBox.setSelected(true);
    openOutputCheckBox.setToolTipText("If this checkbox is selected, the content of the ouput file will be displayed in a new window if the conversion is successful.");
    jpanel.add(openOutputCheckBox, c);
    
    
    rowNumber++;
    
    // launch conversion button row.
    c.gridx = 1; // column 1
    c.gridy = rowNumber;
    c.insets = new Insets(10, 10, 10, 10); // insets for all components
    c.anchor = GridBagConstraints.CENTER;
    
    AbstractAction launchAction = new AbstractAction("Convert") {
      // This method is called when the button is pressed
      public void actionPerformed(ActionEvent evt) {
        // Perform action...
        String inputFileName = inputFileTextField.getText();
        
        if (inputFileName == null || inputFileName.trim().length() == 0 || !(new File(inputFileName).exists())) {
          JOptionPane.showMessageDialog(null, "Please, select an input file first.");
          return;
        }

        System.out.println("Input file name = '" + inputFileName + "'");

        int converterIndex = jList.getSelectedIndex();
        
        if (converterIndex == -1) {
          JOptionPane.showMessageDialog(null, "Please, select a converter.");
          return;          
        }
        
        String inputModelType = "SBMLModel";
        
        // TODO - find out input type if possible using GeneralModel.isCorrectType(file) for example
        
        // Using a hack for now as we know the converters present.
        if (converterClassNames.get(converterIndex).startsWith("BioPAX")) {
          inputModelType = "BioPAXModel";
        }
        
        GeneralConverter converter = converterInstances.get(converterIndex);
        
        String outputFileName = Converter.convertFromFile(inputModelType, converterClassNames.get(converterIndex), inputFileName, outputFileTextField.getText());
        
        if (outputFileName != null && openOutputCheckBox.isSelected()) {
          System.out.println("\nOutput file: " + outputFileName);
          
          displayFileinNewWindow(outputFileName);
          statusPanel.setText("Conversion succesful and result saved on file : " + outputFileName);
        } else if (outputFileName != null ) {
          statusPanel.setText("Conversion succesfull and result saved on file : " + outputFileName);
          JOptionPane.showMessageDialog(null, "Conversion succesful and result saved on file: \n" + outputFileName);
        } else {
          int pos = inputFileName.lastIndexOf(".");
          String errorFileName = inputFileName.substring(0,pos) + ".errorLog";
          outputFileName = inputFileName.substring(0,pos) + converter.getResultExtension();
          statusPanel.setText("Conversion FAILED!");
          displayFileinNewWindow(outputFileName);
          JOptionPane.showMessageDialog(null, "Conversion failed ! Check the sbfc-gui.log or \n" + errorFileName + " files for errors if the output file does not contain the error.");
        }
      }
    };
    
    launchButton = new JButton(launchAction);
    jpanel.add(launchButton, c);
    
    rowNumber++;
    
    c.gridx = 0; // column 1
    c.gridy = rowNumber;
    c.gridwidth = 3;
    c.ipadx = 10; // increases components width by 10 pixels
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    statusPanel = new StatusPanel();
    statusPanel.setText("Status bar");
    jpanel.add(statusPanel, c);

  }

  /**
   * Reads the content of a file, using the provided encoding, into a String.
   * 
   * @param path the path of the file to be read
   * @param encoding the encoding to be used to read the file
   * @return the content of the file
   * @throws IOException if an IO error happen.
   */
  static String readFile(String path, Charset encoding) 
      throws IOException 
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
   * @param outputFileName
   */
  private void displayFileinNewWindow(String outputFileName) 
  {
    if ((outputFileName == null) || (!new File(outputFileName).exists())) {
      System.out.println("Output file '" + outputFileName + "' does not seem to exist !");
      return;
    }
    
    try {
      System.out.println("Trying to read '" + outputFileName + "'.");
      String outputFileContent = readFile(outputFileName, Charset.forName("UTF-8"));
      JFrame outputFileJFrame = new JFrame("Conversion result saved on file : " + outputFileName);
      JTextArea outputFileTA = new JTextArea(outputFileContent);
      outputFileTA.setEditable(false);
      
      outputFileJFrame.add(new JScrollPane(outputFileTA));
      outputFileJFrame.setMinimumSize(new Dimension(800, 600));
      outputFileJFrame.setVisible(true);
      
    } catch (IOException e) {
      System.out.println("There was a problem opening the ouput file: '" + e.getMessage() + "'");
    } 
    
  }

  private void addDisplayName(ArrayList<String> converterDisplayNames,
      String converterClassSimpleName) 
  {
    // TODO
    
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    // Redirecting the standard output to a file
    try {
      System.setOut(new PrintStream(new File("sbfc-gui.log")));
    } catch (FileNotFoundException e) {
    }
    
    ConverterGUI gui = new ConverterGUI();
    
    gui.setMinimumSize(new Dimension(600, 300));
    gui.setResizable(true);
    gui.pack();
    gui.setVisible(true);
    
  }
  
  /**
   * Creates and returns a new ConverterGui instance initialized and
   * setup so that it does not exit on close. Once you want to display it, you just need
   * to call the {@link JFrame#setVisible(true)} method.
   * 
   * @return a new ConverterGui instance
   */
  public static ConverterGUI getConverterGuiInstance() {
    final ConverterGUI gui = new ConverterGUI();
    
    gui.setDefaultCloseOperation(HIDE_ON_CLOSE);
        
    // getting the files menu and removing the exit menuitem
    JPanel panel = (JPanel) ((JLayeredPane) ((JRootPane) gui.getComponent(0)).getComponent(1)).getComponent(0);
    
    JMenu filesMenu = (JMenu) ((JMenuBar) panel.getComponent(0)).getComponent(0);    
    filesMenu.remove(0);
    
    // adding a new menuitem to close the windows without exiting the application
    JMenuItem closeMenuItem = new JMenuItem(new AbstractAction("Close") {      
      @Override
      public void actionPerformed(ActionEvent e) {
        gui.setVisible(false);
      }
    });
    filesMenu.add(closeMenuItem);

    
    gui.setMinimumSize(new Dimension(600, 300));
    gui.setResizable(false);
    gui.pack();

    return gui;
  }
}
