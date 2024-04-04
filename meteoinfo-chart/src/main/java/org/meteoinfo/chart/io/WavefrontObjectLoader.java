package org.meteoinfo.chart.io;

import org.meteoinfo.chart.graphic.TriMeshGraphic;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WavefrontObjectLoader {

    private Logger logger = LoggerFactory.getLogger("Wavefront Object Loader");
    private ArrayList<float[]> vData = new ArrayList<float[]>();    //list of vertex coordinates
    private ArrayList<float[]> vtData = new ArrayList<float[]>();   //list of texture coordinates
    private ArrayList<float[]> vnData = new ArrayList<float[]>();   //list of normal coordinates
    private ArrayList<int[]> fv = new ArrayList<int[]>();           //face vertex indices
    private ArrayList<int[]> ft = new ArrayList<int[]>();           //face texture indices
    private ArrayList<int[]> fn = new ArrayList<int[]>();           //face normal indices
    private int polyCount = 0;                                      //the model polygon count


    /**
     * Constructor
     * @param objFileName Wave front object file
     */
    public WavefrontObjectLoader(String objFileName) {
        loadObjModel(objFileName);
    }

    private void loadObjModel(String objFileName) {
        try {
            BufferedReader br = null;
            if (objFileName.endsWith(".zip")) {
                logger.info("WAVEFRONT MESH IS COMPRESSED! TRY TO EXTRACT FIRST/SINGLE ENTRY!");
                ZipInputStream tZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(objFileName)));
                ZipEntry tZipEntry;
                tZipEntry = tZipInputStream.getNextEntry();
                String inZipEntryName = tZipEntry.getName();
                if (inZipEntryName==null) {
                    logger.error("ERROR! ZIP ENTRY IS NULL!");
                }
                logger.info("EXTRACTING: " + inZipEntryName);
                if (!tZipEntry.isDirectory()) {
                    br = new BufferedReader(new InputStreamReader(tZipInputStream));
                } else {
                    logger.error("ERROR! ZIP ENTRY IS DIRECTORY! SHOULD BE PLAIN FILE!");
                }
            } else {
                br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(objFileName))));
            }

            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {         //read any descriptor data in the file
                    // Zzzz ...
                } else if (line.equals("")) {
                    // Ignore whitespace data
                } else if (line.startsWith("v ")) {  //read in vertex data
                    vData.add(processData(line));
                } else if (line.startsWith("vt ")) { //read texture coordinates
                    vtData.add(processData(line));
                } else if (line.startsWith("vn ")) { //read normal coordinates
                    vnData.add(processData(line));
                } else if (line.startsWith("f ")) {  //read face data
                    processFaceData(line);
                }
            }
            br.close();
            logger.info("MODEL " + objFileName + " SUCCESSFULLY LOADED!");
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    private float[] processData(String read) {
        final String s[] = read.split("\\s+");
        return (processFloatData(s)); //returns an array of processed float data
    }

    private float[] processFloatData(String sdata[]) {
        float data[] = new float[sdata.length - 1];
        for (int loop = 0; loop < data.length; loop++) {
            data[loop] = Float.parseFloat(sdata[loop + 1]);
        }
        return data; //return an array of floats
    }

    private void processFaceData(String fread) {
        polyCount++;
        String s[] = fread.split("\\s+");
        if (fread.contains("//")) { //pattern is present if obj has only v and vn in face data
            for (int loop = 1; loop < s.length; loop++) {
                s[loop] = s[loop].replaceAll("//", "/0/"); //insert a zero for missing vt data
            }
        }
        processFaceIntData(s); //pass in face data
    }

    private void processFaceIntData(String sdata[]) {
        int vdata[] = new int[sdata.length - 1];
        int vtdata[] = new int[sdata.length - 1];
        int vndata[] = new int[sdata.length - 1];
        for (int loop = 1; loop < sdata.length; loop++) {
            String s = sdata[loop];
            String[] temp = s.split("/");
            vdata[loop - 1] = Integer.valueOf(temp[0]) - 1;         //always add vertex indices
            if (temp.length > 1) {                              //we have v and vt data
                vtdata[loop - 1] = Integer.valueOf(temp[1]) - 1;    //add in vt indices
            } else {
                vtdata[loop - 1] = 0;                           //if no vt data is present fill in zeros
            }
            if (temp.length > 2) {                              //we have v, vt, and vn data
                vndata[loop - 1] = Integer.valueOf(temp[2]) - 1;    //add in vn indices
            } else {
                vndata[loop - 1] = 0;                           //if no vn data is present fill in zeros
            }
        }
        if (vdata.length == 3) {
            fv.add(vdata);
            ft.add(vtdata);
            fn.add(vndata);
        } else {
            fv.addAll(quad2Triangles(vdata));
            ft.addAll(quad2Triangles(vtdata));
            fn.addAll(quad2Triangles(vndata));
        }
    }

    private ArrayList<int[]> quad2Triangles(int[] data) {
        int[] triangle1 = new int[]{data[0], data[1], data[2]};
        int[] triangle2 = new int[]{data[2], data[3], data[0]};

        return new ArrayList<int[]>(Arrays.asList(triangle1, triangle2));
    }

    /**
     * Get vertex coordinate data
     * @return Vertex coordinate data
     */
    public ArrayList<float[]> getVertex() {
        return vData;
    }

    /**
     * Get vertex normal data
     * @return Vertex normal data
     */
    public ArrayList<float[]> getVertexNormal() {
        return vnData;
    }

    /**
     * Get vertex texture data
     * @return Vertex texture data
     */
    public ArrayList<float[]> getVertexTexture() {
        return vtData;
    }

    /**
     * Get vertex indices data
     * @return Vertex indices data
     */
    public ArrayList<int[]> getVertexIndices() {
        return fv;
    }

    /**
     * Get normal indices data
     * @return Normal indices data
     */
    public ArrayList<int[]> getNormalIndices() {
        return fn;
    }

    /**
     * Get texture indices data
     * @return Texture indices data
     */
    public ArrayList<int[]> getTextureIndices() {
        return ft;
    }

    /**
     * Get vertex data array
     * @return Vertex data array
     */
    public Array getVertexArray() {
        int n = vData.size();
        Array r = Array.factory(DataType.FLOAT, new int[]{n, 3});
        float[] v;
        int idx = 0;
        for (int i = 0; i < n; i++) {
            v = vData.get(i);
            r.setFloat(idx, v[0]);
            r.setFloat(idx + 1, v[1]);
            r.setFloat(idx + 2, v[2]);
            idx += 3;
        }

        return r;
    }

    /**
     * Get vertex data arrays - x, y, z
     * @return Vertex data arrays
     */
    public Array[] getVertexArrays() {
        int n = vData.size();
        Array x = Array.factory(DataType.FLOAT, new int[]{n});
        Array y = Array.factory(DataType.FLOAT, new int[]{n});
        Array z = Array.factory(DataType.FLOAT, new int[]{n});
        float[] v;
        for (int i = 0; i < n; i++) {
            v = vData.get(i);
            x.setFloat(i, v[0]);
            y.setFloat(i, v[1]);
            z.setFloat(i, v[2]);
        }

        return new Array[]{x, y, z};
    }

    /**
     * Get vertex indices array
     * @return Vertex indices array
     */
    public Array getVertexIndicesArray() {
        int n = fv.size();
        Array r = Array.factory(DataType.INT, new int[]{n, 3});
        int idx = 0;
        int[] v;
        for (int i = 0; i < n; i++) {
            v = fv.get(i);
            r.setInt(idx, v[0]);
            r.setInt(idx + 1, v[1]);
            r.setInt(idx + 2, v[2]);
            idx += 3;
        }

        return r;
    }

    /**
     * Get vertex normal data array
     * @return Vertex normal data array
     */
    public Array getVertexNormalArray() {
        int n = vnData.size();
        if (n == 0) {
            return null;
        }

        Array r = Array.factory(DataType.FLOAT, new int[]{n, 3});
        float[] v;
        int idx = 0;
        for (int i = 0; i < n; i++) {
            v = vnData.get(i);
            r.setFloat(idx, v[0]);
            r.setFloat(idx + 1, v[1]);
            r.setFloat(idx + 2, v[2]);
            idx += 3;
        }

        return r;
    }
}
