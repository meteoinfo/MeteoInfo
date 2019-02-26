/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.application;

import org.meteoinfo.plugin.IPlugin;
import org.meteoinfo.plugin.PluginBase;

/**
 *
 * @author wyq
 */
public class Application extends PluginBase {
    // <editor-fold desc="Variables">
    //private String _name;
    private String path;
    private String _className;
    private IPlugin _pluginObject = null;
    private boolean _isLoad = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    /**
     * Get path
     * @return Path
     */
    public String getPath(){
        return this.path;
    }
    
     /**
     * Set path
     * @param value Path
     */
    public void setPath(String value){
        this.path = value;
    }
    
    /**
     * Get jar path in 'plugins' folder
     * @return Jar path
     */
    public String getJarPath(){
        String path = this.path;
        int idx = path.indexOf("toolbox");
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
