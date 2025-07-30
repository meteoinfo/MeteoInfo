package org.meteoinfo.math.integrate.lsoda.tools;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Data {
    private String filePath;
    private BufferedWriter bufferedWriter;
    private int n;

    public Data(String filepath, String fileName, int neq) throws IOException {
        n=neq;
        filePath = filepath + "/" + fileName;
        // header
        bufferedWriter = new BufferedWriter(new FileWriter(filePath));
        bufferedWriter.write("t,");
        for (int i=1; i<neq; i++)
            bufferedWriter.write("y_"+i+",");
        bufferedWriter.write("y_"+neq);
        bufferedWriter.flush();
    }

    public void write(double t, double[] y) {
        try{
            bufferedWriter.newLine();
            bufferedWriter.write(t +",");
            for (int i=1; i<n; i++)
                bufferedWriter.write(y[i]+",");
            bufferedWriter.write(Double.toString(y[n]));
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error occurred in file writing.");
        }
    }

    public void closeWriter() throws IOException {
        bufferedWriter.close();
    }
}
