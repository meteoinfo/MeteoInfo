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
package org.meteoinfo.map;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class MaskOut {
    // <editor-fold desc="Variables">

    private static MapView _mapView;
    private boolean _setMaskLayer;
    private String _maskLayer;
    private List<String> _layerList;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param aMapView
     */
    public MaskOut(MapView aMapView) {
        _mapView = aMapView;
        _setMaskLayer = false;
        _maskLayer = "";
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Set map view
     * @param mapView Map view
     */
    public void setMapView(MapView mapView){
        _mapView = mapView;
    }
    
    /**
     * Get polygon map layers
     *
     * @return The polygon layer names
     */
    public List<String> getPolygonMapLayers() {
        return _layerList;
    }

    /**
     * Set polygon map layers
     *
     * @param layers The polygon layer names
     */
    public void setPolygonMapLayers(List<String> layers) {
        _layerList = layers;
    }

    /**
     * Get if is mask out
     *
     * @return Boolean
     */
    public boolean isMask() {
        return _setMaskLayer;
    }

    /**
     * Set if is mask out
     *
     * @param istrue Boolean
     */
    public void setMask(boolean istrue) {
        _setMaskLayer = istrue;
        _mapView.paintLayers();
    }

    /**
     * Get mask layer name
     *
     * @return The mask layer name
     */
    public String getMaskLayer() {
        return _maskLayer;
    }

    /**
     * Set mask layer name
     *
     * @param lname The layer name
     */
    public void setMaskLayer(String lname) {
        _maskLayer = lname;
        _mapView.paintLayers();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get layer names
     *
     * @return Layer names
     */
    public static List<String> getLayerNames() {
        List<String> layerNames = new ArrayList<>();
        for (MapLayer aLayer : _mapView.getLayers()) {
            if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
                if (aLayer.getShapeType() == ShapeTypes.Polygon) {
                    layerNames.add(aLayer.getLayerName());
                }
            }
        }

        return layerNames;
    }
    // </editor-fold>

    // <editor-fold desc="BeanInfo">
    public class MaskOutBean {
        
        public MaskOutBean() {
        }

        // <editor-fold desc="Get Set Methods">
        /**
         * Get if is mask out
         *
         * @return Boolean
         */
        public boolean isMask() {
            return _setMaskLayer;
        }

        /**
         * Set if is mask out
         *
         * @param istrue Boolean
         */
        public void setMask(boolean istrue) {
            _setMaskLayer = istrue;
            _mapView.paintLayers();
        }

        /**
         * Get mask layer name
         *
         * @return The mask layer name
         */
        public String getMaskLayer() {
            return _maskLayer;
        }

        /**
         * Set mask layer name
         *
         * @param lname The layer name
         */
        public void setMaskLayer(String lname) {
            _maskLayer = lname;
            _mapView.paintLayers();
        }
        // </editor-fold>
    }

    public static class MaskOutBeanBeanInfo extends BaseBeanInfo {

        public MaskOutBeanBeanInfo() {
            super(MaskOut.MaskOutBean.class);
            ExtendedPropertyDescriptor e = addProperty("maskLayer");
            e.setCategory("General").setPropertyEditorClass(MaskOut.LayerNameEditor.class);
            e.setDisplayName("Layer Name");
            e.setShortDescription("The name of the layers can be used as maskout");
            e = addProperty("mask");
            e.setCategory("General").setDisplayName("Is Maskout");
        }
    }

    public static class LayerNameEditor extends ComboBoxPropertyEditor {

        public LayerNameEditor() {
            super();
            String[] names = (String[]) getLayerNames().toArray(new String[0]);
            setAvailableValues(names);
//            Icon[] icons = new Icon[4];
//            Arrays.fill(icons, UIManager.getIcon("Tree.openIcon"));
//            setAvailableIcons(icons);
        }
    }
    // </editor-fold>
}
