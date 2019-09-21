/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl.mc;

import java.io.File;

public class Main {

    private static String usage = "This script may be executed in either benchmark or extract mode. Mode is specified by the first parameter [benchmark, extract].\nParameters: \n\t-input-vol\t Specifies path to the input volume. If this parameter is set volume dimensions(-vol-dim), data type(-data-type) and iso value(-iso) must also be given.\n\t-vol-dim\t Specifies the generated/read volume dimensions. Dimensions should be given as unsigned integers in format; -vol-dim X Y Z.\n\t-data-type\t Specifies the input file or generated data type. Options [char, uchar, short, ushort, int, uint, float, double].\n\t-vox-dim\t Specifies voxel dimensions used in mesh construction. Dimensions should be given as floating point numbers in format: -vox-dim X Y Z.\n\t-nThread\t Number of threads used in Marching cubes algorithm.This parameter can be either given as a single unsigned integer value or two unsigned integer values in benchmark mode, specifying the range of thread executions that will be tested.\n\t-iter\t\t Used only in benchmark mode to determine how many iterations should be executed for each configuration.\n\t-iso\t\t Isovalue that is used as a threshold for determining active voxels. Type should match the data type.\n\t-o\t\t Path to output file. In extract mode the mesh is written to file in .obj format [required]. In benchmark mode the results are written to file.\n";;

    private static boolean isUint (String input) {
        try {
            return (Integer.parseInt(input) >= 0);
        } catch (NumberFormatException  e) {
            return false;
        }
    }

