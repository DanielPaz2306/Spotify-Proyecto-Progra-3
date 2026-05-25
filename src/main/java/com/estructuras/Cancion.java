/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;



/**
 *
 * @author pchin
 */
public class Cancion {

    public String nombre;
    public String artista;
    public String album;
    public String genero;
    
    public int altura;
    
    public int duracionSeg;
    public String duracionReal;
    public long tamaño; 
    public String ruta;
    public String año;
    
    public Cancion izquierda;
    public Cancion derecha;
    
    public Cancion siguiente;
    public Cancion anterior;

    public Cancion(String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año) {
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.genero = genero;
        this.duracionSeg = duracion;
        int m = duracionSeg / 60;
        int s = duracionSeg % 60;
        this.duracionReal = m + "m " + s + "seg";
        this.tamaño = tamaño;
        this.ruta = ruta;
        this.año = año;
        this.altura = 1;
        this.siguiente = null;
        this.derecha = null;
        this.izquierda = null;
    }
    
    public Cancion(String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año, Cancion siguiente, Cancion anterior){
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.genero = genero;
        this.duracionSeg = duracion;
        int m = duracionSeg / 60;
        int s = duracionSeg % 60;
        this.duracionReal = m + "m " + s + "seg";
        this.tamaño = tamaño;
        this.ruta = ruta;
        this.año = año;
        this.altura = 1;
        this.siguiente = siguiente;
        this.anterior = anterior;
    }

    @Override
    public String toString() {
        return nombre + " - " + artista + " - " + " (" + duracionReal + ")";
    }
    
    
    
}
