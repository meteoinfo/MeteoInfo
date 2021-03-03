/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ndarray;

/**
 *
 * @author Yaqiang Wang
 */
public class Complex extends org.apache.commons.math3.complex.Complex {
    
    public Complex(double real) {
        super(real);
    }
    
    public Complex(double real, double imag){
        super(real, imag);
    }
    
    public Complex(org.apache.commons.math3.complex.Complex c){
        this(c.getReal(), c.getImaginary());
    }
    
    @Override
    public Complex add(org.apache.commons.math3.complex.Complex addend){
        return new Complex(super.add(addend));
    }
    
    @Override
    public Complex add(double addend) {
        return new Complex(super.add(addend));
    }
    
    @Override
    public Complex subtract(org.apache.commons.math3.complex.Complex v){
        return new Complex(super.subtract(v));
    }
    
    @Override
    public Complex subtract(double v) {
        return new Complex(super.subtract(v));
    }
    
    public Complex rSubtract(double v) {
        return new Complex(v - this.getReal(), -this.getImaginary());
    }
    
    @Override
    public Complex multiply(org.apache.commons.math3.complex.Complex v){
        return new Complex(super.multiply(v));
    }
    
    @Override
    public Complex multiply(double v) {
        return new Complex(super.multiply(v));
    }
    
    @Override
    public Complex divide(org.apache.commons.math3.complex.Complex v){
        return new Complex(super.divide(v));
    }
    
    @Override
    public Complex divide(double v) {
        return new Complex(super.divide(v));
    }
    
    public Complex rDivide(double v) {
        return new Complex(v, 0).divide(this);
    }
    
    @Override
    public Complex acos(){
        return new Complex(super.acos());
    }
    
    @Override
    public Complex asin(){
        return new Complex(super.asin());
    }
    
    @Override
    public Complex atan(){
        return new Complex(super.atan());
    }
    
    @Override
    public Complex cos(){
        return new Complex(super.cos());
    }
    
    @Override
    public Complex log(){
        return new Complex(super.log());
    }

    @Override
    public Complex pow(org.apache.commons.math3.complex.Complex v) {
        return new Complex(super.pow(v));
    }
    
    @Override
    public Complex pow(double v){
        return new Complex(super.pow(v));
    }
    
    public Complex rPow(double v){
        return new Complex(v, 0).pow(this);
    }
    
    @Override
    public Complex exp() {
        return new Complex(super.exp());
    }
    
    @Override
    public Complex sin(){
        return new Complex(super.sin());        
    }
    
    @Override
    public Complex sqrt(){
        return new Complex(super.sqrt());
    }
    
    @Override
    public Complex tan(){
        return new Complex(super.tan());
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.getReal());
        if (this.getImaginary() > 0)
            sb.append("+");
        else if (this.getImaginary() == 0){
            if (!String.valueOf(this.getImaginary()).startsWith("-"))
                sb.append("+");
        }
        sb.append(this.getImaginary());
        sb.append("j");
        return sb.toString();
    }
}
