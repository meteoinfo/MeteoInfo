/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl.mc;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Primoz on 11. 07. 2016.
 */
public class ExtractHandler {
    public static void extractHandlerChar(File inputFile, File outFile, final int[] size, final float voxSize[], final char isoValue, int nThreads) {
        char[] scalarField;

        if (inputFile != null) {
            System.out.println("PROGRESS: Reading input data.");
            try {
                int idx = 0;
                scalarField = new char[size[0] * size[1] * size[2]];

                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                while (in.available() > 0) {
                    // Size does not match
                    if (idx >= scalarField.length) {
                        in.close();
                        System.out.println("Invalid volume size was specified.");
                        return;
                    }

                    scalarField[idx++] = (char) in.readByte();
                }

                in.close();

                // Size does not match
                if (idx != scalarField.length) {
                    System.out.println("Invalid volume size was specified.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while reading the volume");
                return;
            }
        }
        else {
            System.out.println("PROGRESS: Generating volume data.");
            scalarField = VolumeGenerator.generateScalarFieldChar(size);
        }

        final char[] finalScalarField = scalarField;

        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float []>> results = new ArrayList<>();

        // Thread work distribution
        int remainder = size[2] % nThreads;
        int segment = size[2] / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        System.out.println("PROGRESS: Executing marching cubes.");

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;


            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubesChar(finalScalarField, new int[]{size[0], size[1], paddedSegmentSize}, size[2], voxSize, isoValue, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i <threads.size(); i ++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("PROGRESS: Writing results to output file.");
        outputToFile(results, outFile);
    }

    public static void extractHandlerShort(File inputFile, File outFile, final int[] size, final float voxSize[], final short isoValue, int nThreads) {
        short[] scalarField;

        if (inputFile != null) {
            System.out.println("PROGRESS: Reading input data.");
            try {
                int idx = 0;
                scalarField = new short[size[0] * size[1] * size[2]];

                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                while (in.available() > 0) {
                    // Size does not match
                    if (idx >= scalarField.length) {
                        in.close();
                        System.out.println("Invalid volume size was specified.");
                        return;
                    }

                    scalarField[idx++] = in.readShort();
                }

                in.close();

                // Size does not match
                if (idx != scalarField.length) {
                    System.out.println("Invalid volume size was specified.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while reading the volume");
                return;
            }
        }
        else {
            System.out.println("PROGRESS: Generating volume data.");
            scalarField = VolumeGenerator.generateScalarFieldShort(size);
        }

        final short[] finalScalarField = scalarField;

        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float []>> results = new ArrayList<>();

        // Thread work distribution
        int remainder = size[2] % nThreads;
        int segment = size[2] / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        System.out.println("PROGRESS: Executing marching cubes.");

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;


            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubesShort(finalScalarField, new int[]{size[0], size[1], paddedSegmentSize}, size[2], voxSize, isoValue, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i <threads.size(); i ++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("PROGRESS: Writing results to output file.");
        outputToFile(results, outFile);
    }

    public static void extractHandlerInt(File inputFile, File outFile, final int[] size, final float voxSize[], final int isoValue, int nThreads) {
        int[] scalarField;

        if (inputFile != null) {
            System.out.println("PROGRESS: Reading input data.");
            try {
                int idx = 0;
                scalarField = new int[size[0] * size[1] * size[2]];

                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                while (in.available() > 0) {
                    // Size does not match
                    if (idx >= scalarField.length) {
                        in.close();
                        System.out.println("Invalid volume size was specified.");
                        return;
                    }

                    scalarField[idx++] = in.readInt();
                }

                in.close();

                // Size does not match
                if (idx != scalarField.length) {
                    System.out.println("Invalid volume size was specified.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while reading the volume");
                return;
            }
        }
        else {
            System.out.println("PROGRESS: Generating volume data.");
            scalarField = VolumeGenerator.generateScalarFieldInt(size);
        }

        final int[] finalScalarField = scalarField;

        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float []>> results = new ArrayList<>();

        // Thread work distribution
        int remainder = size[2] % nThreads;
        int segment = size[2] / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        System.out.println("PROGRESS: Executing marching cubes.");

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;


            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubesInt(finalScalarField, new int[]{size[0], size[1], paddedSegmentSize}, size[2], voxSize, isoValue, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i <threads.size(); i ++) {
            System.out.println("PROGRESS: Reading input data.");
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("PROGRESS: Writing results to output file.");
        outputToFile(results, outFile);
    }

    public static void extractHandlerFloat(File inputFile, File outFile, final int[] size, final float voxSize[], final float isoValue, final int nThreads) {
        float[] scalarField;

        if (inputFile != null) {
            System.out.println("PROGRESS: Reading input data.");
            try {
                int idx = 0;
                scalarField = new float[size[0] * size[1] * size[2]];

                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                while (in.available() > 0) {
                    // Size does not match
                    if (idx >= scalarField.length) {
                        in.close();
                        System.out.println("Invalid volume size was specified.");
                        return;
                    }

                    scalarField[idx++] = in.readFloat();
                }

                in.close();

                // Size does not match
                if (idx != scalarField.length) {
                    System.out.println("Invalid volume size was specified.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while reading the volume");
                return;
            }
        }
        else {
            System.out.println("PROGRESS: Generating volume data.");
            scalarField = VolumeGenerator.generateScalarFieldFloat(size);
        }

        final float[] finalScalarField = scalarField;

        // TIMER
        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float []>> results = new ArrayList<>();

        // Thread work distribution
        int remainder = size[2] % nThreads;
        int segment = size[2] / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        System.out.println("PROGRESS: Executing marching cubes.");

        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;


            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubesFloat(finalScalarField, new int[]{size[0], size[1], paddedSegmentSize}, size[2], voxSize, isoValue, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i <threads.size(); i ++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("PROGRESS: Writing results to output file.");
        outputToFile(results, outFile);
    }

    public static void extractHandlerDouble(File inputFile, File outFile, final int[] size, final float voxSize[], final double isoValue, int nThreads) {
        double[] scalarField;

        if (inputFile != null) {
            try {
                int idx = 0;
                scalarField = new double[size[0] * size[1] * size[2]];

                DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
                while (in.available() > 0) {
                    // Size does not match
                    if (idx >= scalarField.length) {
                        in.close();
                        System.out.println("Invalid volume size was specified.");
                        return;
                    }

                    scalarField[idx++] = in.readDouble();
                }

                in.close();

                // Size does not match
                if (idx != scalarField.length) {
                    System.out.println("Invalid volume size was specified.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while reading the volume");
                return;
            }
        }
        else {
            System.out.println("PROGRESS: Generating volume data.");
            scalarField = VolumeGenerator.generateScalarFieldDouble(size);
        }

        final double[] finalScalarField = scalarField;

        // TIMER
        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<ArrayList<float []>> results = new ArrayList<>();

        // Thread work distribution
        int remainder = size[2] % nThreads;
        int segment = size[2] / nThreads;

        // Z axis offset for vertice position calculation
        int zAxisOffset = 0;

        System.out.println("PROGRESS: Executing marching cubes.");
        for (int i = 0; i < nThreads; i++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;

            // Padding needs to be added to correctly close the gaps between segments
            final int paddedSegmentSize = (i != nThreads - 1) ? segmentSize + 1 : segmentSize;


            // Finished callback
            final CallbackMC callback = new CallbackMC() {
                @Override
                public void run() {
                    results.add(getVertices());
                }
            };

            // Java...
            final int finalZAxisOffset = zAxisOffset;

            // Start the thread
            Thread t = new Thread() {
                public void run() {
                    MarchingCubes.marchingCubesDouble(finalScalarField, new int[]{size[0], size[1], paddedSegmentSize}, size[2], voxSize, isoValue, finalZAxisOffset, callback);
                }
            };

            threads.add(t);
            t.start();

            // Correct offsets for next iteration
            zAxisOffset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i <threads.size(); i ++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("PROGRESS: Writing results to output file.");
        outputToFile(results, outFile);
    }

    private static void outputToFile(ArrayList<ArrayList<float[]>> results, File outFile) {
        try {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outFile));

            int idx = 0;
            for (int i = 0; i < results.size(); i++) {
                ArrayList<float[]> resSeg = results.get(i);

                for (int j = 0; j < resSeg.size(); j++) {
                    if (idx % 3 == 0) {
                        stream.write(("f " + (idx + 1) + " " + (idx + 2) + " " + (idx + 3) + "\n").getBytes());
                    }
                    idx ++;

                    stream.write(("v " + resSeg.get(j)[0] + " " + resSeg.get(j)[1] + " " + resSeg.get(j)[2] + "\n").getBytes());
                }
            }

            stream.flush();
            stream.close();
        }
        catch (Exception e) {
            System.out.println("Something went wrong while writing to the output file");
            return;
        }
    }
}
