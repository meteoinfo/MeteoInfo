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
package org.meteoinfo.plugin;

/**
 *
 * @author yaqiang
 */
public interface IPlugin {
    /**
     * Get application
     * @return Application
     */
    public IApplication getApplication();
    
    /**
     * Set application
     * @param app Application
     */
    public void setApplication(IApplication app);
    
    /**
     * Get plugin name
     * @return Plugin name
     */
    public String getName();
    
    /**
     * Set plugin name
     * @param value Plugin name
     */
    public void setName(String value);
    
    /**
     * Get plugin author
     * @return Plugin author
     */
    public String getAuthor();
    
    /**
     * Set plugin author
     * @param value Plugin author
     */
    public void setAuthor(String value);
    
    /**
     * Get plugin version
     * @return Plugin version
     */
    public String getVersion();
    
    /**
     * Set plugin version
     * @param value Plugin version
     */
    public void setVersion(String value);
    
    /**
     * Get plugin description
     * @return Plugin description
     */
    public String getDescription();
    
    /**
     * Set plugin description
     * @param value Description
     */
    public void setDescription(String value);
    
    /**
     * Plugin load
     */
    public void load();
    
    /**
     * Plugin unload
     */
    public void unload();
}
