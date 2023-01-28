package org.meteoinfo.dataframe;

import java.util.List;

public class MultiIndex extends Index<List> {
    /**
     * Construction
     * @param data The index data
     */
    public MultiIndex(List data) {
        this.data = data;
    }
}
