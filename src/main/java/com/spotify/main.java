package com.spotify;


import com.formdev.flatlaf.FlatDarkLaf;


public class main {

    public static void main(String[] args) {

        FlatDarkLaf.setup();
        
        Inicio inicio = new Inicio();
        
        inicio.setVisible(true);
        
    }
}