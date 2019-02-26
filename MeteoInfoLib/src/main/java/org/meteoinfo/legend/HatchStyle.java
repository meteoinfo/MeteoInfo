/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

/**
 *
 * @author Yaqiang Wang
 */
public enum HatchStyle {
    NONE,    //Not using hatch style
    HORIZONTAL,
    VERTICAL,
    FORWARD_DIAGONAL,
    BACKWARD_DIAGONAL,
    CROSS,
    DIAGONAL_CROSS,
    DOT;
    
    /**
     * Get hatch style from a string
     * @param h Hatch style string
     * @return HatchStyle
     */
    public static HatchStyle getStyle(String h){
        HatchStyle hatch = HatchStyle.NONE;
        switch (h.toLowerCase()){
            case "-":
            case "horizontal":
                hatch = HatchStyle.HORIZONTAL;
                break;
            case "|":
            case "vertical":
                hatch = HatchStyle.VERTICAL;
                break;
            case "\\":
            case "forward_diagonal":
                hatch = HatchStyle.FORWARD_DIAGONAL;
                break;
            case "/":
            case "backward_diagonal":
                hatch = HatchStyle.BACKWARD_DIAGONAL;
                break;
            case "+":
            case "cross":
                hatch = HatchStyle.CROSS;
                break;
            case "x":
            case "diagonal_cross":
                hatch = HatchStyle.DIAGONAL_CROSS;
                break;
            case ".":
            case "dot":
                hatch = HatchStyle.DOT;
                break;
        }   
        return hatch;
    }
}
