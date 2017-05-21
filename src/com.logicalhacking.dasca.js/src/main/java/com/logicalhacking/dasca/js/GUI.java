/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.js;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.ssa.JavaScriptInvoke;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Statement.Kind;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;


public class GUI {
    private final String iconDir="/icons/";
    private JFrame frmSapJsAnalyser;
    private JTextField textField;
    public File file;
    public String items[] = null;
    public DefaultListModel lm = new DefaultListModel();
    public DefaultListModel lm2 = new DefaultListModel();
    public DefaultListModel flm = new DefaultListModel();
    public DefaultListModel nodel = new DefaultListModel();
    public DefaultListModel sliceVar = new DefaultListModel();
    private JTextField textSDG;
    private JTextField search;
    public List<String> slices = new ArrayList<String>();
    public List<String> ssa = new ArrayList<String>();
    private List<String> codes = new ArrayList<String>();
    public SDG sdg;
    public String slice;
    public Date date;
    public DateFormat dateFormat;
    public boolean selected = false;
    public int selec = 0;
    Collection<Statement> collection;
    public Statement st;
    public String hallo;
    public SymbolTable symtab;
    public String command = "";
    public boolean foos = false;
    public boolean function = false;
    public boolean functionExtended = false;
    public boolean foundInput = false;
    public List<String> input = new ArrayList<String>();
    public List<String> html = new ArrayList<String>();
    public List<String> inputFields = new ArrayList<String>();
    public List<String> transfer = new ArrayList<String>();
    public List<String> fsi = new ArrayList<String>();
    public List<String> list = new ArrayList<String>();
    public String expectedResult = "";
    public List<Item> dummys;
    public String xml;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI window = new GUI();
                    window.frmSapJsAnalyser.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public GUI() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Setting Look and Feel Failed");
        }
        try {
            initialize();
        } catch (WalaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() throws WalaException {

        frmSapJsAnalyser = new JFrame();
        frmSapJsAnalyser.setResizable(false);
        frmSapJsAnalyser.setIconImage(Toolkit.getDefaultToolkit().getImage(
                                          GUI.class.getResource(iconDir+"logo.png")));
        frmSapJsAnalyser.getContentPane().setBackground(new Color(255, 215, 0));
        frmSapJsAnalyser.setTitle("SAP JS Analyser @ V 1.0");
        frmSapJsAnalyser.setBounds(100, 100, 1189, 793); 
        frmSapJsAnalyser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmSapJsAnalyser.getContentPane().setLayout(null);
        // JDialog.setDefaultLookAndFeelDecorated(true);
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBorder(null);
        tabbedPane.setBounds(10, 21, 1143, 700);
        frmSapJsAnalyser.getContentPane().add(tabbedPane);

        JPanel panelMain = new JPanel();
        tabbedPane.addTab(
            "Main",
            new ImageIcon(GUI.class
                          .getResource(iconDir+"application_home.png")),
            panelMain, null);
        panelMain.setLayout(null);

        final JTextArea mainConsole = new JTextArea();
        mainConsole.setLineWrap(true);
        JScrollPane scrollPanel1 = new JScrollPane(mainConsole);

        scrollPanel1.setBounds(10, 66, 1021, 600);
        panelMain.add(scrollPanel1);

        JButton btnNewButton = new JButton("Clear");
        btnNewButton.setIcon(new ImageIcon(GUI.class
                                           .getResource(iconDir+"cancel.png")));
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConsole.setText("");
            }
        });
        btnNewButton.setBounds(1041, 636, 85, 23);
        panelMain.add(btnNewButton);

        JPanel panelEditor = new JPanel();
        tabbedPane.addTab(
            "Editor",
            new ImageIcon(GUI.class
                          .getResource(iconDir+"EditedItem.png")),
            panelEditor, null);
        tabbedPane.setEnabledAt(1, true);
        panelEditor.setLayout(null);

        final JTextArea textEditor = new JTextArea();
        textEditor.setBounds(10, 11, 976, 620);
        JScrollPane scrollPane = new JScrollPane(textEditor);
        scrollPane.setBounds(10, 11, 976, 620);

        JPanel panelSDG = new JPanel();
        tabbedPane
        .addTab("SDG",
                new ImageIcon(
                    GUI.class
                    .getResource(iconDir+"application_view_detail.png")),
                panelSDG, null);
        panelSDG.setLayout(null);

        panelEditor.add(scrollPane);

        final JLabel lblEditorIcon = new JLabel("");
        lblEditorIcon.setBounds(67, 640, 42, 28);
        panelEditor.add(lblEditorIcon);

        final JLabel lblEditorStatus = new JLabel("");
        lblEditorStatus.setBounds(119, 640, 915, 21);
        panelEditor.add(lblEditorStatus);

        JPanel panelRhino = new JPanel();
        tabbedPane
        .addTab("Rhino",
                new ImageIcon(
                    GUI.class
                    .getResource(iconDir+"application_osx_terminal.png")),
                panelRhino, null);
        panelRhino.setLayout(null);

        final JLabel lblRhinoStatus = new JLabel("");
        lblRhinoStatus.setBounds(990, 442, 48, 64);
        panelRhino.add(lblRhinoStatus);

        final JTextArea textPane_1 = new JTextArea();
        textPane_1.setBounds(10, 350, 956, 269);
        panelRhino.add(textPane_1);

        JButton btnSave = new JButton("Save");
        btnSave.setIcon(new ImageIcon(GUI.class
                                      .getResource(iconDir+"Save.png")));
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    FileWriter writer;
                    writer = new FileWriter(file, false);
                    textEditor.write(writer);
                    writer.close();
                    mainConsole.append(dateFormat.format(date) + ":Changes in"
                                       + file.getName() + "sucessful" + "\n");
                    lblEditorIcon.setIcon(new ImageIcon(GUI.class
                                                        .getResource(iconDir+"ok.png")));
                    lblEditorStatus.setText("Changes saved successful @ file "
                                            + file.getName());
                } catch (IOException e) {
                    mainConsole.append(dateFormat.format(date)
                                       + ":Cant save changes in " + file.getName() + "\n");
                    e.printStackTrace();
                }
            }
        });
        btnSave.setBounds(996, 572, 100, 23);
        panelEditor.add(btnSave);

        JButton btnNewButton_6 = new JButton("SaveAs");
        btnNewButton_6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                final JFileChooser fc = new JFileChooser();

                fc.setCurrentDirectory(file);
                int returnVal = fc.showSaveDialog(frmSapJsAnalyser);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File temp = fc.getSelectedFile();
                    try {
                        FileWriter writer;
                        writer = new FileWriter(temp, false);
                        textEditor.write(writer);
                        writer.close();
                        mainConsole.append(dateFormat.format(date)
                                           + ":Changes in " + file.getName()
                                           + "successful saved at " + temp.getPath()
                                           + "\n");
                        lblEditorIcon.setIcon(new ImageIcon(GUI.class
                                                            .getResource(iconDir+"ok.png")));
                        lblEditorStatus.setText("Saved @" + temp.getPath());

                    } catch (IOException e) {
                        mainConsole.append(dateFormat.format(date)
                                           + ":Cant save changes in " + file.getName()
                                           + "\n");
                        e.printStackTrace();

                    }

                }
            }
        });

        JPanel panelSlice = new JPanel();
        tabbedPane
        .addTab("Slice",
                new ImageIcon(
                    GUI.class
                    .getResource(iconDir+"application_view_icons.png")),
                panelSlice, null);
        panelSlice.setLayout(null);
        final JLabel labelSeed = new JLabel("NULL");
        labelSeed.setBounds(22, 11, 1106, 33);
        panelSlice.add(labelSeed);
        btnNewButton_6.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"SaveAndNew.png")));
        btnNewButton_6.setBounds(996, 603, 100, 23);
        panelEditor.add(btnNewButton_6);

        Box verticalBox_2 = Box.createVerticalBox();
        verticalBox_2.setBounds(996, 42, 132, 154);
        panelEditor.add(verticalBox_2);

        JLabel lblNewLabel_5 = new JLabel("Detected InputFields:");
        verticalBox_2.add(lblNewLabel_5);

        final JLabel lblInput1 = new JLabel("");
        verticalBox_2.add(lblInput1);

        final JLabel lblInput2 = new JLabel("");
        verticalBox_2.add(lblInput2);

        final JLabel lblInput3 = new JLabel("");
        verticalBox_2.add(lblInput3);

        final JButton btnNewButton_12 = new JButton("Replace Input");
        btnNewButton_12.setIcon(new ImageIcon(GUI.class
                                              .getResource(iconDir+"ImportExport.jpg")));
        // btnNewButton_12.setVisible(foundInput);
        btnNewButton_12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                searchInput(textEditor);
            }
        });
        btnNewButton_12.setBounds(996, 474, 132, 23);
        panelEditor.add(btnNewButton_12);

        final JLabel lblInput4 = new JLabel("");
        verticalBox_2.add(lblInput4);

        JButton btnNewButton_13 = new JButton("ExtractJS");
        btnNewButton_13.setIcon(new ImageIcon(GUI.class
                                              .getResource(iconDir+"Highlight.png")));
        btnNewButton_13.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int temp = 0;
                boolean isHtml = false;
                for (int i = 0; i < html.size(); i++) {
                    if (html.get(i).contains("<html>")) {
                        isHtml = true;
                    }

                }
                if (isHtml) {
                    extractScript(html, textEditor);
                }
                for (int i = 0; i < inputFields.size(); i++) {
                    foundInput = true;
                    btnNewButton_12.setVisible(foundInput);
                    temp++;
                    System.out.println("Temp" + temp + inputFields.get(i));
                }

                if (temp == 0) {

                    lblInput1.setText("No inputs :(");
                }
                if (temp >= 1) {
                    lblInput1.setIcon(new ImageIcon(GUI.class
                                                    .getResource(iconDir+"GreenLed.png")));
                    lblInput1.setText("[1]" + "Name=" + inputFields.get(0));
                }
                if (temp >= 2) {
                    lblInput2.setIcon(new ImageIcon(GUI.class
                                                    .getResource(iconDir+"GreenLed.png")));
                    lblInput2.setText("[2]" + "Name=" + inputFields.get(1));
                }
                if (temp >= 3) {
                    lblInput3.setIcon(new ImageIcon(GUI.class
                                                    .getResource(iconDir+"GreenLed.png")));
                    lblInput3.setText("[3]" + "Name=" + inputFields.get(2));
                }
                if (temp >= 4) {
                    lblInput4.setIcon(new ImageIcon(GUI.class
                                                    .getResource(iconDir+"GreenLed.png")));
                    lblInput4.setText("[4]" + "Name=" + inputFields.get(3));
                }
            }
        });
        btnNewButton_13.setBounds(996, 440, 100, 23);
        panelEditor.add(btnNewButton_13);

        final JLabel rhino_match = new JLabel("");
        rhino_match.setBounds(10, 630, 956, 29);
        panelRhino.add(rhino_match);

        JButton btnEvalall = new JButton("EvalAll");
        btnEvalall.setIcon(new ImageIcon(GUI.class
                                         .getResource(iconDir+"Function.png")));
        btnEvalall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = "";
                for (int i = 0; i < input.size(); i++) {
                    command += input.get(i);
                    System.out.println("Input=" + input.get(i));
                }
                try {
                    System.out.println(command);
                    textPane_1.setText(Main.evalState(command));
                    if (Main.evalState(command) == "Evaluation is Null see Stacktrace for further information or call 911") {
                        lblRhinoStatus.setIcon(new ImageIcon(GUI.class
                                                             .getResource(iconDir+"Error.png")));
                    } else
                        lblRhinoStatus.setIcon(new ImageIcon(GUI.class
                                                             .getResource(iconDir+"ok.png")));

                    mainConsole.append(dateFormat.format(new Date())
                                       + ":sucessfull Evaluated mit Rhino" + "\n");
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":Rhino FileNotfound " + "\n");
                    e1.printStackTrace();
                } catch (ScriptException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":script exception Rhino" + "\n");
                    e1.printStackTrace();
                }
            }

        });
        btnEvalall.setBounds(996, 410, 100, 23);
        panelEditor.add(btnEvalall);

        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null,
                                                null, null, null));
        horizontalBox.setBounds(10, 11, 725, 42);
        panelMain.add(horizontalBox);

        JButton btnOpen = new JButton("Open File");
        btnOpen.setIcon(new ImageIcon(GUI.class
                                      .getResource(iconDir+"open_folder.png")));
        horizontalBox.add(btnOpen);

        textField = new JTextField();
        horizontalBox.add(textField);
        textField.setColumns(10);

        JButton btnSdg = new JButton("Analyse");
        btnSdg.setIcon(new ImageIcon(GUI.class
                                     .getResource(iconDir+"chart_line.png")));
        horizontalBox.add(btnSdg);

        JCheckBox boxAll = new JCheckBox("All Nodes");
        horizontalBox.add(boxAll);
        boxAll.setToolTipText("This will print all available Nodes");

        JPanel panelSliceEdt = new JPanel();
        tabbedPane.addTab(
            "Slice Edit",
            new ImageIcon(GUI.class
                          .getResource(iconDir+"VectorFileTemplate.png")),
            panelSliceEdt, null);
        panelSliceEdt.setLayout(null);

        final JTextPane textSliceEdt = new JTextPane();
        textSliceEdt.setBounds(10, 11, 1000, 648);
        panelSliceEdt.add(textSliceEdt);

        JSeparator separator = new JSeparator();
        horizontalBox.add(separator);

        JLabel lblNewLabel_2 = new JLabel("        ");
        lblNewLabel_2.setForeground(Color.BLACK);

        horizontalBox.add(lblNewLabel_2);

        JButton btnNewButton_2 = new JButton("AutoReturn");
        btnNewButton_2.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"DebugStepOver.png")));
        btnNewButton_2
        .setToolTipText("Performed a automatic slice from return Statement");
        horizontalBox.add(btnNewButton_2);

        JLabel lblNewLabel_4 = new JLabel("     ");
        horizontalBox.add(lblNewLabel_4);

        JButton btnNewButton_7 = new JButton("ReplaceEvaluated");
        btnNewButton_7
        .setToolTipText("only useable  after evaluation and if there is a return statement");
        btnNewButton_7.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"ImportExport.png")));
        horizontalBox.add(btnNewButton_7);

        final JList textPane = new JList();

        textPane.setBounds(10, 35, 956, 279);
        panelRhino.add(textPane);
        btnNewButton_7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    searchReturn(textSliceEdt);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (ScriptException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        btnNewButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                try {
                    autoSliceReturn(mainConsole, labelSeed, textSliceEdt);
                } catch (WalaException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        boxAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AbstractButton abstractButton = (AbstractButton) e.getSource();
                selected = abstractButton.getModel().isSelected();
                mainConsole.append(dateFormat.format(date)
                                   + ":SDG Option changed to Full" + "\n");
            }
        });
        btnSdg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doAnalyze(mainConsole);
                } catch (WalaException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                getFileChooser(mainConsole, textEditor);
            }
        });

        final JList liste = new JList();
        liste.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                liste.getSelectedValue();
                if (liste.getSelectedValue() != null) {

                    slice = liste.getSelectedValue().toString();
                    textSDG.setText(slice);
                    int temp = slice.indexOf("|");
                    hallo = slice.substring(temp + 1);
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":Selected Statement:" + hallo + "\n");
                }
            }
        });
        liste.setModel(lm);
        JScrollPane list_1 = new JScrollPane(liste);
        list_1.setBounds(10, 11, 926, 650);
        panelSDG.add(list_1);

        textSDG = new JTextField();
        textSDG.setBackground(Color.LIGHT_GRAY);
        textSDG.setEditable(false);
        textSDG.setBounds(946, 41, 182, 55);
        panelSDG.add(textSDG);
        textSDG.setColumns(10);

        JLabel lblSelectedNode = new JLabel("Selected Node");
        lblSelectedNode.setBounds(946, 24, 81, 14);
        panelSDG.add(lblSelectedNode);

        Component horizontalStrut = Box.createHorizontalStrut(20);
        horizontalStrut.setBounds(946, 107, 182, 24);
        panelSDG.add(horizontalStrut);

        JLabel lblSelectedSeedstatement = new JLabel("Selected SeedStatement:");
        lblSelectedSeedstatement.setBounds(22, 0, 174, 24);
        panelSlice.add(lblSelectedSeedstatement);

        JList listSlice = new JList();
        listSlice.setBounds(11, 72, 929, 297);
        panelSlice.add(listSlice);
        listSlice.setModel(lm2);

        JLabel lblComputedSlice = new JLabel("Computed Slice:");
        lblComputedSlice.setBounds(10, 55, 163, 14);
        panelSlice.add(lblComputedSlice);

        final JList list_2 = new JList();
        list_2.setBounds(11, 380, 929, 279);
        panelSlice.add(list_2);
        list_2.setModel(sliceVar);

        JPanel panel_2 = new JPanel();
        panel_2.setBackground(Color.LIGHT_GRAY);
        panel_2.setBorder(new TitledBorder(null, "Slice Opt",
                                           TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(951, 72, 163, 587);
        panelSlice.add(panel_2);
        panel_2.setLayout(null);

        JButton btnNewButton_3 = new JButton("Generate SliceFile");
        btnNewButton_3.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"application_home.jpg")));
        btnNewButton_3.setBounds(10, 24, 143, 23);
        panel_2.add(btnNewButton_3);

        final JTextPane txtSliceStatus = new JTextPane();
        txtSliceStatus.setBounds(10, 58, 143, 42);
        panel_2.add(txtSliceStatus);
        txtSliceStatus.setBackground(SystemColor.scrollbar);

        Component horizontalStrut_1 = Box.createHorizontalStrut(20);
        horizontalStrut_1.setBounds(10, 404, 143, 12);
        panel_2.add(horizontalStrut_1);

        JButton btnNewButton_5 = new JButton("ReplaceVars");
        btnNewButton_5.setBounds(20, 427, 124, 25);
        panel_2.add(btnNewButton_5);
        btnNewButton_5.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"Cut.png")));

        JCheckBox chckbxNewCheckBox = new JCheckBox("return foo");
        chckbxNewCheckBox
        .setToolTipText("Insert return foo; at the End of Code");
        chckbxNewCheckBox.setBounds(29, 459, 124, 23);
        panel_2.add(chckbxNewCheckBox);

        JCheckBox chckbxNewCheckBox_1 = new JCheckBox("Only function");
        chckbxNewCheckBox_1.setToolTipText("Only create function body");
        chckbxNewCheckBox_1.setBounds(29, 485, 124, 23);
        panel_2.add(chckbxNewCheckBox_1);

        JButton btnNewButton_8 = new JButton("ReplaceAdd");
        btnNewButton_8.setBounds(22, 515, 122, 25);
        panel_2.add(btnNewButton_8);
        btnNewButton_8.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"Cut.png")));

        JButton btnNewButton_1 = new JButton("Clear");
        btnNewButton_1.setBounds(31, 551, 122, 25);
        panel_2.add(btnNewButton_1);
        btnNewButton_1.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"cancel.png")));
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                clear(mainConsole);
            }
        });
        btnNewButton_8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                searchAndReplaceAdd();
            }
        });
        chckbxNewCheckBox_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AbstractButton abstractButton = (AbstractButton) arg0
                                                .getSource();
                function = abstractButton.getModel().isSelected();
                System.out.println(function);
            }
        });
        chckbxNewCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AbstractButton abstractButton = (AbstractButton) arg0
                                                .getSource();
                foos = abstractButton.getModel().isSelected();
                System.out.println(foos);

            }
        });
        btnNewButton_5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                sliceVar.removeAllElements();
                CGNode nodi = null;
                boolean lock = false;
                HashMap<Integer, SSAInstruction> instructions = new HashMap<Integer, SSAInstruction>();
                for (Statement stmt : collection) {
                    if (lock == false) {
                        nodi = stmt.getNode();
                        IR ir = nodi.getIR();

                        SSACFG cfg = ir.getControlFlowGraph();
                        Iterator<ISSABasicBlock> cfgIt = cfg.iterator();
                        SSAInstruction ssaInstr = null;
                        while (cfgIt.hasNext()) {
                            ISSABasicBlock ssaBb = cfgIt.next();

                            Iterator<SSAInstruction> ssaIt = ssaBb.iterator();

                            while (ssaIt.hasNext()) {
                                ssaInstr = ssaIt.next();
                                instructions.put(ssaInstr.getDef(), ssaInstr);

                            }
                        }
                    }
                    lock = true;
                }

                replaceVars(nodi, instructions, mainConsole);
                getCodeSlice(getCodes());
                System.out
                .println("::::::::::::::::::::::::::::::::::::::::::::::::::::");

            }
        });
        btnNewButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    String temp;
                    ArrayList<String> tempList = new ArrayList<String>();
                    tempList = Main.getSlices(collection);

                    temp = Main.doWriteFile(tempList);
                    txtSliceStatus.setText(temp);
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":SliceFile created " + "\n");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":Cant create SliceFile" + "\n");
                    e1.printStackTrace();
                } catch (ScriptException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":Cant create SliceFile ScriptException" + "\n");
                    e1.printStackTrace();
                }

            }
        });

        search = new JTextField();
        search.setBounds(946, 134, 182, 25);
        panelSDG.add(search);
        search.setColumns(10);

        JButton btnSearch = new JButton("Search");
        btnSearch.setIcon(new ImageIcon(GUI.class
                                        .getResource(iconDir+"Search.png")));
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Search in SDGPanel for all Statements containing input from
                // searchField
                String temp = search.getText();
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Search for " + temp + " in SDG " + "\n");

                if (temp != "") {
                    lm.removeAllElements();
                    for (int i = 0; i < sdg.getMaxNumber(); i++) {

                        if ((sdg.getNode(i).toString().contains(temp) == true)
                                && (sdg.getNode(i).toString().contains("prolo") == false)
                                && (((Statement)sdg.getNode(i)).getKind() == Kind.NORMAL)) {
                            lm.addElement(sdg.getNode(i).toString()
                                          + "This is Node|" + i);
                            // writeInstructions(sdg.getNode(i));
                        }

                    }

                }
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Searched ! " + "\n");
            }

        });
        btnSearch.setBounds(977, 165, 93, 23);
        panelSDG.add(btnSearch);

        Box verticalBox = Box.createVerticalBox();
        verticalBox.setBackground(SystemColor.activeCaptionBorder);
        verticalBox.setBounds(1124, 294, -166, -127);
        panelSDG.add(verticalBox);

        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(new TitledBorder(null, "Slice", TitledBorder.LEADING,
                                         TitledBorder.TOP, null, null));
        panel.setBounds(949, 211, 159, 256);
        panelSDG.add(panel);
        panel.setLayout(null);

        JButton btnSlice = new JButton("Slice");
        btnSlice.setBounds(28, 25, 91, 25);
        panel.add(btnSlice);
        btnSlice.setIcon(new ImageIcon(GUI.class
                                       .getResource(iconDir+"Refresh.png")));

        JCheckBox chckbxNewCheckBox_2 = new JCheckBox("CreateFuncBody");
        chckbxNewCheckBox_2
        .setToolTipText("Only possible if >1 Functions, in other cases use function body in slice panel");
        chckbxNewCheckBox_2.setBounds(18, 57, 124, 23);
        panel.add(chckbxNewCheckBox_2);

        JButton btnNewButton_4 = new JButton("Show IR");
        btnNewButton_4.setBounds(28, 175, 93, 25);
        panel.add(btnNewButton_4);
        btnNewButton_4.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"VectorFileTemplate.png")));

        JButton btnSymbols = new JButton("Symbols");
        btnSymbols.setBounds(28, 211, 93, 25);
        panel.add(btnSymbols);
        btnSymbols.setIcon(new ImageIcon(GUI.class
                                         .getResource(iconDir+"WarningMessage.png")));
        btnSymbols.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Statement sto;
                selec = Integer.parseInt(hallo);
                sto = (Statement) sdg.getNode(selec);
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Selected Statement in sdg " + selec + "\n");
                symtab = sto.getNode().getIR().getSymbolTable();

                List<String> list = new ArrayList<String>();

                for (int i = 1; i < symtab.getMaxValueNumber(); i++) {
                    if (symtab.getValue(i) != null) {
                        // list.add(symtab.getValue(i).toString() + "\n");
                        list.add(" | Symbol Value=" + symtab.getValueString(i)
                                 + "\n");
                    }

                    if (symtab.isConstant(i) == true) {
                        list.add(" 			|isConstant =" + symtab.isConstant(i)
                                 + "\n");
                    }
                }
                createSymbolWindow(list, "Symbols");
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Symtab created window will be opened" + "\n");

            }
        });
        btnNewButton_4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int temp = slice.indexOf("|");
                Statement stat = (Statement) sdg.getNode(Integer.parseInt(slice
                                             .substring(temp + 1)));
                stat.getNode().getIR().toString();
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Action= getIR from Node " + " Sucessfull" + "\n");

                JFrame newframe = new JFrame();
                newframe.setSize(1280, 1024);
                newframe.setTitle("IR() of Node=" + (slice.substring(temp + 1)));
                JTextArea area = new JTextArea(stat.getNode().getIR()
                                               .toString());
                JScrollPane scroll = new JScrollPane(area);

                newframe.getContentPane().add(scroll);
                newframe.setVisible(true);
            }
        });
        chckbxNewCheckBox_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AbstractButton abstractButton = (AbstractButton) arg0
                                                .getSource();
                functionExtended = abstractButton.getModel().isSelected();

            }
        });
        btnSlice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSlice(mainConsole, textSliceEdt, labelSeed);

            }
        });

        JPanel panelNode = new JPanel();
        tabbedPane.addTab(
            "SSA",
            new ImageIcon(GUI.class
                          .getResource(iconDir+"application.png")),
            panelNode, null);
        panelNode.setLayout(null);

        final JList nodesConsole = new JList();
        nodesConsole.setModel(nodel);
        JScrollPane scrollPanel = new JScrollPane(nodesConsole);
        scrollPanel.setBounds(10, 11, 976, 650);

        panelNode.add(scrollPanel);

        JButton btnEval = new JButton("Evaluate");
        btnEval.setIcon(new ImageIcon(GUI.class
                                      .getResource(iconDir+"ExeFile.png")));
        btnEval.setBounds(990, 166, 138, 30);
        panelRhino.add(btnEval);
        btnEval.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConsole.append(dateFormat.format(new Date())
                                   + ":Trying to evaluate Statement" + "\n");
                System.out.println(codes);
                DefaultListModel defau = new DefaultListModel();

                for (int i = 0; i < codes.size(); i++) {
                    defau.add(i, codes.get(i));
                    System.out.println("filling codes"
                                       + codes.get(i).toString());

                    command += codes.get(i).toString();
                }

                try {
                    System.out.println("Command for evaluation=" + command);
                    String temp = Main.evalState(command);
                    textPane_1.setText(temp);
                    boolean found = false;
                    if (expectedResult != "") {
                        if (temp.contains(expectedResult)) {
                            found = true;
                        } else
                            found = false;

                        rhino_match.setText("Expected String is|"
                                            + expectedResult + "| found in rhino result="
                                            + found);
                    }
                    if (Main.evalState(command) == "Evaluation is Null see Stacktrace for further information or call 911") {
                        lblRhinoStatus.setIcon(new ImageIcon(GUI.class
                                                             .getResource(iconDir+"Error.png")));
                    } else
                        lblRhinoStatus.setIcon(new ImageIcon(GUI.class
                                                             .getResource(iconDir+"ok.png")));

                    mainConsole.append(dateFormat.format(new Date())
                                       + ":sucessfull Evaluated mit Rhino" + "\n");
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":Rhino FileNotfound " + "\n");
                    e1.printStackTrace();
                } catch (ScriptException e1) {
                    // TODO Auto-generated catch block
                    mainConsole.append(dateFormat.format(new Date())
                                       + ":script exception Rhino" + "\n");
                    e1.printStackTrace();
                }

                textPane.setModel(defau);
                try {
                    searchReturn(textSliceEdt);

                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (ScriptException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

        });

        JLabel lblNewLabel_1 = new JLabel("Output:");
        lblNewLabel_1.setFont(new Font("Tele-Marines", Font.PLAIN, 12));
        lblNewLabel_1.setBounds(10, 325, 164, 14);
        panelRhino.add(lblNewLabel_1);

        JButton btnNewButton_9 = new JButton("RhinoVersion");
        btnNewButton_9.setIcon(new ImageIcon(GUI.class
                                             .getResource(iconDir+"help.png")));
        btnNewButton_9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFrame window = new JFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setLocation(500, 500);

                Dialog d;

                // Create a modal dialog
                d = new Dialog(window, "RhinoVersion", true);

                // Use a flow layout
                d.setLayout(new FlowLayout());

                // Create an OK button
                Button ok = new Button("OK");
                ok.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        window.setVisible(false);

                    }
                });

                d.add(new Label(Main.rhinoVersion().get(0)));
                d.add(new Label(Main.rhinoVersion().get(1)));
                URI url;
                try {
                    url = new URI("http://www.mozilla.org/rhino/");
                    d.add(new Label(url.toString()));
                } catch (URISyntaxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                d.add(ok);

                // Show dialog
                d.pack();
                d.setVisible(true);
            }
        });
        btnNewButton_9.setBounds(990, 636, 138, 23);
        panelRhino.add(btnNewButton_9);

        final JLabel lblDevil = new JLabel("");
        lblDevil.setBounds(980, 381, 148, 50);
        panelRhino.add(lblDevil);

        JButton btnNewButton_10 = new JButton("SearchEval");
        btnNewButton_10.setIcon(new ImageIcon(GUI.class
                                              .getResource(iconDir+"Devil-icon.png")));
        btnNewButton_10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (searchEval(mainConsole) == "") {
                    lblDevil.setText("No Eval Found");
                } else {

                    lblDevil.setText(searchEval(mainConsole));
                }
            }
        });
        btnNewButton_10.setBounds(990, 207, 138, 30);
        panelRhino.add(btnNewButton_10);

        JPanel FSI = new JPanel();
        tabbedPane.addTab(
            "FSI",
            new ImageIcon(GUI.class
                          .getResource(iconDir+"BusinessProcess.png")), FSI,
            null);
        tabbedPane.setEnabledAt(7, true);
        FSI.setLayout(null);

        JPanel panel_1 = new JPanel();
        panel_1.setBounds(927, 55, 188, 145);
        panel_1.setBackground(Color.LIGHT_GRAY);
        panel_1.setBorder(new TitledBorder(null, "FSI Configuration",
                                           TitledBorder.LEADING, TitledBorder.TOP, null, null));
        FSI.add(panel_1);
        panel_1.setLayout(null);

        final JLabel lblcount = new JLabel("Count of Items=");
        lblcount.setBounds(10, 76, 168, 14);
        panel_1.add(lblcount);

        final JLabel lblfilename = new JLabel("Filename=");
        lblfilename.setBounds(10, 57, 168, 14);
        panel_1.add(lblfilename);
        final JLabel lblfsiok = new JLabel("");
        lblfsiok.setBounds(111, 32, 46, 14);
        panel_1.add(lblfsiok);

        JButton btnOpen_1 = new JButton("Open");
        btnOpen_1.setBounds(10, 26, 88, 25);
        panel_1.add(btnOpen_1);
        btnOpen_1.setHorizontalAlignment(SwingConstants.RIGHT);
        btnOpen_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int count = 0;
                final JFileChooser fc = new JFileChooser();
                File temp = new File("./example/tim");
                fc.setCurrentDirectory(temp);
                int returnVal = fc.showOpenDialog(frmSapJsAnalyser);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (fc.getSelectedFile().toString().contains(".xml")) {
                        StaXParser read = new StaXParser();
                        File file;
                        file = fc.getSelectedFile();
                        lblfilename.setText("Filename=" + file.getName());
                        xml = file.getName();

                        System.out.println(file.getAbsolutePath());
                        dummys = read.readConfig(file.getAbsolutePath());

                        System.out.println("-------||||||||||PRINT XML FILE"
                                           + file.getAbsolutePath() + "|||||||||-------");
                        for (Item item : dummys) {
                            count++;

                            System.out.println(item);
                        }
                        lblcount.setText(String.valueOf("Count of Items=|"
                                                        + count + "|"));
                        lblfsiok.setIcon(new ImageIcon(GUI.class
                                                       .getResource(iconDir+"chart_line.png")));
                    } else
                        lblfilename.setText("Sry only xml files!");
                }
            }
        });
        btnOpen_1.setIcon(new ImageIcon(GUI.class
                                        .getResource(iconDir+"XmlFile.png")));

        final JTextArea fsiConsole = new JTextArea();
        JScrollPane fsiscroll = new JScrollPane(fsiConsole);
        fsiscroll.setBounds(21, 11, 875, 637);
        FSI.add(fsiscroll);

        JButton btnNewButton_11 = new JButton("Go");
        btnNewButton_11.setIcon(new ImageIcon(GUI.class
                                              .getResource(iconDir+"chart_line.png")));
        btnNewButton_11.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    fillFsi(fsi, fsiConsole);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        btnNewButton_11.setBounds(940, 274, 96, 23);
        FSI.add(btnNewButton_11);

        dateFormat = new SimpleDateFormat("HH:mm:ss");
        date = new Date();

        JLabel lblNewLabel = new JLabel("New label");
        lblNewLabel.setIcon(new ImageIcon(GUI.class
                                          .getResource(iconDir+"logo_Sap.png")));
        lblNewLabel.setBounds(974, 11, 175, 23);
        frmSapJsAnalyser.getContentPane().add(lblNewLabel);

        JLabel lblNewLabel_3 = new JLabel("");
        lblNewLabel_3.setIcon(new ImageIcon(GUI.class
                                            .getResource(iconDir+"sap_logo.png")));
        lblNewLabel_3.setBounds(1073, 732, 79, 25);
        frmSapJsAnalyser.getContentPane().add(lblNewLabel_3);
    }
    public void createSymbolWindow(List<String> list, String titel) {
        JFrame newframe = new JFrame();
        newframe.setSize(1280, 1024);
        newframe.setTitle(titel);
        JTextArea area = new JTextArea(list.toString());
        JScrollPane scrolli = new JScrollPane(area);
        newframe.getContentPane().add(scrolli);
        newframe.setVisible(true);

    }

    public void createDialog(String output, String titel) {
        JDialog dialog = new JDialog(frmSapJsAnalyser, "JDialog", true);
        dialog.setTitle(titel);
        JLabel label = new JLabel();
        label.setText(output);
        dialog.getContentPane().add(label);
        dialog.setVisible(true);

    }

    public int getId(String slice) {
        Pattern p = Pattern.compile(".*?(\\d+).*");
        Matcher m = p.matcher(slice);

        if (m.matches()) {

            System.out.println(m.group(1));

        }
        return Integer.parseInt(m.group(1));
    }

    public void getCodeSlice(List<String> liste) {
        for (int i = 0; i < liste.size(); i++) {

            sliceVar.addElement(liste.get(i));

        }

    }

    public List<String> getCodes() {
        for (int i = 0; i < codes.size(); i++) {
            System.out.println("GET CODES=" + codes.get(i));
        }
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
        for (int i = 0; i < codes.size(); i++) {
            System.out.println("CODES=" + codes.get(i));

        }
    }

    /**
     * This method replaces Variables who are deleted during slice because they
     * are constant. It checks all used Variables who are in the Symboltable but
     * not in Sliced Statements. If a lost variable is found , it will be added
     * to the code list with var "parameter"= " value "; It also checks for code
     * like var x=" ", this will be deleted during slicing but are necessary for
     * evaluation . TODO maybe there are some cases not solved by this method!
     * TODO knownn Bugs: in the output bug duplicated values
     *
     * @param nodi
     * @param instructions
     * @throws UnsupportedOperationException
     */
    private void replaceVars(CGNode nodi,
                             HashMap<Integer, SSAInstruction> instructions, JTextArea mainConsole)
    throws UnsupportedOperationException {
        mainConsole.append(dateFormat.format(new Date())
                           + ":Trying replace vars" + "\n");
        System.out.println("instructions Available");
        for (Entry<Integer, SSAInstruction> e : instructions.entrySet()) {
            int s = e.getKey();
            SSAInstruction ssad = e.getValue();
            System.out.println("HashMap" + s + ":" + ssad);
            // mach was
        }
        if (function == false) {
            for (Statement stat : collection) {
                int zeile = 0;
                String name = null;

                int temp = 0;

                temp = getId(stat.toString());

                SSAInstruction ssaInstruction = instructions.get(temp);
                System.out.println("SSa instruction=" + ssaInstruction);
                System.out.println("Statement:" + stat.toString());

                for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
                    System.out.println("Number of uses="
                                       + ssaInstruction.getNumberOfUses());
                    SymbolTable symtabs = stat.getNode().getIR()
                                          .getSymbolTable();
                    System.out.println("Instruction="
                                       + ssaInstruction.getUse(i) + " GetValue "
                                       + symtabs.getValue(ssaInstruction.getUse(i))

                                       + " IsConstan="
                                       + symtabs.isConstant(ssaInstruction.getUse(i)));
                    if (symtabs.getValue(ssaInstruction.getUse(i)) == null) {
                        System.out.println("Value is null"
                                           + ssaInstruction.toString());

                    }
                    name = getLocalNames(nodi, name, ssaInstruction, i);

                    String birne = (String) getCodes().get(zeile);
                    String apfel = symtabs.getValueString(ssaInstruction
                                                          .getUse(i));
                    int tempes = apfel.indexOf("#");
                    apfel = apfel.substring(tempes + 1);

                    System.out
                    .println("Birne" + birne + " " + "Apfel=" + apfel);
                    if (apfel.isEmpty()) {
                        getCodes().add(
                            zeile,
                            "		 " + "var " + name + "=" + "\"" + apfel
                            + "\"" + ";");
                        zeile++;

                    }
                    if ((birne.contains(apfel) == false)
                            && symtabs.getValue(ssaInstruction.getUse(i)) != null) {

                        getCodes().add(
                            zeile,
                            "		 " + "var " + name + "=" + "\"" + apfel
                            + "\"" + ";");
                        zeile++;
                    } else {

                    }

                    System.out.println(getCodes());

                }

            }
        }
        getCodes().add(0, "function aplejuice() {");
        if (foos == true) {
            getCodes().add(getCodes().size(), "return foo;");
        }
        getCodes().add(getCodes().size(), "}");
        getCodes().add(getCodes().size(), "aplejuice();");
        System.out.println(getCodes());
        mainConsole.append(dateFormat.format(new Date())
                           + ":Replaced vars -----> done " + "\n");
    }

    /**
     * @param nodi
     * @param stat
     * @param name
     * @param ssaInstruction
     */
    public void searchAndReplaceAdd() {

        sliceVar.removeAllElements();
        for (Statement stat1 : collection) {
            CGNode nodi1 = null;
            boolean lock = false;
            HashMap<Integer, SSAInstruction> instructions = new HashMap<Integer, SSAInstruction>();
            for (Statement stmt : collection) {
                if (lock == false) {
                    nodi1 = stmt.getNode();
                    IR ir = nodi1.getIR();

                    SSACFG cfg = ir.getControlFlowGraph();
                    Iterator<ISSABasicBlock> cfgIt = cfg.iterator();
                    SSAInstruction ssaInstr = null;
                    while (cfgIt.hasNext()) {
                        ISSABasicBlock ssaBb = cfgIt.next();

                        Iterator<SSAInstruction> ssaIt = ssaBb.iterator();

                        while (ssaIt.hasNext()) {
                            ssaInstr = ssaIt.next();
                            instructions.put(ssaInstr.getDef(), ssaInstr);

                        }
                    }
                }
                lock = true;
            }
            int zeile = 0;
            String name = null;

            int temp = 0;

            temp = getId(stat1.toString());

            SSAInstruction ssaInstruction = instructions.get(temp);

            if (ssaInstruction.toString().contains("binaryop")) {
                System.out.println("Binary operation");
                SymbolTable symtabs = stat1.getNode().getIR().getSymbolTable();
                System.out.println(symtabs.getValue(ssaInstruction.getUse(0)));
                System.out.println(symtabs.getValue(ssaInstruction.getUse(1)));
                String toReplace = "";
                toReplace += getLocalNames(nodi1, name, ssaInstruction, 0)
                             + "+";
                toReplace += getLocalNames(nodi1, name, ssaInstruction, 1);

                String exactly = toReplace;
                System.out.println(exactly);
                String output = (String.valueOf(
                                     symtabs.getValue(ssaInstruction.getUse(0)))
                                 .substring(1) + (String.valueOf(symtabs
                                                  .getValue(ssaInstruction.getUse(1))).substring(1)));
                System.out.println("output" + output);
                for (int i = 0; i < getCodes().size(); i++) {
                    System.out.println(getCodes().get(i).toString());
                    if (getCodes().get(i).toString().contains(exactly)) {
                        System.out.println("Found in codes @" + i);
                        String tempo = getCodes().get(i).replace(exactly,
                                       output);
                        getCodes().remove(i);
                        getCodes().add(i, tempo);
                    }

                }

            }
        }
        getCodeSlice(getCodes());

    }
    /**
     * Lookup for localNames of variables
     *
     * @param nodi
     * @param name
     * @param ssaInstruction
     * @param i
     *            getUse(param)
     * @return the localName
     */
    public String getLocalNames(CGNode nodi, String name,
                                SSAInstruction ssaInstruction, int i) {
        String[] names = nodi.getIR()
                         .getLocalNames(0, ssaInstruction.getUse(i));
        for (int j = 0; j < names.length; j++) {
            System.out.println("    " + names[j]);
            name = names[j];
        }
        return name;
    }
    /**
     * @param mainConsole
     * @param textEditor
     * @throws HeadlessException
     */
    private void getFileChooser(final JTextArea mainConsole,
                                final JTextArea textEditor) throws HeadlessException {
        clear(mainConsole);
        final JFileChooser fc = new JFileChooser();
        File temp = new File("./example/tim");
        fc.setCurrentDirectory(temp);
        int returnVal = fc.showOpenDialog(frmSapJsAnalyser);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textEditor.removeAll();
            textEditor.setText("");
            file = fc.getSelectedFile();
            textField.setText(file.getName());
            mainConsole.append("Opening: " + file.getName() + "." + "\n");
            mainConsole.append(dateFormat.format(new Date())
                               + ":Ok file opened" + "\n");

            Scanner scanner;
            try {
                scanner = new Scanner((file));
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine();
                    textEditor.append(line + "\n");
                    input.add(line);
                    html.add(line);
                    fsi.add(line);
                    Main.transfer.add(line);

                }
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else {
            mainConsole.append(dateFormat.format(new Date())
                               + ":Cant open file" + "\n");

        }
    }

    /**
     * Clear TextAreas and lists Is called by DoSlice button and by clear button
     * in mainPanel
     *
     * @param mainConsole
     */
    public void clear(final JTextArea mainConsole) {

        for (int u = 0; u < inputFields.size(); u++) {
            inputFields.remove(u);
        }

        for (int i = 0; i < input.size(); i++) {
            input.remove(i);
        }
        for (int j = 0; j < html.size(); j++) {
            html.remove(j);
        }
        for (int k = 0; k < transfer.size(); k++) {
            transfer.remove(k);
        }
        for (int l = 0; l < list.size(); l++) {
            list.remove(l);
        }

        sliceVar.removeAllElements();
        mainConsole.append(dateFormat.format(new Date()) + ":Removed SliceVars"
                           + "\n");
        lm2.removeAllElements();
        mainConsole.append(dateFormat.format(new Date()) + ":Removed lm2"
                           + "\n");
        codes.clear();
        mainConsole.append(dateFormat.format(new Date())
                           + ":Removed Elements from code" + "\n");
        mainConsole.append(dateFormat.format(new Date()) + ":Removed finished"
                           + "\n");
    }
    /**
     * Searches for ReturnStatement but mainly it replaces evaluated variable .
     * Also adds a new line in codes list. with evaluated value . Called by
     * ReplaceEvaluated button in mainPanel.
     *
     * @throws FileNotFoundException
     * @throws ScriptException
     */
    public void searchReturn(final JTextPane textSliceEdt)
    throws FileNotFoundException, ScriptException {
        String out = "";
        Statement statm = (Statement) sdg.getNode(Integer.parseInt(hallo));
        CGNode node = statm.getNode();

        SSACFG cfg = node.getIR().getControlFlowGraph();
        String names = "";

        for (int i = 0; i < cfg.getNumberOfNodes(); i++) {
            BasicBlock bb = cfg.getBasicBlock(i);

            // Iterate over all instructions in the basic block
            List<SSAInstruction> instructions = bb.getAllInstructions();

            for (SSAInstruction ins : instructions) {
                if (ins.toString().contains("return")) {
                    System.out.print(ins);
                    for (int u = 0; u < ins.getNumberOfUses(); u++) {

                        System.out.println(ins.getUse(u));

                        names = getLocalNames(node, names, ins, u);
                        System.out.println(names);
                    }
                }
            }

            if (codes.get(codes.size() - 3).contains("return")) {
                System.out.println("Retrun in letzter reihe");
                int temp = codes.get(codes.size() - 1).indexOf("return");
                codes.get(codes.size() - 1).substring(temp + 1);
                if (codes.get(codes.size() - 1).substring(temp + 1)
                        .contains(names)) {
                    System.out
                    .println("symbol found in return statement in codeliste");

                }

            }
        }

        String command = "";
        System.out.println("Codes " + codes);
        System.out.println("command " + command);
        for (int j = 0; j < codes.size(); j++) {
            command += codes.get(j);
        }
        System.out.println("command" + command);
        String rudi = Main.evalState(command);

        if (names == "") {
            codes.add(codes.size() - 4, "var " + "foo" + "=" + "\"" + rudi
                      + "\";");
        } else {
            codes.add(codes.size() - 4, "var " + names + "=" + "\"" + rudi
                      + "\";");
        }

        codes.remove(codes.size() - 4);
        System.out.println(codes);
        for (int i = 0; i < codes.size(); i++) {

            if (codes.get(i).toString().contains("return foo")) {
                codes.remove(i);
                System.out
                .println("removedb return Statement cos it is evaluated");
            }
            out += codes.get(i) + "\n";
        }
        textSliceEdt.setText(out);
    }
    /**
     * MainFunction in gui. Creates everything necessary for analyzing. Called
     * buy Analyze Button in MainPanel!
     *
     * @param mainConsole
     *            for logging !
     * @throws WalaException
     */
    public void doAnalyze(final JTextArea mainConsole) throws WalaException {
        long start = System.currentTimeMillis();
        String dir = file.getPath();
        String extension = "";

        System.out.println(dir + extension);
        try {

            String name = file.getPath();

            System.out.println(name);
            Main.fileName = name;
            Main.exampeDir = file.getParent() + File.separator;
            Main.fileExtension = "";
            Main.analyzeHTML5("", name, "");
            JSCallGraphUtil
            .setTranslatorFactory(new CAstRhinoTranslatorFactory());
            CallGraph cg3 = Main.createCallgraph("", name, "");
            ImprovedJSCallGraphBuilderUtil.CGBuilderType cgbtype = Main
                    .getCGB();
            PropagationCallGraphBuilder b = ImprovedJSCallGraphBuilderUtil
                                            .makeScriptCGBuilder("", name, cgbtype);
            CallGraph cg = b.makeCallGraph(b.getOptions());
            PointerAnalysis pa = b.getPointerAnalysis();;
            sdg = Main.buildSDG(cg3, pa);

            Collection<CGNode> NodesToAnalyze = new HashSet<CGNode>();
            NodesToAnalyze.addAll(Main.getFunctionNode(cg3, file.getName()));
            System.out.println("NodestoAnalyse" + NodesToAnalyze);
            ssa = Main.printSSA(NodesToAnalyze);
            for (int j = 0; j < ssa.size(); j++) {
                nodel.addElement(ssa.get(j));

            }
            mainConsole.append(dateFormat.format(new Date()) + ":SSA created"
                               + "\n");
            // dumpCG(b.getPointerAnalysis(), CG);

            list = Main.printSDG(sdg, selected);
            for (int i = 0; i < list.size(); i++) {

                lm.addElement(list.get(i));

            }
            mainConsole.append(dateFormat.format(new Date()) + ":SDG created"
                               + "\n");
            mainConsole.append(dateFormat.format(new Date())
                               + ":Analysing took ="
                               + (System.currentTimeMillis() - start) + "ms" + "\n");
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            mainConsole.append(dateFormat.format(new Date())
                               + ":Wrong Argument" + "\n");

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            mainConsole.append(dateFormat.format(new Date()) + ": IO Exception"
                               + "\n");
        } catch (CancelException e1) {
            // TODO Auto-generated catch block
            mainConsole.append(dateFormat.format(new Date())
                               + ":Progress Canceled" + "\n");
        } catch (ScriptException e1) {
            // TODO Auto-generated catch block
            mainConsole.append(dateFormat.format(new Date())
                               + ":Script Exception" + "\n");
        } catch (NoSuchMethodException e1) {
            // TODO Auto-generated catch block
            mainConsole.append(dateFormat.format(new Date())
                               + ":No such Method" + "\n");
        }
    }

    /**
     * This method calls Main.doSlice and creates a Slice from a selected
     * Statement.
     *
     * @param mainConsole
     * @param textSliceEdt
     * @param seedTxt
     * @param labelSeed
     */
    public void doSlice(final JTextArea mainConsole,
                        final JTextPane textSliceEdt, final JLabel labelSeed) {
        clear(mainConsole);
        String chuckTemp = "";

        labelSeed.setText(slice);

        st = (Statement) sdg.getNode(Integer.parseInt(hallo));

        mainConsole.append(dateFormat.format(new Date())
                           + ":Action= Slice from Node " + Integer.parseInt(hallo) + "\n");
        try {
            collection = Main.doSlice(sdg, st);
            slices = Main.dumpSlice(collection, 1);
            List<String> blub = new ArrayList<String>();
            blub = Main.dumpSlice(collection, 2);
            setCodes(Main.dumpSliceCode(collection, functionExtended));
            // char[] ol;
            for (int o = 0; o < blub.size(); o++) {
                // ol = new char[blub.get(o).length()];
                // blub.get(o).getChars(6, blub.get(o).length(), ol, 0);
                chuckTemp += blub.get(o) + "\n";
                //
            }
            textSliceEdt.setText(chuckTemp);
            for (int i = 0; i < slices.size(); i++) {
                lm2.addElement(slices.get(i));
                System.out.println("LM2=" + slices.get(i));
            }
            for (int j = 0; j < codes.size(); j++) {
                sliceVar.addElement(codes.get(j));

            }

            mainConsole.append(dateFormat.format(new Date())
                               + ":Action= Slice from Node " + Integer.parseInt(hallo)
                               + " Sucessfull" + "\n");
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (CancelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ScriptException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    /**
     * This method looks for a return statement in the Code. If it found return
     * statement it will compute a slice from it after that it calls
     * {@link #replaceVars(CGNode, HashMap, JTextArea) }
     *
     * @param mainConsole
     * @param seedTxt
     * @param labelSeed
     * @param textSliceEdt
     * @throws WalaException
     */
    public void autoSliceReturn(final JTextArea mainConsole,
                                final JLabel labelSeed, final JTextPane textSliceEdt) throws WalaException {
        sliceVar.removeAllElements();
        lm2.removeAllElements();
        codes.clear();

        boolean found = false;
        mainConsole.append(dateFormat.format(date)
                           + ":Trying to generate automated slice from return Statment"
                           + "\n");
        doAnalyze(mainConsole);

        for (int i = 0; i < sdg.getMaxNumber(); i++) {
            // possible stat.getArt= ssaReturnStatement
            if ((sdg.getNode(i).toString().contains("return") == true)
                    && (sdg.getNode(i).toString().contains("prolo") == false)
                    && (((Statement)sdg.getNode(i)).getKind() == Kind.NORMAL)
                    && (sdg.getNode(i).toString().contains("ctor") == false)) {
                mainConsole.append(dateFormat.format(date)
                                   + ":Found return Statement" + "\n");
                lm.addElement(sdg.getNode(i).toString() + "This is Node|" + i);

                found = true;
            } else {

            }
        }
        if (found == true) {

            slice = lm.get(lm.size() - 1).toString();
            int temp = slice.indexOf("|");
            hallo = slice.substring(temp + 1);
        } else if (found == false) {
            mainConsole.append(dateFormat.format(date)
                               + ":ATTENTION !No return Statement Found "
                               + " not able to compute Slice" + "\n");

        }
        mainConsole.append(dateFormat.format(date)
                           + ":Return Statemnet Number is" + hallo + "\n");
        doSlice(mainConsole, textSliceEdt, labelSeed);

        CGNode nodi = null;
        boolean lock = false;
        HashMap<Integer, SSAInstruction> instructions = new HashMap<Integer, SSAInstruction>();
        for (Statement stmt : collection) {
            if (lock == false) {
                nodi = stmt.getNode();
                IR ir = nodi.getIR();

                SSACFG cfg = ir.getControlFlowGraph();
                Iterator<ISSABasicBlock> cfgIt = cfg.iterator();
                SSAInstruction ssaInstr = null;
                while (cfgIt.hasNext()) {
                    ISSABasicBlock ssaBb = cfgIt.next();

                    Iterator<SSAInstruction> ssaIt = ssaBb.iterator();

                    while (ssaIt.hasNext()) {
                        ssaInstr = ssaIt.next();
                        instructions.put(ssaInstr.getDef(), ssaInstr);
                        Main.printSSAInstruction(ssaInstr);
                    }
                }
            }
            lock = true;
        }
        sliceVar.removeAllElements();
        replaceVars(nodi, instructions, mainConsole);
        getCodeSlice(getCodes());
    }
    public String searchEval(final JTextArea mainConsole) {
        mainConsole
        .append(dateFormat.format(date) + ":Looking for Eval" + "\n");
        String devil = "";
        List<String> temp = new ArrayList<String>();
        temp = getCodes();
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains("eval")
                    | temp.get(i).toString().contains("EVAL")) {
                System.out.println("eval found in" + temp.get(i).toString());

                devil += temp.get(i).toString();
                mainConsole.append(dateFormat.format(date) + ":Eval found !"
                                   + "\n");
            }

        }
        return devil;
    }
    /**
     * This method looks for Inputfields and replace it with dummy
     * {@link #createDummy() }.
     *
     * @param textEditor
     */
    public void searchInput(JTextArea textEditor) {

        for (int i = 0; i < html.size(); i++) {

            if (html.get(i).contains("getElementById(")
                    && (html.get(i).contains("value") | (html.get(i)
                            .contains(".value);")))) {
                System.out.println("Found  in " + html.get(i) + " ID "
                                   + getInputId(html.get(i), html.get(i).length(), 0));
                String temp = html.get(i);
                temp = temp.substring(0, temp.indexOf('=') + 1);
                temp += createDummy();
                html.remove(i);
                html.add(i, temp);
                System.out.println("temp hinzugef�gt" + temp);

            } else
                System.out.println("No input found");
        }
        textEditor.removeAll();
        textEditor.setText("");

        for (int i = 0; i < html.size(); i++) {
            input.add(html.get(i));
            textEditor.append(html.get(i) + "\n");
            System.out.println(html.get(i) + "\n");
        }

    }
    /**
     * This method creates dummy Input which are used to replace the
     * Inputfields. To each dummy belongs a expected result from
     * {@link #expectedResult(int) }.
     *
     */
    public String createDummy() {

        int l = (int) Math.random();
        l = 0;
        HashMap<Integer, String> dumy = new HashMap<Integer, String>();
        dumy.put(0, "(String.fromCharCode(69,86,65,76))+ \"(\";");
        dumy.put(1, "(\"s\"+\"c\"+\"r\"+\"i\"+\"p\"+\"t\");");
        dumy.put(
            2,
            "(document.write(document.URL.substring(document.URL.indexOf(name=)+5,document.URL.length)));");
        dumy.put(3, "(String.fromCharCode(69,86,65,76))+ \"(\";");
        dumy.put(4, "(String.fromCharCode(69,86,65,76))+ \"(\";");
        dumy.put(5, "(String.fromCharCode(69,86,65,76))+ \"(\";");

        expectedResult = expectedResult(l);
        return dumy.get(l);
    }
    /**
     * This method returns the expectedValue which is identified by a ID. The
     * expectedResult belongs to a dummy from {@link #createDummy() }.
     *
     * @return values
     * @param id
     *            identifier
     */
    public String expectedResult(int id) {
        HashMap<Integer, String> values = new HashMap<Integer, String>();
        values.put(0, "EVAL");
        values.put(1, "script");
        values.put(2, "document.write");
        values.put(3, "eval");
        values.put(4, "eval");
        values.put(5, "eval");

        return values.get(id);

    }
    /**
     * This method extracts the id from a found input field. mode 0 = normal
     * mode 1= sql
     *
     * @param input
     *            the source
     * @param length
     *            the length from the string
     */
    public String getInputId(String input, int length, int mode) {
        char[] dst = new char[length];
        int tempEnde = 0;
        int dstBegin = 0;
        String output = "";
        if (mode == 0) {
            int temp = input.indexOf("getElementById(") + 15;

            tempEnde = input.indexOf("value");
            input.getChars(temp, tempEnde, dst, dstBegin);
        }
        if (mode == 1) {
            int temp = input.indexOf("executeSql") + 10;

            tempEnde = input.indexOf(")");
            input.getChars(temp, tempEnde, dst, dstBegin);

        }

        for (char c : dst) {
            output += c;
        }
        System.out.println(output);
        return output;
    }
    /**
     * This method extract the js part of an html site.
     *
     * @param html
     *            The Source e.g. full html site
     * @param textEditor
     *            for output
     */
    public boolean extractScript(List<String> html, JTextArea textEditor) {
        List<String> temp = new ArrayList<String>();
        boolean inCode = false;
        boolean sema = true;
        boolean foundInput = false;
        String out = "";
        for (int i = 0; i < html.size() - 1; i++) {
            sema = true;
            out = "";
            if (html.get(i).contains("<input type=\"text")) {
                if (html.get(i).contains("id=")) {

                    String pinapple = html.get(i);
                    System.out.println("Found input in html extract name");
                    int tempi = html.get(i).indexOf("id=") + 4;
                    while (sema) {
                        if (pinapple.charAt(tempi) == '"') {
                            System.out.println("isTrue");
                            sema = false;
                        } else {
                            System.out
                            .println("Char=" + pinapple.charAt(tempi));
                            out += pinapple.charAt(tempi);
                            tempi++;
                            foundInput = true;
                        }

                    }
                    System.out.println("Input name=" + out);
                    if (inputFields.contains(out) == false)
                        inputFields.add(out);

                }
            }

            if (html.get(i).contains("<textarea id")) {
                foundInput = true;

                String pinapple = html.get(i);
                System.out.println("Found input in html extract name");
                int tempi = html.get(i).indexOf("id=") + 4;
                while (sema) {
                    if (pinapple.charAt(tempi) == '"') {
                        sema = false;
                    } else {
                        System.out.println("Char=" + pinapple.charAt(tempi));
                        out += pinapple.charAt(tempi);
                        tempi++;
                        foundInput = true;

                    }

                }
                System.out.println("Input name=" + out);
                inputFields.add(out);

            }

            if (html.get(i).contains("<script")) {

                inCode = true;

            }
            System.out.println(html.get(i));
            System.out.println("Cotians" + html.get(i).contains(out));
            System.out.println("In Code=" + inCode);
            System.out.println("foundInput=" + foundInput);
            if (html.get(i).contains(out) && inCode && foundInput) {
                System.out.println(out + " in Code Gefunden" + html.get(i));
                // replaceInput(html.get(i));

            }

            if (html.get(i).contains("<script") == false && inCode == false) {

            }

            if (html.get(i).contains("<script") == false && inCode == true
                    && html.get(i).contains("</script>") == false) {
                if (temp.contains(html.get(i)) == false)
                    temp.add(html.get(i));
            }
            if (html.get(i).contains("</script>") && inCode == true) {

                inCode = false;
            }
        }

        textEditor.removeAll();
        textEditor.setText("");
        for (int u = 0; u < input.size(); u++) {
            input.remove(u);
        }
        // html = new ArrayList<String>(temp);

        for (int j = 0; j < html.size(); j++) {
            html.remove(j);
        }
        for (int i = 0; i < temp.size(); i++) {
            input.add(temp.get(i));
            html.add(temp.get(i));
            textEditor.append(temp.get(i) + "\n");
            System.out.println(temp.get(i) + "\n");
        }

        return true;
    }

    /**
     * This method extracts the parameters of an statement. The statemen musst
     * be from the instance invoke.
     *
     * @param stmt
     *            the source Statement which contains the parameters.
     * @return reciver a list of found parameters
     */

    public List<String> extractFunctionParameter(Statement stmt) {
        sliceVar.removeAllElements();

        CGNode nodi1 = null;

        List<String> reciver = new ArrayList<String>();

        nodi1 = stmt.getNode();
        IR ir = nodi1.getIR();
        String name = "";

        SSACFG cfg = ir.getControlFlowGraph();
        Iterator<ISSABasicBlock> cfgIt = cfg.iterator();
        SSAInstruction ssaInstr = null;
        while (cfgIt.hasNext()) {
            ISSABasicBlock ssaBb = cfgIt.next();

            Iterator<SSAInstruction> ssaIt = ssaBb.iterator();

            while (ssaIt.hasNext()) {
                ssaInstr = ssaIt.next();

                if (ssaInstr instanceof JavaScriptInvoke) {
                    System.out.println(ssaInstr.getClass().getName());
                    JavaScriptInvoke invoki;
                    invoki = (JavaScriptInvoke) ssaInstr;
                    System.out.println("Parametetrs="
                                       + invoki.getNumberOfParameters());
                    System.out.println("GetFunction=" + invoki.getFunction());
                    for (int i = 0; i < invoki.getNumberOfParameters(); i++) {

                        System.out.println("Get Use=" + invoki.getUse(i));
                        name = getLocalNames(nodi1, name, ssaInstr, i);
                        System.out.println(name);
                        if (i > 0 && (name.isEmpty() == false)) {
                            if (reciver.contains(name) == false) {

                                reciver.add(name);
                                System.out.println("Omega=" + name);
                            }
                        }

                    }

                }
            }
        }
        System.out.println("Extract Parameters" + reciver);
        return reciver;
    }
    /**
     * This method extracts the Function name from a given statement.
     *
     * @param input
     *            the statement from which the function name is needed.
     *
     */
    public String getFunctionName(String input) {
        String output = "";
        System.out.println("dies ist output=" + input);
        if (input.contains("Lprologue.js") == false) {
            int tempEnde = 0;
            int dstBegin = 0;
            int tempStart = 0;

            tempEnde = input.lastIndexOf(">");
            tempStart = input.lastIndexOf('/');
            char[] dst = new char[tempEnde - tempStart];
            input.getChars(tempStart + 1, tempEnde, dst, dstBegin);

            for (char c : dst) {

                output += c;

            }

        }
        return output;
    }
    public String checCode(String input, List<String> list) {

        String out = "";
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).contains(input.substring(0, input.length() - 1))) {
                System.out.println(list.get(i).contains(input));
                out = list.get(i);
                System.out.println("Das ist out" + out);
            }

        }
        return out;
    }

    public void fillFsi(List<String> theList, JTextArea fsiConsole)
    throws FileNotFoundException, ScriptException {
        String temp = "";
        searchInputFsi(fsiConsole);
        for (int i = 0; i < fsi.size(); i++) {
            temp += fsi.get(i);
            System.out.println("Temp=" + fsi.get(i));
        }

        // int a = 0;
        // System.out.println("Fill" + fsi.size());
        // for (int i = 0; i < theList.size(); i++) {
        // fsiConsole.append(theList.get(i) + " \n");
        //
        // }
        // System.out.println("list size=" + list.size());
        //
        // for (int i = 0; i < list.size(); i++) {
        // fsiConsole.append(list.get(i) + "\n");
        // if (list.get(i).toString().contains("return") == true
        // & list.get(i).toString().contains("pro") == false
        // & list.get(i).toString().contains("ctor") == false
        // & list.get(i).toString().contains("pro") == false) {
        // fsiConsole.append("\n"
        // + "--------possible slice statements-------" + "\n");
        // fsiConsole.append("Return" + list.get(i) + "\n");
        // a = i;
        //
        // }
        //
        // }
        // getFsiSeed(a, fsiConsole);
    }
    public void getFsiSeed(int node, JTextArea fsiConsole) {
        slice = list.get(node).toString();
        int temp = slice.indexOf("|");
        hallo = slice.substring(temp + 1);
        fsiConsole.append("\n" + "FSI SLice from Number" + hallo);
    }

    public void searchInputFsi(JTextArea fsiConsole)
    throws FileNotFoundException, ScriptException {
        int tempo = 0;
        String out = "";
        long start = System.currentTimeMillis();
        for (int i = 0; i < fsi.size(); i++) {

            if (fsi.get(i).contains("getElementById(")
                    && (fsi.get(i).contains("value") | (fsi.get(i)
                            .contains(".value);")))) {
                fsiConsole.append("Status:Eingabe gefunden" + "\n");
                String temp = fsi.get(i);
                temp = temp.substring(0, temp.indexOf('=') + 1);
                for (Item item : dummys) {
                    if (containsEncodeUri(fsi)) {
                        fsiConsole.append(("Found EncodeUri" + "\n"));
                        String dum = dummys.get(tempo).getInput();

                        try {
                            dum = URLDecoder.decode(dum, "UTF-8");
                            String temp3 = temp + dum;
                            fsiConsole.append("Decoded Dummy" + dum + " \n");

                            fsi.add(i, temp3);
                            fsiConsole.append(temp3 + " hinzugef�gt" + "\n");
                        } catch (UnsupportedEncodingException e) {
                            fsiConsole.append("Fehler beim Dekodieren");
                            e.printStackTrace();
                        }
                    } else {
                        fsiConsole.append("Status:Setzte dummy"
                                          + dummys.get(tempo).toString() + " ein" + "\n");
                        String temp2 = temp + dummys.get(tempo).getInput();
                        fsi.remove(i);
                        fsi.add(i, temp2);
                        fsiConsole.append(temp2 + " hinzugef�gt" + "\n");
                    }
                    for (int l = 0; l < fsi.size(); l++) {
                        out += fsi.get(l);
                        fsiConsole.append(fsi.get(l) + "\n");

                    }
                    String bobo = Main.evalState(out);
                    fsiConsole.append("Ergebniss=" + bobo + "\n");
                    fsiConsole.append("Erwartungswerte=" + "\n"
                                      + dummys.get(tempo).getExp1() + " enhalten ["
                                      + bobo.contains(dummys.get(tempo).getExp1()) + "]"
                                      + " \n" + dummys.get(tempo).getExp2()
                                      + " enhalten ["
                                      + bobo.contains(dummys.get(tempo).getExp2()) + "]"
                                      + "\n" + dummys.get(tempo).getExp3()
                                      + " enhalten ["
                                      + bobo.contains(dummys.get(tempo).getExp3()) + "]"
                                      + "\n" + "Filterung betr�gt="
                                      + isSanni(tempo, bobo) + "%" + "\n");
                    tempo++;
                    fsiConsole
                    .append("------------------------------------------------------------------"
                            + "\n");
                }
                long end = System.currentTimeMillis();
                fsiConsole.append("Test finished" + "\n"
                                  + "Used configuration file=" + xml + "\n"
                                  + "Tested dummys=" + tempo + "\n" + "Time used="
                                  + (end - start) + "ms" + "\n");

            } else if (fsi.get(i).contains("executeSql")) {

            }
            System.out.println("No input found");
        }

    }
    /*
     * Method to find out, if a mathod is a sanitizer or not.
     *
     * @ param input the dummy input
     *
     * @ evaluation the evaluated value
     */
    public double isSanni(int input, String evaluation) {
        double count = 0;
        double ergebniss = 0;
        double temp = 0;
        if (evaluation.contains(dummys.get(input).getExp1())) {
            count++;
        }
        if (evaluation.contains(dummys.get(input).getExp2())) {
            count++;
        }
        if (evaluation.contains(dummys.get(input).getExp3())) {
            count++;
        }
        System.out.println(count);
        ergebniss = (count / 3);
        temp = 100 - (ergebniss * 100);

        System.out.println("Ergebniss ist=" + ergebniss + "   " + temp);
        return temp;
    }
    /*
     *
     */
    public boolean containsEncodeUri(List<String> input) {
        boolean ret = false;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).contains("encodeURI")) {
                System.out.println("Encode in" + input.get(i));
                ret = true;
            }
        }

        return ret;
    }
}
