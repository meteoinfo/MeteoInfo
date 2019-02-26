 /* Copyright 2012 - Yaqiang Wang,
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

import org.meteoinfo.plugin.IPlugin;
import org.meteoinfo.plugin.PluginBase;

/**
 *
 * @author Yaqiang Wang
 */
public class Plugin extends PluginBase{
    // <editor-fold desc="Variables">
    //private String _name;
    private String _jarFileName;
    private String _className;
    private IPlugin _pluginObject = null;
    private boolean _isLoad = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    /**
     * Get jar file name
     * @return Jar file name
     */
    public String getJarFileName(){
        return this._jarFileName;
    }
    
     /**
     * Set jar file name
     * @param value Jar file name
     */
    public void setJarFileName(String value){
        this._jarFileName = value;
    }
    
    /**
     * Get jar path in 'plugins' folder
     * @return Jar path
     */
    public String getJarPath(){
        String path = this._jarFileName;
        int idx = path.indexOf("plugins");
        if (idx >= 0){
            path = path.substring(idx + 8);
        }
        
        return path;
    }     
    
    /**
     * Get class name
     * @return Class name
     */
    public String getClassName(){
        return this._className;
    }
    
    /**
     * Set class name
     * @param value Class name
     */
    public void setClassName(String value){
        this._className = value;
    }
    
    /**
     * Get plugin object
     * @return Plugin object
     */
    public IPlugin getPluginObject(){
        return this._pluginObject;
    }
    
    /**
     * Set plugin object
     * @param value Plugin object
     */
    public void setPluginObject(IPlugin value){
        this._pluginObject = value;
        if (value != null){
            this.setName(value.getName());
            this.setAuthor(value.getAuthor());
            this.setVersion(value.getVersion());
            this.setDescription(value.getDescription());
        }
    }
    
    /**
     * Get if load the plugin
     * @return Boolean
     */
    public boolean isLoad(){
        return this._isLoad;
    }
    
    /**
     * Set if load the plugin
     * @param value Boolean
     */
    public void setLoad(boolean value){
        this._isLoad = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Override toString method
     * @return String
     */
    @Override
    public String toString(){
        return this.getName();
    }
    // </editor-fold>
}
