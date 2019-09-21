/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl.mc;

import java.util.ArrayList;
import org.meteoinfo.ndarray.Array;

/**
 * Created by Primoz on 11. 07. 2016.
 */
public class MarchingCubes {
    static float[] lerp(float[] vec1, float[] vec2, float alpha){
        return new float[]{vec1[0] + (vec2[0] - vec1[0]) * alpha, vec1[1] + (vec2[1] - vec1[1]) * alpha, vec1[2] + (vec2[2] - vec1[2]) * alpha};
    }

    static void marchingCubesChar(char[] values, int[] volDim, int volZFull, float[] voxDim, char isoLevel, int offset, CallbackMC callback) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];


        // Calculate maximal possible axis value (used in vertice normalization)
        float maxX = voxDim[0] * (volDim[0] - 1);
        float maxY = voxDim[1] * (volDim[1] - 1);
        float maxZ = voxDim[2] * (volZFull - 1);
        float maxAxisVal = Math.max(maxX, Math.max(maxY, maxZ));

        // Volume iteration
        for (int z = 0; z < volDim[2] - 1; z++) {
            for (int y = 0; y < volDim[1] - 1; y++) {
                for (int x = 0; x < volDim[0] - 1; x++) {

                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * (z + offset)),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    //							  X              Y                    Z
                    float position[] = new float[]{x * voxDim[0], y * voxDim[1], (z + offset) * voxDim[2]};

                    // Voxel intensities
                    char value0 = values[p],
                            value1 = values[px],
                            value2 = values[py],
                            value3 = values[pxy],
                            value4 = values[pz],
                            value5 = values[pxz],
                            value6 = values[pyz],
                            value7 = values[pxyz];

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (isoLevel - value0) / (value1 - value0);
                        vertList[0] = lerp(position, new float[]{position[0] + voxDim[0], position[1], position[2]}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (isoLevel - value1) / (value3 - value1);
                        vertList[1] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (isoLevel - value2) / (value3 - value2);
                        vertList[2] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (isoLevel - value0) / (value2 - value0);
                        vertList[3] = lerp(position, new float[]{position[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (isoLevel - value4) / (value5 - value4);
                        vertList[4] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (isoLevel - value5) / (value7 - value5);
                        vertList[5] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (isoLevel - value6) / (value7 - value6);
                        vertList[6] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (isoLevel - value4) / (value6 - value4);
                        vertList[7] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (isoLevel - value0) / (value4 - value0);
                        vertList[8] = lerp(position, new float[]{position[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (isoLevel - value1) / (value5 - value1);
                        vertList[9] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (isoLevel - value3) / (value7 - value3);
                        vertList[10] = lerp(new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1]+ voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (isoLevel - value2) / (value6 - value2);
                        vertList[11] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices normalized with the maximal possible value
                        vertices.add(new float[] {vertList[index3][0] / maxAxisVal - 0.5f, vertList[index3][1] / maxAxisVal - 0.5f, vertList[index3][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index2][0] / maxAxisVal - 0.5f, vertList[index2][1] / maxAxisVal - 0.5f, vertList[index2][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index1][0] / maxAxisVal - 0.5f, vertList[index1][1] / maxAxisVal - 0.5f, vertList[index1][2] / maxAxisVal - 0.5f});

                        i += 3;
                    }
                }
            }
        }

        callback.setVertices(vertices);
        callback.run();
    }

    static void marchingCubesShort(short[] values, int[] volDim, int volZFull, float[] voxDim, short isoLevel, int offset, CallbackMC callback) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];


        // Calculate maximal possible axis value (used in vertice normalization)
        float maxX = voxDim[0] * (volDim[0] - 1);
        float maxY = voxDim[1] * (volDim[1] - 1);
        float maxZ = voxDim[2] * (volZFull - 1);
        float maxAxisVal = Math.max(maxX, Math.max(maxY, maxZ));

        // Volume iteration
        for (int z = 0; z < volDim[2] - 1; z++) {
            for (int y = 0; y < volDim[1] - 1; y++) {
                for (int x = 0; x < volDim[0] - 1; x++) {

                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * (z + offset)),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    //							  X              Y                    Z
                    float position[] = new float[]{x * voxDim[0], y * voxDim[1], (z + offset) * voxDim[2]};

                    // Voxel intensities
                    short value0 = values[p],
                            value1 = values[px],
                            value2 = values[py],
                            value3 = values[pxy],
                            value4 = values[pz],
                            value5 = values[pxz],
                            value6 = values[pyz],
                            value7 = values[pxyz];

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (isoLevel - value0) / (value1 - value0);
                        vertList[0] = lerp(position, new float[]{position[0] + voxDim[0], position[1], position[2]}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (isoLevel - value1) / (value3 - value1);
                        vertList[1] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (isoLevel - value2) / (value3 - value2);
                        vertList[2] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (isoLevel - value0) / (value2 - value0);
                        vertList[3] = lerp(position, new float[]{position[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (isoLevel - value4) / (value5 - value4);
                        vertList[4] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (isoLevel - value5) / (value7 - value5);
                        vertList[5] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (isoLevel - value6) / (value7 - value6);
                        vertList[6] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (isoLevel - value4) / (value6 - value4);
                        vertList[7] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (isoLevel - value0) / (value4 - value0);
                        vertList[8] = lerp(position, new float[]{position[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (isoLevel - value1) / (value5 - value1);
                        vertList[9] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (isoLevel - value3) / (value7 - value3);
                        vertList[10] = lerp(new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1]+ voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (isoLevel - value2) / (value6 - value2);
                        vertList[11] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices normalized with the maximal possible value
                        vertices.add(new float[] {vertList[index3][0] / maxAxisVal - 0.5f, vertList[index3][1] / maxAxisVal - 0.5f, vertList[index3][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index2][0] / maxAxisVal - 0.5f, vertList[index2][1] / maxAxisVal - 0.5f, vertList[index2][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index1][0] / maxAxisVal - 0.5f, vertList[index1][1] / maxAxisVal - 0.5f, vertList[index1][2] / maxAxisVal - 0.5f});

                        i += 3;
                    }
                }
            }
        }

        callback.setVertices(vertices);
        callback.run();
    }

    static void marchingCubesInt(int[] values, int[] volDim, int volZFull, float[] voxDim, int isoLevel, int offset, CallbackMC callback) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];


        // Calculate maximal possible axis value (used in vertice normalization)
        float maxX = voxDim[0] * (volDim[0] - 1);
        float maxY = voxDim[1] * (volDim[1] - 1);
        float maxZ = voxDim[2] * (volZFull - 1);
        float maxAxisVal = Math.max(maxX, Math.max(maxY, maxZ));

        // Volume iteration
        for (int z = 0; z < volDim[2] - 1; z++) {
            for (int y = 0; y < volDim[1] - 1; y++) {
                for (int x = 0; x < volDim[0] - 1; x++) {

                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * (z + offset)),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    //							  X              Y                    Z
                    float position[] = new float[]{x * voxDim[0], y * voxDim[1], (z + offset) * voxDim[2]};

                    // Voxel intensities
                    int value0 = values[p],
                            value1 = values[px],
                            value2 = values[py],
                            value3 = values[pxy],
                            value4 = values[pz],
                            value5 = values[pxz],
                            value6 = values[pyz],
                            value7 = values[pxyz];

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (isoLevel - value0) / (value1 - value0);
                        vertList[0] = lerp(position, new float[]{position[0] + voxDim[0], position[1], position[2]}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (isoLevel - value1) / (value3 - value1);
                        vertList[1] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (isoLevel - value2) / (value3 - value2);
                        vertList[2] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (isoLevel - value0) / (value2 - value0);
                        vertList[3] = lerp(position, new float[]{position[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (isoLevel - value4) / (value5 - value4);
                        vertList[4] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (isoLevel - value5) / (value7 - value5);
                        vertList[5] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (isoLevel - value6) / (value7 - value6);
                        vertList[6] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (isoLevel - value4) / (value6 - value4);
                        vertList[7] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (isoLevel - value0) / (value4 - value0);
                        vertList[8] = lerp(position, new float[]{position[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (isoLevel - value1) / (value5 - value1);
                        vertList[9] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (isoLevel - value3) / (value7 - value3);
                        vertList[10] = lerp(new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1]+ voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (isoLevel - value2) / (value6 - value2);
                        vertList[11] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices normalized with the maximal possible value
                        vertices.add(new float[] {vertList[index3][0] / maxAxisVal - 0.5f, vertList[index3][1] / maxAxisVal - 0.5f, vertList[index3][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index2][0] / maxAxisVal - 0.5f, vertList[index2][1] / maxAxisVal - 0.5f, vertList[index2][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index1][0] / maxAxisVal - 0.5f, vertList[index1][1] / maxAxisVal - 0.5f, vertList[index1][2] / maxAxisVal - 0.5f});

                        i += 3;
                    }
                }
            }
        }

        callback.setVertices(vertices);
        callback.run();
    }

    static void marchingCubesFloat(float[] values, int[] volDim, int volZFull, float[] voxDim, float isoLevel, int offset, CallbackMC callback) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];


        // Calculate maximal possible axis value (used in vertice normalization)
        float maxX = voxDim[0] * (volDim[0] - 1);
        float maxY = voxDim[1] * (volDim[1] - 1);
        float maxZ = voxDim[2] * (volZFull - 1);
        float maxAxisVal = Math.max(maxX, Math.max(maxY, maxZ));

        // Volume iteration
        for (int z = 0; z < volDim[2] - 1; z++) {
            for (int y = 0; y < volDim[1] - 1; y++) {
                for (int x = 0; x < volDim[0] - 1; x++) {

                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * (z + offset)),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    //							  X              Y                    Z
                    float position[] = new float[]{x * voxDim[0], y * voxDim[1], (z + offset) * voxDim[2]};

                    // Voxel intensities
                    float value0 = values[p],
                            value1 = values[px],
                            value2 = values[py],
                            value3 = values[pxy],
                            value4 = values[pz],
                            value5 = values[pxz],
                            value6 = values[pyz],
                            value7 = values[pxyz];

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (float) ((isoLevel - value0) / (value1 - value0));
                        vertList[0] = lerp(position, new float[]{position[0] + voxDim[0], position[1], position[2]}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (float) ((isoLevel - value1) / (value3 - value1));
                        vertList[1] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (float) ((isoLevel - value2) / (value3 - value2));
                        vertList[2] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (float) ((isoLevel - value0) / (value2 - value0));
                        vertList[3] = lerp(position, new float[]{position[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (float) ((isoLevel - value4) / (value5 - value4));
                        vertList[4] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (float) ((isoLevel - value5) / (value7 - value5));
                        vertList[5] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (float) ((isoLevel - value6) / (value7 - value6));
                        vertList[6] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (float) ((isoLevel - value4) / (value6 - value4));
                        vertList[7] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (float) ((isoLevel - value0) / (value4 - value0));
                        vertList[8] = lerp(position, new float[]{position[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (float) ((isoLevel - value1) / (value5 - value1));
                        vertList[9] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (float) ((isoLevel - value3) / (value7 - value3));
                        vertList[10] = lerp(new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1]+ voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (float) ((isoLevel - value2) / (value6 - value2));
                        vertList[11] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices normalized with the maximal possible value
                        vertices.add(new float[] {vertList[index3][0] / maxAxisVal - 0.5f, vertList[index3][1] / maxAxisVal - 0.5f, vertList[index3][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index2][0] / maxAxisVal - 0.5f, vertList[index2][1] / maxAxisVal - 0.5f, vertList[index2][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index1][0] / maxAxisVal - 0.5f, vertList[index1][1] / maxAxisVal - 0.5f, vertList[index1][2] / maxAxisVal - 0.5f});

                        i += 3;
                    }
                }
            }
        }

        callback.setVertices(vertices);
        callback.run();
    }

    static void marchingCubesDouble(double[] values, int[] volDim, int volZFull, float[] voxDim, double isoLevel, int offset, CallbackMC callback) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];


        // Calculate maximal possible axis value (used in vertice normalization)
        float maxX = voxDim[0] * (volDim[0] - 1);
        float maxY = voxDim[1] * (volDim[1] - 1);
        float maxZ = voxDim[2] * (volZFull - 1);
        float maxAxisVal = Math.max(maxX, Math.max(maxY, maxZ));

        // Volume iteration
        for (int z = 0; z < volDim[2] - 1; z++) {
            for (int y = 0; y < volDim[1] - 1; y++) {
                for (int x = 0; x < volDim[0] - 1; x++) {

                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * (z + offset)),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    //							  X              Y                    Z
                    float position[] = new float[]{x * voxDim[0], y * voxDim[1], (z + offset) * voxDim[2]};

                    // Voxel intensities
                    double value0 = values[p],
                            value1 = values[px],
                            value2 = values[py],
                            value3 = values[pxy],
                            value4 = values[pz],
                            value5 = values[pxz],
                            value6 = values[pyz],
                            value7 = values[pxyz];

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (float) ((isoLevel - value0) / (value1 - value0));
                        vertList[0] = lerp(position, new float[]{position[0] + voxDim[0], position[1], position[2]}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (float) ((isoLevel - value1) / (value3 - value1));
                        vertList[1] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (float) ((isoLevel - value2) / (value3 - value2));
                        vertList[2] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (float) ((isoLevel - value0) / (value2 - value0));
                        vertList[3] = lerp(position, new float[]{position[0], position[1] + voxDim[1], position[2]}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (float) ((isoLevel - value4) / (value5 - value4));
                        vertList[4] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (float) ((isoLevel - value5) / (value7 - value5));
                        vertList[5] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (float) ((isoLevel - value6) / (value7 - value6));
                        vertList[6] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (float) ((isoLevel - value4) / (value6 - value4));
                        vertList[7] = lerp(new float[]{position[0], position[1], position[2] + voxDim[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (float) ((isoLevel - value0) / (value4 - value0));
                        vertList[8] = lerp(position, new float[]{position[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (float) ((isoLevel - value1) / (value5 - value1));
                        vertList[9] = lerp(new float[]{position[0] + voxDim[0], position[1], position[2]}, new float[]{position[0] + voxDim[0], position[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (float) ((isoLevel - value3) / (value7 - value3));
                        vertList[10] = lerp(new float[]{position[0] + voxDim[0], position[1] + voxDim[1], position[2]}, new float[]{position[0] + voxDim[0], position[1]+ voxDim[1], position[2] + voxDim[2]}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (float) ((isoLevel - value2) / (value6 - value2));
                        vertList[11] = lerp(new float[]{position[0], position[1] + voxDim[1], position[2]}, new float[]{position[0], position[1] + voxDim[1], position[2] + voxDim[2]}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices normalized with the maximal possible value
                        vertices.add(new float[] {vertList[index3][0] / maxAxisVal - 0.5f, vertList[index3][1] / maxAxisVal - 0.5f, vertList[index3][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index2][0] / maxAxisVal - 0.5f, vertList[index2][1] / maxAxisVal - 0.5f, vertList[index2][2] / maxAxisVal - 0.5f});
                        vertices.add(new float[] {vertList[index1][0] / maxAxisVal - 0.5f, vertList[index1][1] / maxAxisVal - 0.5f, vertList[index1][2] / maxAxisVal - 0.5f});

                        i += 3;
                    }
                }
            }
        }

        callback.setVertices(vertices);
        callback.run();
    }
    
    public static ArrayList<float[]> marchingCubes(Array values, Array xc, Array yc, Array zc, float isoLevel) {

        ArrayList<float[]> vertices = new ArrayList<>();
        // Actual position along edge weighted according to function values.
        float vertList[][] = new float[12][3];

        int[] shape = values.getShape();
        int[] volDim = new int[]{shape[2], shape[1], shape[0]};

        // Volume iteration
        float xx, yy, zz;
        for (int z = 0; z < volDim[2] - 1; z++) {
            zz = zc.getFloat(z);
            for (int y = 0; y < volDim[1] - 1; y++) {
                yy = yc.getFloat(y);
                for (int x = 0; x < volDim[0] - 1; x++) {
                    xx = xc.getFloat(x);
                    // Indices pointing to cube vertices
                    //              pyz  ___________________  pxyz
                    //                  /|                 /|
                    //                 / |                / |
                    //                /  |               /  |
                    //          pz   /___|______________/pxz|
                    //              |    |              |   |
                    //              |    |              |   |
                    //              | py |______________|___| pxy
                    //              |   /               |   /
                    //              |  /                |  /
                    //              | /                 | /
                    //              |/__________________|/
                    //             p                     px

                    int p = x + (volDim[0] * y) + (volDim[0] * volDim[1] * z),
                            px = p + 1,
                            py = p + volDim[0],
                            pxy = py + 1,
                            pz = p + volDim[0] * volDim[1],
                            pxz = px + volDim[0] * volDim[1],
                            pyz = py + volDim[0] * volDim[1],
                            pxyz = pxy + volDim[0] * volDim[1];

                    // X, Y, Z position
                    //float position[] = new float[]{xx, yy, zz};

                    // Voxel intensities
                    float value0 = values.getFloat(p),
                            value1 = values.getFloat(px),
                            value2 = values.getFloat(py),
                            value3 = values.getFloat(pxy),
                            value4 = values.getFloat(pz),
                            value5 = values.getFloat(pxz),
                            value6 = values.getFloat(pyz),
                            value7 = values.getFloat(pxyz);

                    // Voxel is active if its intensity is above isolevel
                    int cubeindex = 0;
                    if (value0 > isoLevel) cubeindex |= 1;
                    if (value1 > isoLevel) cubeindex |= 2;
                    if (value2 > isoLevel) cubeindex |= 8;
                    if (value3 > isoLevel) cubeindex |= 4;
                    if (value4 > isoLevel) cubeindex |= 16;
                    if (value5 > isoLevel) cubeindex |= 32;
                    if (value6 > isoLevel) cubeindex |= 128;
                    if (value7 > isoLevel) cubeindex |= 64;

                    // Fetch the triggered edges
                    int bits = TablesMC.MC_EDGE_TABLE[cubeindex];

                    // If no edge is triggered... skip
                    if (bits == 0) continue;

                    // Interpolate the positions based od voxel intensities
                    float mu = 0.5f;

                    // bottom of the cube
                    if ((bits & 1) != 0) {
                        mu = (float) ((isoLevel - value0) / (value1 - value0));
                        vertList[0] = lerp(new float[]{xx, yy, zz}, new float[]{xc.getFloat(x + 1), yy, zz}, mu);
                    }
                    if ((bits & 2) != 0) {
                        mu = (float) ((isoLevel - value1) / (value3 - value1));
                        vertList[1] = lerp(new float[]{xc.getFloat(x + 1), yy, zz}, new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zz}, mu);
                    }
                    if ((bits & 4) != 0) {
                        mu = (float) ((isoLevel - value2) / (value3 - value2));
                        vertList[2] = lerp(new float[]{xx, yc.getFloat(y + 1), zz}, new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zz}, mu);
                    }
                    if ((bits & 8) != 0) {
                        mu = (float) ((isoLevel - value0) / (value2 - value0));
                        vertList[3] = lerp(new float[]{xx, yy, zz}, new float[]{xx, yc.getFloat(y + 1), zz}, mu);
                    }
                    // top of the cube
                    if ((bits & 16) != 0) {
                        mu = (float) ((isoLevel - value4) / (value5 - value4));
                        vertList[4] = lerp(new float[]{xx, yy, zc.getFloat(z + 1)}, new float[]{xc.getFloat(x + 1), yy, zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 32) != 0) {
                        mu = (float) ((isoLevel - value5) / (value7 - value5));
                        vertList[5] = lerp(new float[]{xc.getFloat(x + 1), yy, zc.getFloat(z + 1)}, new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 64) != 0) {
                        mu = (float) ((isoLevel - value6) / (value7 - value6));
                        vertList[6] = lerp(new float[]{xx, yc.getFloat(y + 1), zc.getFloat(z + 1)}, new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 128) != 0) {
                        mu = (float) ((isoLevel - value4) / (value6 - value4));
                        vertList[7] = lerp(new float[]{xx, yy, zc.getFloat(z + 1)}, new float[]{xx, yc.getFloat(y + 1), zc.getFloat(z + 1)}, mu);
                    }
                    // vertical lines of the cube
                    if ((bits & 256) != 0) {
                        mu = (float) ((isoLevel - value0) / (value4 - value0));
                        vertList[8] = lerp(new float[]{xx, yy, zz}, new float[]{xx, yy, zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 512) != 0) {
                        mu = (float) ((isoLevel - value1) / (value5 - value1));
                        vertList[9] = lerp(new float[]{xc.getFloat(x + 1), yy, zz}, new float[]{xc.getFloat(x + 1), yy, zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 1024) != 0) {
                        mu = (float) ((isoLevel - value3) / (value7 - value3));
                        vertList[10] = lerp(new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zz}, new float[]{xc.getFloat(x + 1), yc.getFloat(y + 1), zc.getFloat(z + 1)}, mu);
                    }
                    if ((bits & 2048) != 0) {
                        mu = (float) ((isoLevel - value2) / (value6 - value2));
                        vertList[11] = lerp(new float[]{xx, yc.getFloat(y + 1), zz}, new float[]{xx, yc.getFloat(y + 1), zc.getFloat(z + 1)}, mu);
                    }

                    // construct triangles -- get correct vertices from triTable.
                    int i = 0;
                    // "Re-purpose cubeindex into an offset into triTable."
                    cubeindex <<= 4;

                    while (TablesMC.MC_TRI_TABLE[cubeindex + i] != -1) {
                        int index1 = TablesMC.MC_TRI_TABLE[cubeindex + i];
                        int index2 = TablesMC.MC_TRI_TABLE[cubeindex + i + 1];
                        int index3 = TablesMC.MC_TRI_TABLE[cubeindex + i + 2];

                        // Add triangles vertices
                        vertices.add(new float[] {vertList[index3][0], vertList[index3][1], vertList[index3][2]});
                        vertices.add(new float[] {vertList[index2][0], vertList[index2][1], vertList[index2][2]});
                        vertices.add(new float[] {vertList[index1][0], vertList[index1][1], vertList[index1][2]});

                        i += 3;
                    }
                }
            }
        }
        
        return vertices;
    }
}
