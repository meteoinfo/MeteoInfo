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
public class BenchmarkHandler {

    public static void benchmarkChar(File inputFile, File outFile, final int[] size, final float voxSize[], final char isoValue, int nThreadsMin, int nThreadsMax, int iterations) {
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

        System.out.println("PROGRESS: Performing benchmark.");
        StringBuilder benchmarkResults = new StringBuilder();

        for (int nThreads = nThreadsMin; nThreads <= nThreadsMax; nThreads++) {

            final ArrayList<Double> times = new ArrayList<>();

            for (int it = 0; it < iterations; it++) {

                // TIMER
                final long start = System.currentTimeMillis();

                ArrayList<Thread> threads = new ArrayList<>();
                final ArrayList<ArrayList<float []>> results = new ArrayList<>();


                // Thread work distribution
                int remainder = size[2] % nThreads;
                int segment = size[2] / nThreads;

                // Z axis offset for vertice position calculation
                int zAxisOffset = 0;


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

                // Time measurement
                long end= System.currentTimeMillis();
                times.add((end - start) / 1000.0);
            }

            double sumTime = 0.0, avgTime;

            for (int i = 0; i < times.size(); i++) {
                sumTime += times.get(i);
            }
            // Average time
            avgTime = sumTime / iterations;

            // Standard deviation
            double sd = 0;

            for (int i = 0; i < times.size(); i++) {
                sd += Math.pow((times.get(i) - avgTime), 2);
            }

            sd = Math.sqrt(sd / iterations);


            System.out.println("Threads: " + nThreads);
            System.out.println("Iterations: " + iterations);
            System.out.println("Average time: " + avgTime + "s");
            System.out.println("Standard deviation: " + sd);
            System.out.println("-------------------------------------------------------------");

            benchmarkResults.append("Threads: " + nThreads);
            benchmarkResults.append("Iterations: " + iterations);
            benchmarkResults.append("Average time: " + avgTime + "s");
            benchmarkResults.append("Standard deviation: " + sd);
            benchmarkResults.append("-------------------------------------------------------------");
        }

        System.out.println("PROGRESS: Writing results to output file.");

        if (outFile != null) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile));
                osw.write(benchmarkResults.toString());
            }
            catch (Exception e) {
                System.out.println("Something went wrong while writing the results to file.");
                return;
            }
        }
    }

    public static void benchmarkShort(File inputFile, File outFile, final int[] size, final float voxSize[], final short isoValue, int nThreadsMin, int nThreadsMax, int iterations) {
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

        System.out.println("PROGRESS: Performing benchmark.");
        StringBuilder benchmarkResults = new StringBuilder();

        for (int nThreads = nThreadsMin; nThreads <= nThreadsMax; nThreads++) {

            final ArrayList<Double> times = new ArrayList<>();

            for (int it = 0; it < iterations; it++) {

                // TIMER
                final long start = System.currentTimeMillis();

                ArrayList<Thread> threads = new ArrayList<>();
                final ArrayList<ArrayList<float []>> results = new ArrayList<>();


                // Thread work distribution
                int remainder = size[2] % nThreads;
                int segment = size[2] / nThreads;

                // Z axis offset for vertice position calculation
                int zAxisOffset = 0;


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

                // Time measurement
                long end= System.currentTimeMillis();
                times.add((end - start) / 1000.0);
            }

            double sumTime = 0.0, avgTime;

            for (int i = 0; i < times.size(); i++) {
                sumTime += times.get(i);
            }
            // Average time
            avgTime = sumTime / iterations;

            // Standard deviation
            double sd = 0;

            for (int i = 0; i < times.size(); i++) {
                sd += Math.pow((times.get(i) - avgTime), 2);
            }

            sd = Math.sqrt(sd / iterations);


            System.out.println("Threads: " + nThreads);
            System.out.println("Iterations: " + iterations);
            System.out.println("Average time: " + avgTime + "s");
            System.out.println("Standard deviation: " + sd);
            System.out.println("-------------------------------------------------------------");

            benchmarkResults.append("Threads: " + nThreads);
            benchmarkResults.append("Iterations: " + iterations);
            benchmarkResults.append("Average time: " + avgTime + "s");
            benchmarkResults.append("Standard deviation: " + sd);
            benchmarkResults.append("-------------------------------------------------------------");
        }

        System.out.println("PROGRESS: Writing results to output file.");

        if (outFile != null) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile));
                osw.write(benchmarkResults.toString());
            }
            catch (Exception e) {
                System.out.println("Something went wrong while writing the results to file.");
                return;
            }
        }
    }

    public static void benchmarkInt(File inputFile, File outFile, final int[] size, final float voxSize[], final int isoValue, int nThreadsMin, int nThreadsMax, int iterations) {
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

        System.out.println("PROGRESS: Performing benchmark.");
        StringBuilder benchmarkResults = new StringBuilder();

        for (int nThreads = nThreadsMin; nThreads <= nThreadsMax; nThreads++) {

            final ArrayList<Double> times = new ArrayList<>();

            for (int it = 0; it < iterations; it++) {

                // TIMER
                final long start = System.currentTimeMillis();

                ArrayList<Thread> threads = new ArrayList<>();
                final ArrayList<ArrayList<float []>> results = new ArrayList<>();


                // Thread work distribution
                int remainder = size[2] % nThreads;
                int segment = size[2] / nThreads;

                // Z axis offset for vertice position calculation
                int zAxisOffset = 0;


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
                    try {
                        threads.get(i).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Time measurement
                long end= System.currentTimeMillis();
                times.add((end - start) / 1000.0);
            }

            double sumTime = 0.0, avgTime;

            for (int i = 0; i < times.size(); i++) {
                sumTime += times.get(i);
            }
            // Average time
            avgTime = sumTime / iterations;

            // Standard deviation
            double sd = 0;

            for (int i = 0; i < times.size(); i++) {
                sd += Math.pow((times.get(i) - avgTime), 2);
            }

            sd = Math.sqrt(sd / iterations);


            System.out.println("Threads: " + nThreads);
            System.out.println("Iterations: " + iterations);
            System.out.println("Average time: " + avgTime + "s");
            System.out.println("Standard deviation: " + sd);
            System.out.println("-------------------------------------------------------------");

            benchmarkResults.append("Threads: " + nThreads);
            benchmarkResults.append("Iterations: " + iterations);
            benchmarkResults.append("Average time: " + avgTime + "s");
            benchmarkResults.append("Standard deviation: " + sd);
            benchmarkResults.append("-------------------------------------------------------------");
        }

        System.out.println("PROGRESS: Writing results to output file.");

        if (outFile != null) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile));
                osw.write(benchmarkResults.toString());
            }
            catch (Exception e) {
                System.out.println("Something went wrong while writing the results to file.");
                return;
            }
        }
    }

    public static void benchmarkFloat(File inputFile, File outFile, final int[] size, final float voxSize[], final float isoValue, final int nThreadsMin, int nThreadsMax, int iterations) {
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

        System.out.println("PROGRESS: Performing benchmark.");
        StringBuilder benchmarkResults = new StringBuilder();

        for (int nThreads = nThreadsMin; nThreads <= nThreadsMax; nThreads++) {

            final ArrayList<Double> times = new ArrayList<>();

            for (int it = 0; it < iterations; it++) {

                // TIMER
                final long start = System.currentTimeMillis();

                ArrayList<Thread> threads = new ArrayList<>();
                final ArrayList<ArrayList<float []>> results = new ArrayList<>();


                // Thread work distribution
                int remainder = size[2] % nThreads;
                int segment = size[2] / nThreads;

                // Z axis offset for vertice position calculation
                int zAxisOffset = 0;


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

                // Time measurement
                long end= System.currentTimeMillis();
                times.add((end - start) / 1000.0);
            }

            double sumTime = 0.0, avgTime;

            for (int i = 0; i < times.size(); i++) {
                sumTime += times.get(i);
            }
            // Average time
            avgTime = sumTime / iterations;

            // Standard deviation
            double sd = 0;

            for (int i = 0; i < times.size(); i++) {
                sd += Math.pow((times.get(i) - avgTime), 2);
            }

            sd = Math.sqrt(sd / iterations);


            System.out.println("Threads: " + nThreads);
            System.out.println("Iterations: " + iterations);
            System.out.println("Average time: " + avgTime + "s");
            System.out.println("Standard deviation: " + sd);
            System.out.println("-------------------------------------------------------------");

            benchmarkResults.append("Threads: " + nThreads);
            benchmarkResults.append("Iterations: " + iterations);
            benchmarkResults.append("Average time: " + avgTime + "s");
            benchmarkResults.append("Standard deviation: " + sd);
            benchmarkResults.append("-------------------------------------------------------------");
        }

        System.out.println("PROGRESS: Writing results to output file.");

        if (outFile != null) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile));
                osw.write(benchmarkResults.toString());
            }
            catch (Exception e) {
                System.out.println("Something went wrong while writing the results to file.");
                return;
            }
        }
    }

    public static void benchmarkDouble(File inputFile, File outFile, final int[] size, final float voxSize[], final double isoValue, int nThreadsMin, int nThreadsMax, int iterations) {
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

        System.out.println("PROGRESS: Performing benchmark.");
        StringBuilder benchmarkResults = new StringBuilder();

        for (int nThreads = nThreadsMin; nThreads <= nThreadsMax; nThreads++) {

            final ArrayList<Double> times = new ArrayList<>();

            for (int it = 0; it < iterations; it++) {

                // TIMER
                final long start = System.currentTimeMillis();

                ArrayList<Thread> threads = new ArrayList<>();
                final ArrayList<ArrayList<float []>> results = new ArrayList<>();


                // Thread work distribution
                int remainder = size[2] % nThreads;
                int segment = size[2] / nThreads;

                // Z axis offset for vertice position calculation
                int zAxisOffset = 0;


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

                // Time measurement
                long end= System.currentTimeMillis();
                times.add((end - start) / 1000.0);
            }

            double sumTime = 0.0, avgTime;

            for (int i = 0; i < times.size(); i++) {
                sumTime += times.get(i);
            }
            // Average time
            avgTime = sumTime / iterations;

            // Standard deviation
            double sd = 0;

            for (int i = 0; i < times.size(); i++) {
                sd += Math.pow((times.get(i) - avgTime), 2);
            }

            sd = Math.sqrt(sd / iterations);


            System.out.println("Threads: " + nThreads);
            System.out.println("Iterations: " + iterations);
            System.out.println("Average time: " + avgTime + "s");
            System.out.println("Standard deviation: " + sd);
            System.out.println("-------------------------------------------------------------");

            benchmarkResults.append("Threads: " + nThreads);
            benchmarkResults.append("Iterations: " + iterations);
            benchmarkResults.append("Average time: " + avgTime + "s");
            benchmarkResults.append("Standard deviation: " + sd);
            benchmarkResults.append("-------------------------------------------------------------");
        }

        System.out.println("PROGRESS: Writing results to output file.");

        if (outFile != null) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile));
                osw.write(benchmarkResults.toString());
            }
            catch (Exception e) {
                System.out.println("Something went wrong while writing the results to file.");
                return;
            }
        }
    }
}
