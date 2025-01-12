package com.csci5408.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IO {

    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public IO(){}

    public static String readLine(){
        try {
            return IO.bufferedReader.readLine();
        }
        catch (IOException ioException){
            System.out.println(ioException);
            return null;
        }
    }

    public static int read(){
        try {
            return IO.bufferedReader.read();
        }
        catch (IOException ioException){
            System.out.println(ioException);
            return Integer.MIN_VALUE;
        }
    }

    public static <T> void print(T s) {
        System.out.print(s);
    }

    public static <T> void println(T s) {
        System.out.println(s);
    }

}
