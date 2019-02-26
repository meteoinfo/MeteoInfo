/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author User
 */
public class Test extends JPanel {
    private boolean isOk = false;
    public Test(){
        isOk = false;
    }
    
    public boolean getIsOK(){
        return isOk;
    }
    
    public void setIsOK(boolean istrue){
        isOk = istrue;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawString("Test", 20, 20);
    }
}
