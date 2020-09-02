/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class Popup extends JWindow {
    // <editor-fold desc="Variables">
    private final static int MAX_HEIGHT = 300;
    private final static int MIN_WIDTH = 200;
    private final static int MAX_WIDTH = 400;
    private final JTextComponent textCompnent;
    private int dotPosition;
    private final JList list;
    private String[] originalData;
    private String[] data;
    private String typed;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param frame JFrame
     * @param textComponent 
     */
    public Popup(JFrame frame, JTextComponent textComponent){
        super(frame);
        this.textCompnent = textComponent;
        this.setSize(200, 200);
        this.list = new JList();
        this.list.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                Popup.this.type(e);
            }
        });
        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    Popup.this.chooseSelected();
                }
            }
        });
        this.list.setBackground(new Color(255,255,225));
        this.list.setForeground(Color.BLACK);
        this.getContentPane().add(new JScrollPane(this.list));
        this.list.setSelectedIndex(0);
        this.typed = "";
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) 
            {
                typed = "";
            }
            @Override
            public void componentShown(ComponentEvent e) {
                
            }
        });
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set method list
     * @param methodList Method list 
     */
    public void setMethods(String[] methodList){
        this.data = methodList;
        this.originalData = methodList;
        this.list.setListData(methodList);
    }
    
//    @Override
//    public void show(){
//        this.dotPosition = this.textCompnent.getCaretPosition();
//        this.setSize(this.getPreferredSize());
//        //super.show();
//        this.setVisible(true);
//    }
    
    public void showMethodCompletionList(String[] list, Point displayPoint){        
        Dimension size = this.getPreferredSize();        
        this.setLocation(displayPoint);
        this.setBounds(displayPoint.x, displayPoint.y, size.width, size.height);
        this.setMethods(list);
        if (this.textCompnent != null)
            this.dotPosition = this.textCompnent.getCaretPosition();
        //this.show();
        this.setVisible(true);
        this.list.setSelectedIndex(0);
        this.setAlwaysOnTop(true);
    }
    
    /**
     * Show popup window
     * @param displayPoint Display pooint
     */
    public void showPopup(Point displayPoint){        
        Dimension size = this.getPreferredSize();        
        this.setLocation(displayPoint);
        this.setBounds(displayPoint.x, displayPoint.y, size.width, size.height);
        if (this.textCompnent != null)
            this.dotPosition = this.textCompnent.getCaretPosition();
        this.setVisible(true);
        this.list.setSelectedIndex(0);
        this.setAlwaysOnTop(true);
    }
    
    @Override
    public Dimension getPreferredSize(){
        // need to add a magic amount to the size to avoid scrollbars
        // I'm sure there's a better way to do this
        int MAGIC = 20;
        Dimension size = this.list.getPreferredScrollableViewportSize();
        int height = size.height + MAGIC;
        int width = size.width + MAGIC;
        if (height > Popup.MAX_HEIGHT)
            height = Popup.MAX_HEIGHT;
        if (width > Popup.MAX_WIDTH)
            width = Popup.MAX_WIDTH;
        if (width < Popup.MIN_WIDTH)
            width = Popup.MIN_WIDTH;
        return new Dimension(width, height);
    }    
    
    public synchronized void type(KeyEvent e) {
        if (!this.isVisible())
            return;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                this.setVisible(false);
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_TAB:
                this.chooseSelected();
                e.consume();
                break;
            case KeyEvent.VK_SPACE:
                this.chooseSelected();
                break;
            case KeyEvent.VK_PERIOD:
                this.chooseSelected();
                break;
            case KeyEvent.VK_LEFT_PARENTHESIS:
                this.chooseSelected();
                break;
            case KeyEvent.VK_UP:
                this.up();
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                this.down();
                e.consume();
                break;
            case KeyEvent.VK_PAGE_UP:
                this.pageUp();
                e.consume();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                this.pageDown();
                e.consume();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (this.typed.isEmpty()){
                    this.setVisible(false);
                    break;
                }
                this.typed = this.typed.substring(0, this.typed.length() - 1);
                this.data = filter(this.originalData, this.typed);
                this.list.setListData(this.data);
                this.list.setSelectedIndex(0);
                break;
            default:
                char c = e.getKeyChar();
                if (Character.isJavaIdentifierPart(c)){
                    this.typed += c; 
                    this.data = filter(this.data, this.typed);
                    this.list.setListData(this.data);
                    this.list.setSelectedIndex(0);
                }
                break;
        }
    }
    
    private void chooseSelected(){
        Object value = this.list.getSelectedValue();
        if (value != null){
            int startPosition = this.dotPosition;
            int caretPosition = this.textCompnent.getCaretPosition();
            this.textCompnent.select(startPosition, caretPosition) ;
            this.textCompnent.replaceSelection(value.toString());
            caretPosition = startPosition + value.toString().length();
            try {
                this.textCompnent.setCaretPosition(caretPosition);
            } catch (Exception e){
                
            }
        }
        this.setVisible(false);
    }
    
    private void down(){
        int index = this.list.getSelectedIndex();
        int max = this.getListSize() - 1;
        
        if (index < max){
            index += 1;
            this.setSelected(index);
        } else if (index == max){
            index = 0;
            this.setSelected(index);
        }
    }
    
    private void up(){
        int index = this.list.getSelectedIndex();
        
        if (index == 0){
            index = this.getListSize() - 1;
            this.setSelected(index);
        } else if (index > 0){
            index -= 1;
            this.setSelected(index);
        }
    }
    
    private void pageUp(){
        int index = this.list.getSelectedIndex();
        int visibleRows = this.list.getVisibleRowCount();
        index = Math.max(index - visibleRows, 0);
        this.setSelected(index);
    }
    
    private void pageDown(){
        int index = this.list.getSelectedIndex();
        int visibleRows = this.list.getVisibleRowCount();
        index = Math.min(index + visibleRows, this.getListSize() - 1);
        this.setSelected(index);
    }
    
    private void setSelected(int index){
        this.list.setSelectedIndex(index);
        this.list.ensureIndexIsVisible(index);
    }
    
    private int getListSize(){
        return this.list.getModel().getSize();
    }
    
    private String[] filter(String[] list, String prefix){
        prefix = prefix.toLowerCase();
        List<String> nlist = new ArrayList<>();
        for (String str : list){
            if (str.toLowerCase().startsWith(prefix))
                nlist.add(str);
        }
        return nlist.toArray(new String[nlist.size()]);
    }
    // </editor-fold>
}
