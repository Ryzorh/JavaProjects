/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.first;

/**
 *
 * @author eduard
 */
public class Prova {
    Main main;
    public Prova(Main main){
        this.main=main;
    }
    public void setGlobalVariable(){
        main.globalVariable="Variable has been set.";
    }
}