    private static boolean isFloat (String input) {
        try {
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException  e) {
            return false;
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println(usage);
            return;
        } else if (args[0].equals("-help")) {
            System.out.println(usage);
        }

        // Benchmark or extract mode
        boolean benchmark = false;

        // Default num of threads is max available
        int nThreadsMin = java.lang.Thread.activeCount();
        if (nThreadsMin == 0) {
            nThreadsMin = 1;
        }
        int nThreadsMax = nThreadsMin;

        File inputFile = null;
        File outFile = null;
        String type = null;
        String isoValueStr = null;
        int iterations = 10;    // Default 10 iterations per benchmark

        boolean customSizeSpecified = false;
        int[] size = {64, 64, 64};
        float[] voxSize = {1.0f, 1.0f, 1.0f};

        //region PARAMETER PARSING
        // Read execution type
        if (args[0].equals("benchmark")) {
            benchmark = true;
        } else if (!args[0].equals("extract")) {
            System.out.println("Invalid execution type. Valid options [extract, benchmark]");
            return;
        }

        // Flag parsing
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-input-vol")) {
                // Volume path specified
                // Output file path is specified
                if (i + 1 >= args.length || args[i + 1].charAt(0) == '-') {
                    System.out.println("Missing file path after -input-vol flag.");
                    return;
                }

                // Store the file name and offset iterator
                inputFile = new File(args[++i]);

                if (!inputFile.exists() || inputFile.isDirectory()) {
                    System.out.println("Specified volume file does not exist.");
                    return;
                }
            } else if (args[i].equals("-vol-dim")) {
                // Volume dimensions are given
                if (i + 3 >= args.length || args[i + 1].charAt(0) == '-' || args[i + 2].charAt(0) == '-' || args[i + 3].charAt(0) == '-') {
                    System.out.println("Missing volume dimensions after -vol-dim flag.");
                    return;
                }

                String x = (args[++i]);
                String y = (args[++i]);
                String z = (args[++i]);

                if (!isUint(x) || !isUint(y) || !isUint(z)) {
                    System.out.println("Invalid volume dimensions format. Specify dimensions as three unsigned integers.");
                    return;
                }

                customSizeSpecified = true;
                size[0] = Integer.parseInt(x);
                size[1] = Integer.parseInt(y);
                size[2] = Integer.parseInt(z);
            } else if (args[i].equals("-vox-dim")) {
                // Voxel dimensions are given
                if (i + 3 >= args.length) {
                    System.out.println("Missing voxel dimensions after -vox-dim flag.");
                    return;
                }

                String x = args[++i];
                String y = args[++i];
                String z = args[++i];

                if (!isFloat(x) || !isFloat(y) || !isFloat(z)) {
                    System.out.println("Invalid voxel dimensions format. Specify voxel dimensions as three positive floats.");
                    return;
                }

                voxSize[0] = Float.parseFloat(x);
                voxSize[0] = Float.parseFloat(y);
                voxSize[0] = Float.parseFloat(z);
            } else if (args[i].equals("-nThread")) {
                // Number of threads is given
                // FIRST VALUE
                if (i + 1 >= args.length || args[i + 1].charAt(0) == '-') {
                    System.out.println("Missing number or range of threads after -nThread flag.");
                    return;
                }

                // Validate first number
                String tmp = args[++i];

                if (!isUint(tmp)) {
                    System.out.println("Invalid nThread value format. Specify unsigned integer value or two if range.");
                    return;
                }

                // Parse C-str
                nThreadsMin = Integer.parseInt(tmp);

                // SECOND VALUE (If given)
                if (i + 1 < args.length && args[i + 1].charAt(0) != '-') {
                    // Validate second number
                    tmp = args[++i];
                    if (!isUint(tmp)) {
                        System.out.println("Invalid nThread value format. Specify unsigned integer value or two if range.");
                        return;
                    }

                    // Parse C-str
                    nThreadsMax = Integer.parseInt(tmp);
                } else {
                    nThreadsMax = nThreadsMin;
                }

            } else if (args[i].equals("-iso")) {
                // ISO value is given
                if (i + 1 >= args.length) {
                    System.out.println("Missing iso value after -iso flag.");
                    return;
                }

                isoValueStr = args[++i];

                if (!isFloat(isoValueStr)) {
                    System.out.println("Invalid iso value format. Please specify float.");
                    return;
                }
            } else if (args[i].equals("-iter")) {
                // ISO value is given
                if (i + 1 >= args.length) {
                    System.out.println("Missing number of iterations after -iter flag.");
                    return;
                }

                String iterationsStr = args[++i];

                if (!isUint(iterationsStr)) {
                    System.out.println("Invalid iterations value format. Please specify unsigned integer.");
                    return;
                }

                iterations = Integer.parseInt(iterationsStr);
            } else if (args[i].equals("-o")) {
                // Output file path is specified
                if (i + 1 >= args.length || args[i + 1].charAt(0) == '-') {
                    System.out.println("Missing file path after -o flag.");
                    return;
                }

                // Store the file name and offset iterator
                outFile = new File(args[++i]);

                if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                    System.out.println("Specified output file path is invaild.");
                }
            } else if (args[i].equals("-data-type")) {
                // Volume data type is specified
                if (i + 1 >= args.length || args[i + 1].charAt(0) == '-') {
                    System.out.println("Missing type after -data-type flag.");
                    return;
                }

                // Data type is specified (char, uchar, short, ushort, int, uint, float, double)
                if (!args[i + 1].equals("char") && !args[i + 1].equals("uchar") && !args[i + 1].equals("short") && !args[i + 1].equals("ushort") && args[i + 1].equals("uint") && args[i + 1].equals("float") && args[i + 1].equals("double")) {
                    System.out.println("Invalid data type. Available data types: char, uchar, short, ushort, int, uint, float, double.");
                    return;
                }

                type = args[++i];
            } else {
                System.out.println("Unknown parameter: " + args[i]);
                return;
            }
        }

        if (inputFile != null && (!customSizeSpecified || type == null || isoValueStr == null)) {
            System.out.println("If custom volume is imported, you must input volume dimensions(-vol-dim), data type (-data-type) and iso value (-iso).");
            return;
        }
        //endregion

        if (benchmark) {
            switch (type) {
                case "char":
                    BenchmarkHandler.benchmarkChar(inputFile, outFile, size, voxSize, (char) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "uchar":
                    BenchmarkHandler.benchmarkChar(inputFile, outFile, size, voxSize, (char) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "short":
                    BenchmarkHandler.benchmarkShort(inputFile, outFile, size, voxSize, (short) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "ushort":
                    BenchmarkHandler.benchmarkShort(inputFile, outFile, size, voxSize, (short) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "int":
                    BenchmarkHandler.benchmarkInt(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "uint":
                    BenchmarkHandler.benchmarkInt(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "float":
                    BenchmarkHandler.benchmarkFloat(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Float.parseFloat(isoValueStr) : 0.5f), nThreadsMin, nThreadsMax, iterations);
                    break;
                case "double":
                    BenchmarkHandler.benchmarkDouble(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Double.parseDouble(isoValueStr) : 0.5), nThreadsMin, nThreadsMax, iterations);
                    break;
            }
        } else {
            if (outFile == null) {
                System.out.println("To extract the data the output file path is needed (-o).");
                return;
            }

            switch (type) {
                case "char":
                    ExtractHandler.extractHandlerChar(inputFile, outFile, size, voxSize, (char) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMax);
                    break;
                case "uchar":
                    ExtractHandler.extractHandlerChar(inputFile, outFile, size, voxSize, (char) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMax);
                    break;
                case "short":
                    ExtractHandler.extractHandlerShort(inputFile, outFile, size, voxSize, (short) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMax);
                    break;
                case "ushort":
                    ExtractHandler.extractHandlerShort(inputFile, outFile, size, voxSize, (short) ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0.5), nThreadsMax);
                    break;
                case "int":
                    ExtractHandler.extractHandlerInt(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0), nThreadsMax);
                    break;
                case "uint":
                    ExtractHandler.extractHandlerInt(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Integer.parseInt(isoValueStr) : 0), nThreadsMax);
                    break;
                case "float":
                    ExtractHandler.extractHandlerFloat(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Float.parseFloat(isoValueStr) : 0.5f), nThreadsMax);
                    break;
                case "double":
                    ExtractHandler.extractHandlerDouble(inputFile, outFile, size, voxSize, ((isoValueStr != null) ? Double.parseDouble(isoValueStr) : 0.5), nThreadsMax);
                    break;
            }
        }
    }
}
