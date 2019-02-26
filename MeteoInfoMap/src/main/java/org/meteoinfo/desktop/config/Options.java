/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.desktop.config;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author yaqiang
 */
public class Options {
    // <editor-fold desc="Variables">

    private String _fileName;
    private Font _textFont = new Font("Simsun", Font.PLAIN, 15);
    private Font _legendFont;
    private String _scriptLanguage = "Groovy"; 
    private boolean showStartMeteoDataDlg = true;
    private Point mainFormLocation = new Point(0, 0);
    private Dimension mainFormSize = new Dimension(1000, 650);
    // </editor-fold>
    // <editor-fold desc="Constructor">
    
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get text font
     *
     * @return Text Font
     */
    public Font getTextFont() {
        return _textFont;
    }

    /**
     * Set text font
     *
     * @param font Text font
     */
    public void setTextFont(Font font) {
        _textFont = font;
    }

    /**
     * Get legend text font
     *
     * @return Legend Text Font
     */
    public Font getLegendFont() {
        return _legendFont;
    }

    /**
     * Get file name
     *
     * @return File name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set legend text font
     *
     * @param font Legend text font
     */
    public void setLegendFont(Font font) {
        _legendFont = font;
    }
    
    /**
     * Get script language name - Groovy or Jython
     *
     * @return Script language name
     */
    public String getScriptLanguage() {
        return this._scriptLanguage;        
    }

    /**
     * Set script language name - Groovy or Jython
     *
     * @param value Script language name
     */
    public void setScriptLanguage(String value) {
        this._scriptLanguage = value;
    }
    
    /**
     * Get if show start meteo data dialog
     * @return Boolean
     */
    public boolean isShowStartMeteoDataDlg(){
        return this.showStartMeteoDataDlg;
    }
    
    /**
     * Set if show start meteo data dialog
     * @param value Boolean
     */
    public void setShowStartMeteoDataDlg(boolean value){
        this.showStartMeteoDataDlg = value;
    }
    
    /**
     * Get main form location
     * @return Main form location
     */
    public Point getMainFormLocation(){
        return this.mainFormLocation;
    }
    
    /**
     * Set main form location
     * @param value Main form location
     */
    public void setMainFormLocation(Point value){
        this.mainFormLocation = value;
    }
    
    /**
     * Get main form size
     * @return Main form size
     */
    public Dimension getMainFormSize(){
        return this.mainFormSize;
    }
    
    /**
     * Set main form size
     * @param value Main form size
     */
    public void setMainFormSize(Dimension value){
        this.mainFormSize = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Save configure file
     *
     * @param fileName File name
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void saveConfigFile(String fileName) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("MeteoInfo");
        File af = new File(fileName);
        Attr fn = doc.createAttribute("File");
        Attr type = doc.createAttribute("Type");
        fn.setValue(af.getName());
        type.setValue("configurefile");
        root.setAttributeNode(fn);
        root.setAttributeNode(type);
        doc.appendChild(root);

        //Path
        Element path = doc.createElement("Path");
        Attr pAttr = doc.createAttribute("OpenPath");
        String userPath = System.getProperty("user.dir");
        pAttr.setValue(userPath);
        path.setAttributeNode(pAttr);
        root.appendChild(path);

        //Font
        Element font = doc.createElement("Font");
        Element textFont = doc.createElement("TextFont");
        Attr nameAttr = doc.createAttribute("FontName");
        Attr sizeAttr = doc.createAttribute("FontSize");
        nameAttr.setValue(_textFont.getFontName());
        sizeAttr.setValue(String.valueOf(_textFont.getSize()));
        textFont.setAttributeNode(nameAttr);
        textFont.setAttributeNode(sizeAttr);
        font.appendChild(textFont);
        Element legendFont = doc.createElement("LegendFont");
        nameAttr = doc.createAttribute("FontName");
        sizeAttr = doc.createAttribute("FontSize");
        nameAttr.setValue(_legendFont.getFontName());
        sizeAttr.setValue(String.valueOf(_legendFont.getSize()));
        legendFont.setAttributeNode(nameAttr);
        legendFont.setAttributeNode(sizeAttr);
        font.appendChild(legendFont);
        root.appendChild(font);
        
        //Script language
        Element scriptlang = doc.createElement("ScriptLanguage");
        Attr slAttr = doc.createAttribute("Language");
        slAttr.setValue(this._scriptLanguage);
        scriptlang.setAttributeNode(slAttr);
        root.appendChild(scriptlang);
        
        //Start up form setting
        Element startForm = doc.createElement("Startup");
        Attr meteoDlgAttr = doc.createAttribute("ShowMeteoDataDlg");
        Attr mfLocationAttr = doc.createAttribute("MainFormLocation");
        Attr mfSizeAttr = doc.createAttribute("MainFormSize");
        meteoDlgAttr.setValue(String.valueOf(this.showStartMeteoDataDlg));
        mfLocationAttr.setValue(String.valueOf(this.mainFormLocation.x) + "," +
                String.valueOf(this.mainFormLocation.y));
        mfSizeAttr.setValue(String.valueOf(this.mainFormSize.width) + "," +
                String.valueOf(this.mainFormSize.height));
        startForm.setAttributeNode(meteoDlgAttr);
        startForm.setAttributeNode(mfLocationAttr);
        startForm.setAttributeNode(mfSizeAttr);
        root.appendChild(startForm);
                
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            
            Properties properties = transformer.getOutputProperties();
            properties.setProperty(OutputKeys.ENCODING, "UTF-8");
            properties.setProperty(OutputKeys.INDENT, "yes");
            properties.setProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperties(properties);
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            FileOutputStream out = new FileOutputStream(fileName);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (TransformerException mye) {
        } catch (IOException exp) {
        }
    }

