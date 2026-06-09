/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import javax.swing.DefaultListModel;

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
    
    public DefaultListModel<Cancion> recorrerLista(){
       if(EstaVacio()) return new DefaultListModel<Cancion>();
       
       Cancion temp = inicio;
       DefaultListModel<Cancion> historial = new DefaultListModel<Cancion>();
       
       while(temp != null){
           historial.addElement(temp);
           temp = temp.siguiente;
       }
       
       return historial;
       
       
    }
    
}
