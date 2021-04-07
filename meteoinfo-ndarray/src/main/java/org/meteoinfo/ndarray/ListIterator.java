package org.meteoinfo.ndarray;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ListIterator implements IndexIterator {

    private int count = 0;
    private int currElement = 0;
    private int size = 0;
    private int rank = 1;
    private List<List<Integer>> ranges;
    private Array array;
    private Index rangeIndex;
    private IndexIterator rangeIndexIter;

    /**
     * Constructor
     * @param array The array
     * @param ranges The range list - mixed with int list and Range
     */
    public ListIterator(Array array, List<Object> ranges) {
        this.array = array;
        this.rank = ranges.size();
        int[] shape = new int[this.rank];
        this.ranges = new ArrayList<>();
        List<Integer> list;
        for (int i = 0; i < this.rank; i++) {
            Object k = ranges.get(i);
            if (k instanceof Range) {
                Range kr = (Range) k;
                shape[i] = kr.length();
                list = new ArrayList<>();
                for (int j = kr.first(); j <= kr.last(); j += kr.stride()) {
                    list.add(j);
                }
                this.ranges.add(list);
            } else {
                List<Integer> kl = (List<Integer>) k;
                shape[i] = kl.size();
                this.ranges.add(kl);
            }
        }

        Array tempArray = Array.factory(DataType.INT, shape);
        this.size = (int) tempArray.getSize();
        this.rangeIndex = tempArray.getIndex();
        this.rangeIndexIter = tempArray.getIndexIterator();
    }

    @Override
    public boolean hasNext() {
        return this.rangeIndexIter.hasNext();
    }

    @Override
    public double getDoubleNext() {
        return 0;
    }

    @Override
    public void setDoubleNext(double val) {

    }

    @Override
    public double getDoubleCurrent() {
        return 0;
    }

    @Override
    public void setDoubleCurrent(double val) {

    }

    @Override
    public float getFloatNext() {
        return 0;
    }

    @Override
    public void setFloatNext(float val) {

    }

    @Override
    public float getFloatCurrent() {
        return 0;
    }

    @Override
    public void setFloatCurrent(float val) {

    }

    @Override
    public long getLongNext() {
        return 0;
    }

    @Override
    public void setLongNext(long val) {

    }

    @Override
    public long getLongCurrent() {
        return 0;
    }

    @Override
    public void setLongCurrent(long val) {

    }

    @Override
    public int getIntNext() {
        return 0;
    }

    @Override
    public void setIntNext(int val) {

    }

    @Override
    public int getIntCurrent() {
        return 0;
    }

    @Override
    public void setIntCurrent(int val) {

    }

    @Override
    public short getShortNext() {
        return 0;
    }

    @Override
    public void setShortNext(short val) {

    }

    @Override
    public short getShortCurrent() {
        return 0;
    }

    @Override
    public void setShortCurrent(short val) {

    }

    @Override
    public byte getByteNext() {
        return 0;
    }

    @Override
    public void setByteNext(byte val) {

    }

    @Override
    public byte getByteCurrent() {
        return 0;
    }

    @Override
    public void setByteCurrent(byte val) {

    }

    @Override
    public char getCharNext() {
        return 0;
    }

    @Override
    public void setCharNext(char val) {

    }

    @Override
    public char getCharCurrent() {
        return 0;
    }

    @Override
    public void setCharCurrent(char val) {

    }

    @Override
    public String getStringNext() {
        return null;
    }

    @Override
    public void setStringNext(String val) {

    }

    @Override
    public String getStringCurrent() {
        return null;
    }

    @Override
    public void setStringCurrent(String val) {

    }

    @Override
    public boolean getBooleanNext() {
        return false;
    }

    @Override
    public void setBooleanNext(boolean val) {

    }

    @Override
    public boolean getBooleanCurrent() {
        return false;
    }

    @Override
    public void setBooleanCurrent(boolean val) {

    }

    @Override
    public Complex getComplexNext() {
        return null;
    }

    @Override
    public void setComplexNext(Complex val) {

    }

    @Override
    public Complex getComplexCurrent() {
        return null;
    }

    @Override
    public void setComplexCurrent(Complex val) {

    }

    @Override
    public void setDateCurrent(LocalDateTime val) {

    }

    @Override
    public LocalDateTime getDateNext() {
        return null;
    }

    @Override
    public void setDateNext(LocalDateTime val) {

    }

    @Override
    public LocalDateTime getDateCurrent() {
        return null;
    }

    @Override
    public Object getObjectNext() {
        return null;
    }

    @Override
    public void setObjectNext(Object val) {

    }

    @Override
    public Object getObjectCurrent() {
        return null;
    }

    @Override
    public void setObjectCurrent(Object val) {

    }

    @Override
    public Object next() {
        return this.rangeIndexIter.next();
    }

    @Override
    public int[] getCurrentCounter() {
        int[] currentCounter = new int[this.rank];
        int[] rangeCounter = this.rangeIndexIter.getCurrentCounter();
        int i = 0;
        for (List<Integer> range : this.ranges) {
            currentCounter[i] = range.get(rangeCounter[i]);
            i += 1;
        }

        return currentCounter;
    }
}
