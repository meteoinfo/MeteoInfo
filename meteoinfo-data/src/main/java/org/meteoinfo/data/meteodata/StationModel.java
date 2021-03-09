/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.meteodata;

/**
 *
 * @author yaqiang
 */
public class StationModel {
    // <editor-fold desc="Variables">

    private String _stId;
    private String _stName;
    private double _lon;
    private double _lat;
    private double _cloudCover;
    private double _windDirection;
    private double _windSpeed;
    private double _pressure;
    private double _pressureChange;
    private double _weather;
    private double _previousWeather;
    private double _visibility;
    private double _temperature;
    private double _dewPoint;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public StationModel() {
    }

    /**
     * Constructor
     *
     * @param initValue Initialize value
     */
    public StationModel(double initValue) {
        _cloudCover = initValue;
        _windDirection = initValue;
        _windSpeed = initValue;
        _pressure = initValue;
        _pressureChange = initValue;
        _weather = initValue;
        _previousWeather = initValue;
        _visibility = initValue;
        _temperature = initValue;
        _dewPoint = initValue;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get station identifer
     *
     * @return Station identifer
     */
    public String getStationIdentifer() {
        return _stId;
    }

    /**
     * Set station identifer
     *
     * @param value
     */
    public void setStationIdentifer(String value) {
        _stId = value;
    }

    /**
     * Get station name
     *
     * @return Station name
     */
    public String getStationName() {
        return _stName;
    }

    /**
     * Set station name
     *
     * @param value Station name
     */
    public void setStationName(String value) {
        _stName = value;
    }

    /**
     * Get longitude
     *
     * @return Longitude
     */
    public double getLongitude() {
        return _lon;
    }

    /**
     * Set longitude
     *
     * @param value Longitude
     */
    public void setLongitude(double value) {
        _lon = value;
    }

    /**
     * Get latitude
     *
     * @return Latitude
     */
    public double getLatitude() {
        return _lat;
    }

    /**
     * Set latitude
     *
     * @param value Latitude
     */
    public void setLatitude(double value) {
        _lat = value;
    }

    /**
     * Get cloud cover
     *
     * @return Cloud cover
     */
    public double getCloudCover() {
        return _cloudCover;
    }

    /**
     * Set cloud cover
     *
     * @param value Cloud cover
     */
    public void setCloudCover(double value) {
        _cloudCover = value;
    }

    /**
     * Get wind direction
     *
     * @return Wind direction
     */
    public double getWindDirection() {
        return _windDirection;
    }

    /**
     * Set wind direction
     *
     * @param value Wind direction
     */
    public void setWindDirection(double value) {
        _windDirection = value;
    }

    /**
     * Get wind speed
     *
     * @return Wind speed
     */
    public double getWindSpeed() {
        return _windSpeed;
    }

    /**
     * Set wind speed
     *
     * @param value Wind speed
     */
    public void setWindSpeed(double value) {
        _windSpeed = value;
    }

    /**
     * Get pressure
     *
     * @return Pressure
     */
    public double getPressure() {
        return _pressure;
    }

    /**
     * Set pressure
     *
     * @param value Pressure
     */
    public void setPressure(double value) {
        _pressure = value;
    }

    /**
     * Get pressure change
     *
     * @return Pressure change
     */
    public double getPressureChange() {
        return _pressureChange;
    }

    /**
     * Set pressure change
     *
     * @param value Pressure change
     */
    public void setPressureChange(double value) {
        _pressureChange = value;
    }

    /**
     * Get weather
     *
     * @return Weather
     */
    public double getWeather() {
        return _weather;
    }

    /**
     * Set weather
     *
     * @param value Weather
     */
    public void setWeather(double value) {
        _weather = value;
    }

    /**
     * Get previous weather
     *
     * @return Previous weather
     */
    public double getPreviousWeather() {
        return _previousWeather;
    }

    /**
     * Set previous weather
     *
     * @param value Previous weather
     */
    public void setPreviousWeather(double value) {
        _previousWeather = value;
    }

    /**
     * Get visibility
     *
     * @return Visibility
     */
    public double getVisibility() {
        return _visibility;
    }

    /**
     * Set visibility
     *
     * @param value Visibility
     */
    public void setVisibility(double value) {
        _visibility = value;
    }

    /**
     * Get temperature
     *
     * @return Temperature
     */
    public double getTemperature() {
        return _temperature;
    }

    /**
     * Set temperature
     *
     * @param value Temperature
     */
    public void setTemperature(double value) {
        _temperature = value;
    }

    /**
     * Get dew point
     *
     * @return Dew point
     */
    public double getDewPoint() {
        return _dewPoint;
    }

    /**
     * Set dew point
     *
     * @param value Dew point
     */
    public void setDewPoint(double value) {
        _dewPoint = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
