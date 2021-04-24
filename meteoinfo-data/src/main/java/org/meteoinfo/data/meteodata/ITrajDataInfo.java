package org.meteoinfo.data.meteodata;

import org.meteoinfo.table.DataTable;

import java.util.List;

public interface ITrajDataInfo {

    /**
     * Get DataTable list
     * @return DataTable list
     */
    public abstract List<DataTable> getDataTables();

    /**
     * Get TrajectoryInfo list
     * @return TrajectoryInfo list
     */
    public abstract List<TrajectoryInfo> getTrajInfoList();

    /**
     * Get X coordinate variable name
     * @return X coordinate variable name
     */
    public abstract String getXVarName();

    /**
     * Get Y coordinate variable name
     * @return Y coordinate variable name
     */
    public abstract String getYVarName();

    /**
     * Get Z coordinate variable name
     * @return Z coordinate variable name
     */
    public abstract String getZVarName();

    /**
     * Get time coordinate variable name
     * @return Time coordinate variable name
     */
    public abstract String getTVarName();

    /**
     * Get variables
     * @return Variables
     */
    public abstract List<Variable> getVariables();

}
