package org.meteoinfo.ndarray;

import java.util.List;

public class FlatIndex {
    private Array array;
    private Index index;

    /**
     * Constructor
     * @param array The array
     */
    public FlatIndex(Array array) {
        this.array = array;
        this.index = this.array.getIndex();
    }

    /**
     * Get object
     * @param idx The flat index
     * @return The object
     */
    public Object getObject(int idx) {
        this.index.setCurrentIndex(idx);
        return this.array.getObject(this.index);
    }

    /**
     * Set object
     * @param idx The flat index
     * @param value The object
     */
    public void setObject(int idx, Object value) {
        this.index.setCurrentIndex(idx);
        this.array.setObject(this.index, value);
    }

    /**
     * Get section array
     * @param first The first index
     * @param last The last index
     * @param stride The stride
     * @return Array
     */
    public Array section(int first, int last, int stride) {
        int n = 1 + Math.abs(last - first) / Math.abs(stride);
        Array r = Array.factory(this.array.dataType, new int[]{n});
        int ii = 0;
        if (last >= first) {
            for (int i = first; i <= last; i += stride) {
                index.setCurrentIndex(i);
                r.setObject(ii, this.array.getObject(index));
                ii += 1;
            }
        } else {
            for (int i = first; i >= last; i += stride) {
                index.setCurrentIndex(i);
                r.setObject(ii, this.array.getObject(index));
                ii += 1;
            }
        }
        return r;
    }

    /**
     * Set section array
     * @param idx Index list
     * @return Section array
     */
    public Array section(List<Integer> idx) {
        Array r = Array.factory(this.array.dataType, new int[]{idx.size()});
        int ii = 0;
        for (int i : idx) {
            this.index.setCurrentIndex(i);
            r.setObject(ii, this.array.getObject(this.index));
            ii += 1;
        }
        return r;
    }

    /**
     * Set section array
     * @param first The first index
     * @param last The last index
     * @param stride The stride
     * @param value The value
     */
    public void setSection(int first, int last, int stride, Object value) {
        if (last >= first) {
            for (int i = first; i <= last; i += stride) {
                index.setCurrentIndex(i);
                this.array.setObject(index, value);
            }
        } else {
            for (int i = first; i >= last; i += stride) {
                index.setCurrentIndex(i);
                this.array.setObject(index, value);
            }
        }
    }

    /**
     * Set section array
     * @param first The first index
     * @param last The last index
     * @param stride The stride
     * @param value The value array
     */
    public void setSection(int first, int last, int stride, Array value) {
        IndexIterator iterator = value.getIndexIterator();
        if (last >= first) {
            for (int i = first; i <= last; i += stride) {
                index.setCurrentIndex(i);
                this.array.setObject(index, iterator.getObjectNext());
            }
        } else {
            for (int i = first; i >= last; i += stride) {
                index.setCurrentIndex(i);
                this.array.setObject(index, iterator.getObjectNext());
            }
        }
    }

    /**
     * Set section
     * @param idx Index list
     * @param value Value
     */
    public void setSection(List<Integer> idx, Object value) {
        for (int i : idx) {
            this.index.setCurrentIndex(i);
            this.array.setObject(index, value);
        }
    }

    /**
     * Set section
     * @param idx Index list
     * @param value Value array
     */
    public void setSection(List<Integer>idx, Array value) {
        IndexIterator iterator = value.getIndexIterator();
        for (int i : idx) {
            this.index.setCurrentIndex(i);
            this.array.setObject(index, iterator.getObjectNext());
        }
    }
}