    /**
     * Load configure file
     *
     * @param fileName File name
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void loadConfigFile(String fileName) throws ParserConfigurationException, SAXException, IOException {
        _fileName = fileName;

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        InputSource is = new InputSource(br);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);

        Element root = doc.getDocumentElement();        
        try {
            //Path
            Node path = root.getElementsByTagName("Path").item(0);
            String currentPath = path.getAttributes().getNamedItem("OpenPath").getNodeValue();
            if (new File(currentPath).isDirectory()) {
                System.setProperty("user.dir", currentPath);
            }

            //Font
            Element font = (Element) root.getElementsByTagName("Font").item(0);
            Node textFont = font.getElementsByTagName("TextFont").item(0);
            String fontName = textFont.getAttributes().getNamedItem("FontName").getNodeValue();
            float fontSize = Float.parseFloat(textFont.getAttributes().getNamedItem("FontSize").getNodeValue());
            this._textFont = new Font(fontName, Font.PLAIN, (int) fontSize);

            Node legendFont = font.getElementsByTagName("LegendFont").item(0);
            fontName = legendFont.getAttributes().getNamedItem("FontName").getNodeValue();
            fontSize = Float.parseFloat(legendFont.getAttributes().getNamedItem("FontSize").getNodeValue());
            this._legendFont = new Font(fontName, Font.PLAIN, (int) fontSize);
            
            //Script language
            Node scriptlang = root.getElementsByTagName("ScriptLanguage").item(0);
            this._scriptLanguage = scriptlang.getAttributes().getNamedItem("Language").getNodeValue();   
            
            //Start up form setting
            Node startForm = root.getElementsByTagName("Startup").item(0);
            this.showStartMeteoDataDlg = Boolean.parseBoolean(startForm.getAttributes().getNamedItem("ShowMeteoDataDlg").getNodeValue());
            String loc = startForm.getAttributes().getNamedItem("MainFormLocation").getNodeValue();
            this.mainFormLocation.x = Integer.parseInt(loc.split(",")[0]);
            this.mainFormLocation.y = Integer.parseInt(loc.split(",")[1]);            
            String size = startForm.getAttributes().getNamedItem("MainFormSize").getNodeValue();
            this.mainFormSize.width = Integer.parseInt(size.split(",")[0]);
            this.mainFormSize.height = Integer.parseInt(size.split(",")[1]);
        } catch (Exception e) {
        }
    }
    // </editor-fold>
}
