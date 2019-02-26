/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global.event;

import java.util.EventListener;

/**
 *
 * @author yaqiang
 */
public interface INodeSelectedListener extends EventListener{
    public void nodeSelectedEvent(NodeSelectedEvent event);
}
