/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import java.io.File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

/**
 *
 * @author pchin
 */
public class ArbolAVL {
    
    

    private Cancion raiz;
    private int contador;
    
    private static ArbolAVL instancia;

    private ArbolAVL() {}

    public static ArbolAVL getInstancia() {
        if (instancia == null) {
            instancia = new ArbolAVL();
        }
        return instancia;
    }
    
    public int getContador(){
        return contador;
    }
    
    public void limpiar() {
        this.raiz = null;
        this.contador = 0;
    }
    
    private int altura(Cancion cancion){
        if(cancion == null) return 0;
        
        else{
            return cancion.altura;
        }
    }
    
    private int factorBalance(Cancion cancion){
        if(cancion == null){
            return 0;
        }
        else{
            return altura(cancion.izquierda) - altura(cancion.derecha);
        }
    }
    
    private void actualizarAltura(Cancion cancion){
        cancion.altura = 1 + Math.max(altura(cancion.izquierda), altura(cancion.derecha));
    }
    
    private Cancion rotarDerecha(Cancion y) {
        Cancion x = y.izquierda;
        Cancion T2 = x.derecha;

        x.derecha = y;
        y.izquierda = T2;

        actualizarAltura(y);
        actualizarAltura(x);

        return x;
    }
    
    private Cancion rotarIzquierda(Cancion x) {
        Cancion y = x.derecha;
        Cancion T2 = y.izquierda;

        y.izquierda = x;
        x.derecha = T2;

        actualizarAltura(x);
        actualizarAltura(y);

        return y;
    }
    
    private Cancion balancear(Cancion cancion){
        actualizarAltura(cancion);
        int balance = factorBalance(cancion);
        
        if(balance > 1 && factorBalance(cancion.izquierda) >= 0){
            return rotarDerecha(cancion);
        }
        
        if(balance > 1 && factorBalance(cancion.izquierda) < 0){
            cancion.izquierda = rotarIzquierda(cancion.izquierda);
            return rotarDerecha(cancion);
        }
        
        if(balance < -1 && factorBalance(cancion.derecha) <= 0){
            return rotarIzquierda(cancion);
        }
        
        if(balance < -1 && factorBalance(cancion.derecha) > 0){
            cancion.derecha = rotarDerecha(cancion.derecha);
            return rotarIzquierda(cancion);
        }
        
        return cancion;
    }
    
    public void Insertar(String nombre, String artista, String album, String genero, int duracionSeg, long tamaño, String ruta, String año){
        raiz = insertarRecursivo(raiz, nombre, artista, album, genero, duracionSeg, tamaño, ruta, año);
    } 
    
    public Cancion insertarRecursivo(Cancion nodo, String nombre, String artista, String album, String genero, int duracionSeg, long tamaño, String ruta, String año){
        if(nodo == null){
            contador++;
            return new Cancion(nombre, artista, album, genero, duracionSeg, tamaño, ruta, año);
        }
        
        int cmp = nombre.compareToIgnoreCase(nodo.nombre);
        
        if(cmp < 0){
            nodo.izquierda = insertarRecursivo(nodo.izquierda, nombre, artista, album, genero, duracionSeg, tamaño, ruta, año);
        }
        if(cmp > 0){
            nodo.derecha = insertarRecursivo(nodo.derecha, nombre, artista, album, genero, duracionSeg, tamaño, ruta, año);
        }
        else{
            System.out.println("DUPLICADO----------");
            return nodo;
        }
        return balancear(nodo);
    }
    
    public void insertarDesdeArchivo(String ruta){
        try{
            File archivo = new File(ruta);
            AudioFile audioFile = AudioFileIO.read(archivo);
            
            Tag tag = audioFile.getTag();
            
            AudioHeader header = audioFile.getAudioHeader();
            
            String nombre = tag.getFirst(FieldKey.TITLE);

            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = archivo.getName();
            }




            String artista = tag.getFirst(FieldKey.ARTIST);
            if (artista == null || artista.trim().isEmpty()) {
                artista = "Desconocido";
            }

            String album = tag.getFirst(FieldKey.ALBUM);
            if (album == null || album.trim().isEmpty()) {
                album = "Sin álbum";
            }

            String genero  = tag.getFirst(FieldKey.GENRE);
            String año     = tag.getFirst(FieldKey.YEAR);
            int duracion   = header.getTrackLength();
            long tamaño    = archivo.length();

            this.Insertar(nombre, artista, album, genero, duracion, tamaño, ruta, año);
            
            
        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void insertarDesdeCarpeta(String rutaCarpeta){
        File carpeta = new File(rutaCarpeta);

        if (!carpeta.exists() || !carpeta.isDirectory()) return;

        File[] contenido = carpeta.listFiles();
        if (contenido == null) return;

        for (File archivo : contenido) {
            if (archivo.isDirectory()) {
                insertarDesdeCarpeta(archivo.getAbsolutePath());
            } else if (esAudio(archivo.getName())) {
                try {
                    insertarDesdeArchivo(archivo.getAbsolutePath());
                    System.out.println("CARGADA " + archivo.getName());
                } catch (Exception e) {
                    System.out.println("NO CARGADA " + archivo.getName());
                }
            }
        }
    }
    
    private boolean esAudio(String nombre) {
        String n = nombre.toLowerCase();
        return n.endsWith(".mp3")  ||
               n.endsWith(".flac") ||
               n.endsWith(".m4a")  ||
               n.endsWith(".ogg")  ||
               n.endsWith(".wav");
    }
    
    public void eliminar(Cancion eliminar) {
        raiz = eliminar(raiz, eliminar);
    }
    
    public Cancion eliminar(Cancion nodo, Cancion eliminar){
        if(nodo == null){
            return null;
        }
        
        int cmp = eliminar.nombre.compareToIgnoreCase(nodo.nombre);
        
        if(cmp < 0){
            nodo.izquierda = eliminar(nodo.izquierda, eliminar);
        }
        if(cmp > 0){
            nodo.derecha = eliminar(nodo.derecha, eliminar);
        }
        if(cmp == 0){
            nodo.izquierda = eliminar(nodo.izquierda, eliminar);
        }
        else{
            if(nodo.izquierda == null) return nodo.derecha;
            if(nodo.derecha == null) return nodo.izquierda;
            
            Cancion sucesor = minimo(nodo.derecha);
            nodo.nombre  = sucesor.nombre;
            nodo.derecha =  eliminar(nodo.derecha, sucesor);
        }
        return balancear(nodo);
    }
    
    private Cancion minimo(Cancion nodo) {
        while (nodo.izquierda != null) nodo = nodo.izquierda;
        return nodo;
    }
    
    public boolean buscar(Cancion nodo) {
        return buscar(raiz, nodo);
    }

    private boolean buscar(Cancion nodo, Cancion valor) {
        if (nodo == null) return false;
        if (valor == nodo) return true;
        
        int cmp = valor.nombre.compareToIgnoreCase(nodo.nombre);
        
        if(cmp < 0){
            return buscar(nodo.izquierda, valor);
        }
        if(cmp > 0){
            return buscar(nodo.derecha, valor);
        }
        else{
            return buscar(nodo.izquierda, valor);
        }
    }
    
    public void inorden() {
        inorden(raiz);
        System.out.println();
    }

    private void inorden(Cancion nodo) {
        if (nodo == null) return;
        inorden(nodo.izquierda);
        System.out.print(nodo.nombre + " ");
        inorden(nodo.derecha);
    }
    
    
    
}
