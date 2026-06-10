/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author pchin
 */
public class Historial {
    public Cancion inicio;
    public Cancion fin;
    public int contador;
    
    private static Historial instancia;
    
    private Historial(){
        inicio = fin = null;
    }
    
    public static Historial getInstancia() {
        if (instancia == null) {
            instancia = new Historial();
        }
        return instancia;
    }
    
    public boolean EstaVacio(){
        if(inicio == null && fin == null) return true;
        return false;
    }
    
    public void Insertar(Cancion cancion){
        Cancion nuevo = new Cancion(cancion.nombre, cancion.artista, cancion.album, cancion.genero, cancion.duracionSeg, cancion.tamaño, cancion.ruta, cancion.año);
        
        if(EstaVacio()){
            inicio = fin = nuevo;
            return;
        }
        
        nuevo.siguiente = inicio;
        inicio = nuevo;
    }
    
    public List<Cancion> recorrerLista(){
       if(EstaVacio()) return new ArrayList<Cancion>();
       
       Cancion temp = inicio;
       List<Cancion> historial = new ArrayList<Cancion>();
       
       while(temp != null){
           historial.add(temp);
           temp = temp.siguiente;
       }
       
       return historial;
    }
    
}
