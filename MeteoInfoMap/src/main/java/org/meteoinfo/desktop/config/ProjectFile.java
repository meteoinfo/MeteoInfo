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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.meteoinfo.desktop.forms.FrmMain;
import org.meteoinfo.map.MapView;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjectFile {
    // <editor-fold desc="Variables">

    private String _fileName;
    private String _pathFileName;
    private FrmMain _mainForm;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param mainForm Main form
     */
    public ProjectFile(JFrame mainForm) {
        _fileName = "";
        _pathFileName = "";
        _mainForm = (FrmMain)mainForm;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get project file name
     *
     * @return The porject file name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set project file name
     *
     * @param fn File name
     */
    public void setFileName(String fn) {
        _fileName = fn;
    }

    /**
     * Get configure file name
     *
     * @return Configure file name
     */
    public String getConfigureFileName() {
        return _pathFileName;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Save project">

    /**
     * Save project file
     *
     * @param aFile File name
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void saveProjFile(String aFile) throws ParserConfigurationException {
        _fileName = aFile;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("MeteoInfo");
        File af = new File(aFile);
        Attr fn = doc.createAttribute("File");
        Attr type = doc.createAttribute("Type");
        fn.setValue(af.getName());
        type.setValue("projectfile");
        root.setAttributeNode(fn);
        root.setAttributeNode(type);
        doc.appendChild(root);

        //Add language element
        //addLanguageElement(doc, root, Thread.CurrentThread.CurrentUICulture.Name);

        //Add LayersLegend content
        _mainForm.getMapDocument().getMapLayout().updateMapFrameOrder();
        _mainForm.getMapDocument().exportProjectXML(doc, root, _fileName);

        //Add MapLayout content
        _mainForm.getMapDocument().getMapLayout().exportProjectXML(doc, root);

        //Save project file            
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
            //PrintWriter pw = new PrintWriter(new FileOutputStream(aFile));
            FileOutputStream out = new FileOutputStream(aFile);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (TransformerException mye) {
        } catch (IOException exp) {
        }
    }

    // </editor-fold>
    // <editor-fold desc="Load project">
    /**
     * Load project file
     *
     * @param aFile
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void loadProjFile(String aFile) throws ParserConfigurationException, SAXException, IOException {
        _fileName = aFile;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(aFile));

        Element root = doc.getDocumentElement();

        Properties property = System.getProperties();
        String path = System.getProperty("user.dir");
        property.setProperty("user.dir", new File(aFile).getAbsolutePath());

        //Load elements
        //_mainForm.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(true);
        MapView mapView = _mainForm.getMapDocument().getActiveMapFrame().getMapView();
        mapView.setLockViewUpdate(true);
        //LoadLanguageElement(root);         
        //Load map frames content
        _mainForm.getMapDocument().importProjectXML(root);
        _mainForm.getMapDocument().getMapLayout().setMapFrames(_mainForm.getMapDocument().getMapFrames());
        //Load MapLayout content
        _mainForm.getMapDocument().getMapLayout().importProjectXML(root);
        //_mainForm.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(false);
        //_mainForm.getMapDocument().getActiveMapFrame().getMapView().paintLayers();

        property.setProperty("user.dir", path);
    }
    
    // </editor-fold>
    // </editor-fold>
}
