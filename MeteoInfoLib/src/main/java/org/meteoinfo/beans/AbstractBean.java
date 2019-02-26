/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

public abstract class AbstractBean {

    private transient PropertyChangeSupport pcs;
    private transient VetoableChangeSupport vcs;

    protected AbstractBean() {
        this.pcs = new PropertyChangeSupport(this);
        this.vcs = new VetoableChangeSupport(this);
    }

    protected AbstractBean(PropertyChangeSupport pcs, VetoableChangeSupport vcs) {
        if (pcs == null) {
            throw new NullPointerException("PropertyChangeSupport must not be null");
        }
        if (vcs == null) {
            throw new NullPointerException("VetoableChangeSupport must not be null");
        }

        this.pcs = pcs;
        this.vcs = vcs;
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public final PropertyChangeListener[] getPropertyChangeListeners() {
        return this.pcs.getPropertyChangeListeners();
    }

    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(propertyName, listener);
    }

    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(propertyName, listener);
    }

    public final PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.pcs.getPropertyChangeListeners(propertyName);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void firePropertyChange(PropertyChangeEvent evt) {
        this.pcs.firePropertyChange(evt);
    }

    protected final void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        this.pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    protected final boolean hasPropertyChangeListeners(String propertyName) {
        return this.pcs.hasListeners(propertyName);
    }

    protected final boolean hasVetoableChangeListeners(String propertyName) {
        return this.vcs.hasListeners(propertyName);
    }

    public final void addVetoableChangeListener(VetoableChangeListener listener) {
        this.vcs.addVetoableChangeListener(listener);
    }

    public final void removeVetoableChangeListener(VetoableChangeListener listener) {
        this.vcs.removeVetoableChangeListener(listener);
    }

    public final VetoableChangeListener[] getVetoableChangeListeners() {
        return this.vcs.getVetoableChangeListeners();
    }

    public final void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vcs.addVetoableChangeListener(propertyName, listener);
    }

    public final void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vcs.removeVetoableChangeListener(propertyName, listener);
    }

    public final VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return this.vcs.getVetoableChangeListeners(propertyName);
    }

    protected final void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
            throws PropertyVetoException {
        this.vcs.fireVetoableChange(propertyName, oldValue, newValue);
    }

    protected final void fireVetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {
        this.vcs.fireVetoableChange(evt);
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        AbstractBean result = (AbstractBean) super.clone();
        result.pcs = new PropertyChangeSupport(result);
        result.vcs = new VetoableChangeSupport(result);
        return result;
    }
}