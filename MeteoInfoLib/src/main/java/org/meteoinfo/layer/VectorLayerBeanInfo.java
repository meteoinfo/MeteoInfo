/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.layer;

import com.l2fprod.common.beans.BaseBeanInfo;

/**
 *
 * @author Yaqiang
 */
public class VectorLayerBeanInfo extends BaseBeanInfo {
    public VectorLayerBeanInfo(){
        super(VectorLayer.class);                
        addProperty("fileName").setCategory("Read only").setReadOnly().setDisplayName("File name");
        addProperty("layerType").setCategory("Read only").setReadOnly().setDisplayName("Layer type");
        addProperty("layerDrawType").setCategory("Read only").setReadOnly().setDisplayName("Layer draw type");
        addProperty("shapeType").setCategory("Read only").setReadOnly().setDisplayName("Shape type");
        addProperty("handle").setCategory("Read only").setReadOnly().setDisplayName("Handle");
        addProperty("layerName").setCategory("Editable").setDisplayName("Layer name");
        addProperty("visible").setCategory("Editable").setDisplayName("Visible");
        addProperty("maskout").setCategory("Editable").setDisplayName("Is maskout");
        //addProperty("transparency").setCategory("Editable").setDisplayName("Transparency Percent");
        addProperty("avoidCollision").setCategory("Editable").setDisplayName("Avoid collision");
    }
}
