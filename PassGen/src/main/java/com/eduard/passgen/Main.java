/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.passgen;

import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author eduard
 */
public class Main {
    public static int readNumber(Scanner scn, String message){
        System.out.println(message);
        try{
            return Integer.parseInt(scn.next());
        }catch(NumberFormatException nFE){
            return readNumber(scn, message);
        }
    }
    public static void main(String[] args){
        Scanner scn = new Scanner(System.in);
        int seed = readNumber(scn, "Introduce the seed please.");
        int nlen = readNumber(scn, "Introduce the password length.");
        int nPss = readNumber(scn, "Introduce the number of passwords which you want to generate.");
        String[] base={
            "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
            "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
            "0","1","2","3","4","5","6","7","8","9",};
        Random rnd = new Random(seed);
        for(int j=0; j<nPss; j++){
            String pass="";
            for( int i=0; i< nlen; i++){
                pass+=base[(int)Math.floor(rnd.nextDouble()*base.length)];
            }
            System.out.println(pass);
        }
        scn.close();
    }
}
