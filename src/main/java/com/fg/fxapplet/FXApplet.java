/*******************************************************************************
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright (c) 2019, Waarp SAS, and individual contributors by the @author
 *  tags. See the COPYRIGHT.txt in the distribution for a full listing of
 *  individual contributors.
 *
 *  All Waarp Project is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  Waarp . If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.fg.fxapplet;

import com.fg.util.FLoader;
import com.fg.util.FadingFilter;
import com.fg.xmleditor.FXDocumentModel;
import com.fg.xmleditor.FXDocumentModelImpl;
import com.fg.xmleditor.FXDoubleView;
import com.fg.xmleditor.FXModelStatusListener;
import com.fg.xmleditor.FXStatusEvent;
import netscape.javascript.JSObject;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class FXApplet extends JApplet {
  public static final String DOC_NAME = "DOC_NAME";
  public static final String XML_SCHEMA = "XML_SCHEMA";
  public static final String XML_SOURCE = "XML_SOURCE";
  public static final String BASE_URL = "BASE_URL";
  public static final String NAMESPACE = "NAMESPACE";
  public static final String ELEMENT = "ELEMENT";
  public static final String XML_DEST = "XML_DEST";
  public static final String CONTENT = "CONTENT";
  public static final String ON_START = "ON_START";
  public static final String ON_LOAD = "ON_LOAD";
  public static final String ON_SAVE = "ON_SAVE";

  static {
    try {
      UIManager
          .setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception ignore) {
    }
  }

  InnerListener innerListener;
  String prmDocName;
  String prmXMLSchema;
  String prmXMLSource;
  String prmBaseURL;
  String prmNamespace;
  String prmElement;
  String prmXMLDest;
  String prmOnStart;
  String prmOnLoad;
  String prmOnSave;
  String prmCookieName;
  String prmCookieValue;
  FXDocumentModel model;
  FXDoubleView dblView;
  JToolBar toolbar;
  JButton btnXsdLoad;
  JButton btnXmlLoad;
  JButton btnReload;
  JButton btnSave;
  JToggleButton btnHorizSplit;
  JToggleButton btnVertSplit;
  ButtonGroup splitGroup;
  JToggleButton btnSync;
  JMenuBar menuBar;
  JMenu menuDocument;
  JMenuItem mXsdLoad;
  JMenuItem mXmlLoad;
  JMenuItem mReload;
  JMenuItem mSave;
  JMenu menuLF;
  private JTextField textField;
  private JTextField textFieldXml;

  public FXApplet() {
    innerListener = new InnerListener();
    model = new FXDocumentModelImpl();
    dblView = new FXDoubleView(model);
    toolbar = new JToolBar();
    splitGroup = new ButtonGroup();
    menuBar = new JMenuBar();
    menuDocument = new JMenu("Document");
    menuLF = new JMenu("Look & Feel");
  }

  public String getDocName() {
    return prmDocName;
  }

  public void setDocName(String docName) {
    prmDocName = normalize(docName);
  }

  public String getXMLSchema() {
    return prmXMLSchema;
  }

  public void setXMLSchema(String xmlSchema) {
    prmXMLSchema = normalize(xmlSchema);
  }

  public String getXMLSource() {
    return prmXMLSource;
  }

  public void setXMLSource(String xmlSource) {
    prmXMLSource = normalize(xmlSource);
  }

  public String getBaseURL() {
    return prmBaseURL;
  }

  public void setBaseURL(String baseURL) {
    prmBaseURL = baseURL == null? null : baseURL.trim();
  }

  public String getNamespace() {
    return prmNamespace;
  }

  public void setNamespace(String namespace) {
    prmNamespace = namespace == null? null : namespace.trim();
  }

  public String getElement() {
    return prmElement;
  }

  public void setElement(String rootElementName) {
    prmElement = normalize(rootElementName);
  }

  public String getXMLDest() {
    return prmXMLDest;
  }

  public void setXMLDest(String xmlDest) {
    prmXMLDest = normalize(xmlDest);
  }

  public void init() {
    setDocName(getParameter("DOC_NAME"));
    setXMLSchema(getParameter("XML_SCHEMA"));
    setXMLSource(getParameter("XML_SOURCE"));
    setBaseURL(getParameter("BASE_URL"));
    setNamespace(getParameter("NAMESPACE"));
    setElement(getParameter("ELEMENT"));
    setXMLDest(getParameter("XML_DEST"));
    prmOnStart = normalize(getParameter("ON_START"));
    prmOnLoad = normalize(getParameter("ON_LOAD"));
    prmOnSave = normalize(getParameter("ON_SAVE"));
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    doLoadXMLDocument(null);
  }

  String normalize(String s) {
    if (s == null) {
      return null;
    } else {
      s = s.trim();
      return s.length() == 0? null : s;
    }
  }

  private void jbInit()
      throws Exception {
    setSize(new Dimension(812, 526));
    model.addModelStatusListener(innerListener);
    getContentPane().add(dblView, "Center");
    getContentPane().add(toolbar, "North");
    ImageIcon imgXsd = FLoader.getIcon(this, "OpenXSD.gif");
    ImageIcon imgXml = FLoader.getIcon(this, "OpenXML.gif");
    ImageIcon imgReload = FLoader.getIcon(this, "Reload.gif");
    ImageIcon imgSave = FLoader.getIcon(this, "Save.gif");
    ImageIcon imgHorizSplit = FLoader.getIcon(this, "HorizSplit.gif");
    ImageIcon imgVertSplit = FLoader.getIcon(this, "VertSplit.gif");
    ImageIcon imgSync = FLoader.getIcon(this, "Sync.gif");
    btnXsdLoad = new Btn(imgXsd, "Load XSD schema");
    btnXmlLoad = new Btn(imgXml, "Load XML Document");
    btnReload = new Btn(imgReload, "Reload XML Document");
    btnSave = new Btn(imgSave, "Save XML Document");
    toolbar.addSeparator();
    btnHorizSplit = new ToggleBtn(imgHorizSplit, "Horizontal Split");
    splitGroup.add(btnHorizSplit);
    btnVertSplit = new ToggleBtn(imgVertSplit, "Vertical Split");
    splitGroup.add(btnVertSplit);
    btnSync = new ToggleBtn(imgSync, "Synchronized Node Selection");
    mReload = new MItem("Reload XML Document", imgReload);
    menuDocument.add(mReload);
    mXsdLoad = new MItem("Load XSD Document", imgXsd);
    menuDocument.add(mXsdLoad);
    mXmlLoad = new MItem("Load XML Document", imgXml);
    menuDocument.add(mXmlLoad);
    mSave = new MItem("Save XML Document", imgSave);
    menuDocument.add(mSave);
    menuBar.add(menuDocument);
    javax.swing.UIManager.LookAndFeelInfo lfi[] =
        UIManager.getInstalledLookAndFeels();
    LookAndFeel lf = UIManager.getLookAndFeel();
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < lfi.length; i++) {
      JRadioButtonMenuItem mi = new JRadioButtonMenuItem(lfi[i].getName());
      group.add(mi);
      if (lfi[i].getClassName().equals(lf.getClass().getName())) {
        mi.setSelected(true);
      }
      mi.addActionListener(innerListener);
      menuLF.add(mi);
    }

    menuBar.add(menuLF);
    menuBar.setBorder(BorderFactory.createEtchedBorder());
    setJMenuBar(menuBar);

    JLabel lblXsdFile = new JLabel("XSD File");
    lblXsdFile.setHorizontalAlignment(SwingConstants.RIGHT);
    lblXsdFile.setPreferredSize(new Dimension(60, 14));
    lblXsdFile.setMinimumSize(new Dimension(60, 14));
    menuBar.add(lblXsdFile);

    textField = new JTextField();
    menuBar.add(textField);
    textField.setColumns(10);

    JLabel lblXmlFile = new JLabel("XML File");
    lblXmlFile.setHorizontalAlignment(SwingConstants.RIGHT);
    lblXmlFile.setPreferredSize(new Dimension(60, 14));
    lblXmlFile.setMinimumSize(new Dimension(60, 14));
    menuBar.add(lblXmlFile);

    textFieldXml = new JTextField();
    menuBar.add(textFieldXml);
    textFieldXml.setColumns(10);
  }

  void doLoadXMLDocument(String xmlContent) {
    xmlContent = normalize(xmlContent);
    if (prmXMLSchema != null) {
      showStatus("Loading XML document. Please wait ...");
      try {
        java.util.List lostElements = null;
        File tmp = new File(prmXMLSchema);
        URL xsdURL = null;
        if (tmp.exists()) {
          xsdURL = tmp.toURI().toURL();
        } else {
          xsdURL = new URL(getCodeBase() + prmXMLSchema);
        }
        if (xmlContent != null) {
          StringReader reader = new StringReader(xmlContent);
          InputSource src = new InputSource(reader);
          src.setSystemId(getXMLBaseURL().toString());
          xmlContent = null;
          lostElements = model.openDocument(xsdURL, src);
        } else if (prmXMLSource != null) {
          tmp = new File(prmXMLSource);
          URL xmlURL = null;
          if (tmp.exists()) {
            xmlURL = tmp.toURI().toURL();
          } else {
            xmlURL = new URL(getCodeBase() + prmXMLSource);
          }
          URLConnection con = xmlURL.openConnection();
          con.connect();
          InputStream in = con.getInputStream();
          try {
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            InputSource src = new InputSource(reader);
            src.setSystemId(getXMLBaseURL().toString());
            lostElements = model.openDocument(xsdURL, src);
          } finally {
            try {
              in.close();
            } catch (Exception exception1) {
            }
            if (con instanceof HttpURLConnection) {
              HttpURLConnection httpConn = (HttpURLConnection) con;
              try {
                httpConn.disconnect();
              } catch (Exception ignore) {
              }
            }
          }
        } else if (prmNamespace != null && prmElement != null) {
          model.newDocument(xsdURL, prmNamespace, prmElement);
        } else if (prmElement != null) {
          model.newDocument(xsdURL, prmElement);
        } else {
          model.newDocument(xsdURL);
        }
        if (lostElements != null) {
          StringBuffer sb = new StringBuffer(
              "Error: The source XML document is invalid.\nThe following elements have not been loaded:");
          for (int i = 0; i < lostElements.size(); i++) {
            sb.append("\n");
            int k = sb.length();
            Node element = (Node) lostElements.get(i);
            sb.append(element.getNodeName());
            for (Node node = element.getParentNode();
                 node != null && !(node instanceof Document);
                 node = node.getParentNode()) {
              sb.insert(k, node.getNodeName() + "/");
            }

          }

          dblView.showErrorMessage(sb.toString());
        }
        if (prmDocName != null) {
          dblView.showInfoMessage(prmDocName);
        }
        showStatus("XML document has been loaded");
      } catch (Exception ex) {
        showStatus("");
        dblView.showErrorMessage(ex.getMessage());
        ex.printStackTrace();
      }
    }
  }

  URL getXMLBaseURL()
      throws Exception {
    if (prmBaseURL != null) {
      return new URL(getCodeBase() + prmBaseURL);
    }
    if (prmXMLSource != null) {
      return new URL(getCodeBase() + prmXMLSource);
    } else {
      return null;
    }
  }

  public void start() {
    if (prmOnStart != null) {
      callJavascriptHandler(prmOnStart);
    }
  }

  void callJavascriptHandler(String methodName) {
    try {
      JSObject win = JSObject.getWindow(this);
      win.call(methodName, new Object[] {
          this
      });
    } catch (Exception ex) {
      dblView.showErrorMessage(ex.toString());
    }
  }

  public void loadXMLDocument(String xmlContent) {
    SwingUtilities.invokeLater(new XMLDocumentLoader(xmlContent));
  }

  public String getXMLDocumentAsText() {
    Document doc = model.getDocument();
    if (doc == null) {
      return "";
    }
    StringWriter writer = new StringWriter();
    try {
      try {
        OutputFormat format = new OutputFormat(doc, "UTF-8", true);
        XMLSerializer serial = new XMLSerializer(writer, format);
        serial.asDOMSerializer();
        serial.serialize(doc);
        String s = writer.toString();
        return s;
      } catch (IOException ex) {
        dblView.showErrorMessage(ex.toString());
        ex.printStackTrace();
      }
      return "";
    } finally {
      try {
        writer.close();
      } catch (Exception ignore) {
      }
    }
  }

  void saveXMLDocument() {
    if (!dblView.hasDocument()) {
      return;
    }
    dblView.stopEditing();
    if (!dblView.isDocValid()) {
      dblView.showErrorMessage("Error: Invalid document can not be saved");
      return;
    }
    prmXMLDest = null;
    if (prmXMLDest == null) {
      String cur = textFieldXml.getText();
      JFileChooser fc = null;
      if (cur != null && !cur.isEmpty()) {
        File parent = new File(cur).getParentFile();
        fc = new JFileChooser(parent);
      } else {
        fc = new JFileChooser();
      }
      int val = fc.showSaveDialog(rootPane);
      if (val == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        textFieldXml.setText(file.getAbsolutePath());
        prmXMLDest = textFieldXml.getText();
        OutputStream out = null;
        try {
          Document doc = model.getDocument();
          if (doc == null) {
            return;
          }
          out = new FileOutputStream(file);
          OutputFormat format = new OutputFormat(doc, "UTF-8", true);
          java.io.Writer writer = new OutputStreamWriter(out);
          XMLSerializer serial = new XMLSerializer(writer, format);
          serial.asDOMSerializer();
          serial.serialize(doc);
          dblView.showErrorMessage("File written");
        } catch (Exception ex) {
          dblView.showErrorMessage(ex.toString());
          ex.printStackTrace();
        } finally {
          try {
            if (out != null) {
              out.close();
            }
          } catch (Exception exception2) {
          }
        }
        return;
      } else {
        dblView.showErrorMessage("Error: XML_DEST parameter is not specified");
        return;
      }
    }
    HttpURLConnection httpConn = null;
    OutputStream out = null;
    BufferedReader reader = null;
    try {
      Document doc = model.getDocument();
      if (doc == null) {
        return;
      }
      URL url = new URL(getCodeBase() + prmXMLDest);
      httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setDoInput(true);
      httpConn.setDoOutput(true);
      httpConn.setUseCaches(false);
      httpConn.setRequestProperty("Content-Type", "text/plain");
      out = httpConn.getOutputStream();
      OutputFormat format = new OutputFormat(doc, "UTF-8", true);
      java.io.Writer writer = new OutputStreamWriter(out);
      XMLSerializer serial = new XMLSerializer(writer, format);
      serial.asDOMSerializer();
      serial.serialize(doc);
      reader =
          new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
      String message;
      String line;
      for (message = ""; (line = reader.readLine()) != null;
           message = message + line + "\n") {
        ;
      }
      dblView.showErrorMessage("Server response: " + message.trim());
    } catch (Exception ex) {
      dblView.showErrorMessage(ex.toString());
      ex.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (Exception exception1) {
      }
      try {
        out.close();
      } catch (Exception exception2) {
      }
      try {
        httpConn.disconnect();
      } catch (Exception ignore) {
      }
    }
  }

  void updateLookAndFeel(String lfName) {
    LookAndFeel lf = UIManager.getLookAndFeel();
    if (lf != null && lf.getName().equals(lfName)) {
      return;
    }
    javax.swing.UIManager.LookAndFeelInfo lfi[] =
        UIManager.getInstalledLookAndFeels();
    for (int i = 0; i < lfi.length; i++) {
      if (lfi[i].getName().equals(lfName)) {
        try {
          dblView.stopEditing();
          UIManager.setLookAndFeel(lfi[i].getClassName());
          SwingUtilities.updateComponentTreeUI(this);
          return;
        } catch (Exception exception) {
        }
      }
    }

  }

  public String getAppletInfo() {
    return "XML Editor Applet. Author: Felix Golubov, WMG 2003.";
  }

  public String[][] getParameterInfo() {
    return (new String[][] {
        new String[] {
            "XML_SCHEMA", "String",
            "Relative URL of XML Schema document. Mandatory parameter."
        }, new String[] {
        "XML_SOURCE", "String",
        "Relative URL of XML document (may be servlet url with query string). Optional parameter. Required for loading existing XML documents."
    }, new String[] {
        "BASE_URL", "String",
        "Relative base URL of XML document. Optional parameter. When not provided, the XML_SOURCE URL is used."
    }, new String[] {
        "NAMESPACE", "String",
        "Namespace of the root XML element. Optional parameter. Required for creating new XML documents."
    }, new String[] {
        "ELEMENT", "String",
        "Name of the root XML element. Optional parameter. Required for creating new XML documents."
    }, new String[] {
        "XML_DEST", "String",
        "Relative URL of a servlet which saves XML documents (may have query string). Mandatory parameter."
    }, new String[] {
        "ON_START", "String",
        "Name of a javascript event handler method, which is called from applet start() method. Optional parameter."
    }, new String[] {
        "ON_LOAD", "String",
        "Name of a javascript event handler method, which is called when \"Load\" button clicked. Optional parameter."
    }, new String[] {
        "ON_SAVE", "String",
        "Name of a javascript event handler method, which is called when \"Save\" button clicked. Optional parameter."
    }
    });
  }

  public void destroy() {
    Thread.yield();
  }

  class Btn extends JButton {

    public Btn(ImageIcon icon, String toolTipText) {
      super(icon);
      setFocusPainted(false);
      setDisabledIcon(FadingFilter.fade(icon));
      setToolTipText(toolTipText);
      setPreferredSize(new Dimension(35, 27));
      setMinimumSize(new Dimension(35, 27));
      setMaximumSize(new Dimension(35, 27));
      addActionListener(innerListener);
      toolbar.add(this, null);
    }
  }

  class ToggleBtn extends JToggleButton {

    public ToggleBtn(ImageIcon icon, String toolTipText) {
      super(icon);
      setFocusPainted(false);
      setDisabledIcon(FadingFilter.fade(icon));
      setToolTipText(toolTipText);
      setPreferredSize(new Dimension(35, 27));
      setMinimumSize(new Dimension(35, 27));
      setMaximumSize(new Dimension(35, 27));
      addActionListener(innerListener);
      toolbar.add(this, null);
    }
  }

  class MItem extends JMenuItem {

    public MItem(String text, ImageIcon icon) {
      super(text);
      if (icon != null) {
        setIcon(icon);
        setDisabledIcon(FadingFilter.fade(icon));
      }
      addActionListener(innerListener);
    }
  }

  class XMLDocumentLoader
      implements Runnable {

    String xmlContent;

    public XMLDocumentLoader(String xmlContent) {
      this.xmlContent = xmlContent;
    }

    public void run() {
      doLoadXMLDocument(xmlContent);
    }
  }

  class InnerListener
      implements ActionListener, FXModelStatusListener {

    InnerListener() {
    }

    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == btnReload || source == mReload) {
        if (prmOnLoad != null) {
          callJavascriptHandler(prmOnLoad);
        } else {
          doLoadXMLDocument(null);
        }
      } else if (source == btnSave || source == mSave) {
        if (prmOnSave != null) {
          callJavascriptHandler(prmOnSave);
        } else {
          saveXMLDocument();
        }
      } else if (source == btnXmlLoad || source == mXmlLoad) {
        String cur = textFieldXml.getText();
        JFileChooser fc = null;
        if (cur != null && !cur.isEmpty()) {
          File parent = new File(cur).getParentFile();
          fc = new JFileChooser(parent);
        } else {
          fc = new JFileChooser();
        }
        int val = fc.showOpenDialog(rootPane);
        if (val == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          textFieldXml.setText(file.getAbsolutePath());
        }
        setXMLSource(textFieldXml.getText());
        doLoadXMLDocument(null);
      } else if (source == btnXsdLoad || source == mXsdLoad) {
        String cur = textField.getText();
        JFileChooser fc = null;
        if (cur != null && !cur.isEmpty()) {
          File parent = new File(cur).getParentFile();
          fc = new JFileChooser(parent);
        } else {
          fc = new JFileChooser();
        }
        int val = fc.showOpenDialog(rootPane);
        if (val == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          textField.setText(file.getAbsolutePath());
        }
        setXMLSchema(textField.getText());
        doLoadXMLDocument(null);
      } else if (source == btnHorizSplit) {
        dblView.setOrientation(1);
      } else if (source == btnVertSplit) {
        dblView.setOrientation(0);
      } else if (source == btnSync) {
        dblView.setSyncSelectNodes(btnSync.getModel().isSelected());
      } else if (source instanceof JRadioButtonMenuItem) {
        updateLookAndFeel(((JRadioButtonMenuItem) source).getText());
      }
    }

    public void newDocumentLoaded(FXStatusEvent e) {
      btnSave.setEnabled(dblView.hasDocument() && dblView.isDocValid());
      mSave.setEnabled(dblView.hasDocument() && dblView.isDocValid());
    }

    public void docValidityStatusChanged(FXStatusEvent e) {
      btnSave.setEnabled(dblView.hasDocument() && dblView.isDocValid());
      mSave.setEnabled(dblView.hasDocument() && dblView.isDocValid());
    }
  }
}
