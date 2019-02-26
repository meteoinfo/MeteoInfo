/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author wyq
 */
public class AppCollection extends ArrayList<Application> {
    // <editor-fold desc="Variables">
    private String _pluginPath;
    private String _pluginConfigFile;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get plugin path
     * @return Plugin path
     */
    public String getPluginPath(){
        return this._pluginPath;
    }
    
    /**
     * Set plugin path
     * @param value Plugin path
     */
    public void setPluginPath(String value){
        this._pluginPath = value;
    }
    
    /**
     * Get plugin configure file
     * @return Plugin configure file
     */
    public String getPluginConfigFile(){
        return this._pluginConfigFile;
    }
    
    /**
     * Set plugin configure file
     * @param value Plugin configure file
     */
    public void setPluginConfigFile(String value){
        this._pluginConfigFile = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Save plugin configure file
     * @throws ParserConfigurationException 
     */
    public void saveConfigFile() throws ParserConfigurationException{
        this.saveConfigFile(this._pluginConfigFile);
    }
    
    /**
     * Save plugin configure file
     *
     * @param fileName File name
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public void saveConfigFile(String fileName) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();        
        
        //Plugins
        Element pluginsElem = doc.createElement("Applications");
        for (Application app : this){
            Element pluginElem = doc.createElement("Application");
            Attr pluginNameAttr = doc.createAttribute("Name");
            Attr pluginAuthorAttr = doc.createAttribute("Author");
            Attr pluginVersionAttr = doc.createAttribute("Version");
            Attr pluginDescriptionAttr = doc.createAttribute("Description");
            Attr pluginPathAttr = doc.createAttribute("Path");
            Attr pluginClassNameAttr = doc.createAttribute("ClassName");
            Attr pluginIsLoadAttr = doc.createAttribute("IsLoad");
            
            pluginNameAttr.setValue(app.getName());
            pluginAuthorAttr.setValue(app.getAuthor());
            pluginVersionAttr.setValue(app.getVersion());
            pluginDescriptionAttr.setValue(app.getDescription());
            pluginPathAttr.setValue(app.getPath());
            pluginClassNameAttr.setValue(app.getClassName());
            pluginIsLoadAttr.setValue(String.valueOf(app.isLoad()));
            
            pluginElem.setAttributeNode(pluginNameAttr);
            pluginElem.setAttributeNode(pluginAuthorAttr);
            pluginElem.setAttributeNode(pluginVersionAttr);
            pluginElem.setAttributeNode(pluginDescriptionAttr);
            pluginElem.setAttributeNode(pluginPathAttr);
            pluginElem.setAttributeNode(pluginClassNameAttr);
            pluginElem.setAttributeNode(pluginIsLoadAttr);
            
            pluginsElem.appendChild(pluginElem);
        } 
        doc.appendChild(pluginsElem);

        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
        } catch (TransformerException mye) {
        } catch (IOException exp) {
        }
    }

    /**
     * Load plugin configure file
     *
     * @param fileName File name
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void loadConfigFile(String fileName) throws ParserConfigurationException, SAXException, IOException { 
        if (!new File(fileName).exists())
            return;
        
        this._pluginConfigFile = fileName;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        InputSource is = new InputSource(br);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);    
        try {                        
            //Plugins
            this.clear();
            Element pluginsElem = doc.getDocumentElement();
            NodeList pluginNodeList =  pluginsElem.getElementsByTagName("Application");
            for (int i = 0; i < pluginNodeList.getLength(); i++){
                Node pluginNode = pluginNodeList.item(i);
                Application plugin = new Application();
                NamedNodeMap attrs = pluginNode.getAttributes();
                plugin.setName(attrs.getNamedItem("Name").getNodeValue());
                plugin.setAuthor(attrs.getNamedItem("Author").getNodeValue());
                plugin.setVersion(attrs.getNamedItem("Version").getNodeValue());
                plugin.setDescription(attrs.getNamedItem("Description").getNodeValue());
                String path = attrs.getNamedItem("Path").getNodeValue();
                plugin.setPath(path);
                plugin.setClassName(attrs.getNamedItem("ClassName").getNodeValue());
                plugin.setLoad(Boolean.parseBoolean(attrs.getNamedItem("IsLoad").getNodeValue()));
                this.add(plugin);
            }
        } catch (Exception e) {
        }
    }
    // </editor-fold>
}
