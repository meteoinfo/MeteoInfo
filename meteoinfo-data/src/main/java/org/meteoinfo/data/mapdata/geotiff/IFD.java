package org.meteoinfo.data.mapdata.geotiff;

import org.meteoinfo.ndarray.ArrayDate;

import java.util.ArrayList;
import java.util.List;

public class IFD {
    private List<IFDEntry> tags = new ArrayList<>();

    /**
     * Constructor
     */
    public IFD() {
    }

    /**
     * Get tags
     * @return Tags
     */
    public List<IFDEntry> getTags() {return this.tags;}

    /**
     * Set tags
     * @param value Tags
     */
    public void setTags(List<IFDEntry> value) {
        this.tags = value;
    }

    /**
     * Find tag
     *
     * @param tag Tag
     * @return IFDEntry
     */
    IFDEntry findTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        for (IFDEntry ifd : this.tags) {
            if (ifd.tag == tag) {
                return ifd;
            }
        }
        return null;
    }

    /**
     * Add a tag
     * @param tag The tag
     */
    public void addTag(IFDEntry tag) {
        this.tags.add(tag);
    }

    /**
     * Delete tag
     *
     * @param ifd IFDEntry
     */
    public void deleteTag(IFDEntry ifd) {
        this.tags.remove(ifd);
    }

    /**
     * Get tag by index
     * @param i Index
     * @return Tag
     */
    public IFDEntry getTag(int i) {
        return this.tags.get(i);
    }

    /**
     * Get tag number
     * @return Tag number
     */
    public int getTagNum() {
        return this.tags.size();
    }
}
