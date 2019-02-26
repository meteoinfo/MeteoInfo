/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.plugin;

/**
 *
 * @author yaqiang
 */
public abstract class PluginBase implements IPlugin{
    private IApplication _application = null;
    private String _name;
    private String _author;
    private String _version;
    private String _description;

    @Override
    public IApplication getApplication() {
        return this._application;
    }

    @Override
    public void setApplication(IApplication app) {
        this._application = app;
    }

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public void setName(String value) {
        this._name = value;
    }
    
    @Override
    public String getAuthor() {
        return this._author;
    }

    @Override
    public void setAuthor(String value) {
        this._author = value;
    }

    @Override
    public String getVersion() {
        return this._version;
    }

    @Override
    public void setVersion(String value) {
        this._version = value;
    }

    @Override
    public String getDescription() {
        return this._description;
    }

    @Override
    public void setDescription(String value) {
        this._description = value;
    }

    @Override
    public void load() {
        
    }

    @Override
    public void unload() {
        
    }
}
