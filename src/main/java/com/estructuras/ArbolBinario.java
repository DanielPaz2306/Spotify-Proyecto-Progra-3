/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;
import java.io.File;
import javax.swing.DefaultListModel;

/**
 *
 * @author pchin
 */
public class ArbolBinario {
    public Cancion raiz;
    private static ArbolBinario instancia;
    
    public int cantidad;

    private ArbolBinario() {}

    public static ArbolBinario getInstancia() {
        if (instancia == null) {
            instancia = new ArbolBinario();
        }
        return instancia;
    }    

    public boolean EstaVacio(){
        if(raiz == null){
            return true;
        }
        else{
            return false;
        }
    }
    
    public void insertar(String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año){
        raiz = insertarRecursivo(raiz, nombre, artista, album, genero, duracion, tamaño, ruta, año);
    }
    
    private Cancion insertarRecursivo(Cancion actual, String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año){
        if(actual == null){
            cantidad++;
            return new Cancion(nombre, artista, album, genero, duracion, tamaño, ruta, año);
        }
        int cmp = nombre.compareToIgnoreCase(actual.nombre);
        
        if(cmp < 0){
            actual.izquierda = insertarRecursivo(actual.izquierda, nombre, artista, album, genero, duracion, tamaño, ruta, año);
        }
        else if(cmp > 0) {
            actual.derecha = insertarRecursivo(actual.derecha, nombre, artista, album, genero, duracion, tamaño, ruta, año);
        }
        else {
            System.out.println("DUPLICADO: " + nombre);
        }
        

        return actual;
    }
    
    public String recorrerInOrder(Cancion cancion) {
    if (cancion == null) return "";

    String resultado = "";
    resultado += recorrerInOrder(cancion.izquierda);
    resultado +=" [ " + cancion.nombre + 
                " | " + cancion.artista + 
                " | " + cancion.album + 
                " | " + cancion.genero + 
                " | " + cancion.duracionReal + 
                " | " + cancion.tamaño + 
                " | " + cancion.ruta  + 
                " | " + cancion.año + 
                " ]\n";
    resultado += recorrerInOrder(cancion.derecha);

    return resultado;
}
    
    public void recorrerPreOrder(Cancion cancion) {
        if (cancion != null) {
            System.out.println("[ " + cancion.nombre + 
                    " | " + cancion.artista + 
                    " | " + cancion.album + 
                    " | " + cancion.genero + 
                    " | " + cancion.duracionReal + 
                    " | " + cancion.tamaño + 
                    " | " + cancion.ruta  + 
                    " | " + cancion.año + 
                    " ]");
            recorrerPreOrder(cancion.izquierda);
            recorrerPreOrder(cancion.derecha);
        }
    }
    
    public void recorrerPostOrder(Cancion cancion) {
    if (cancion != null) {
        recorrerPostOrder(cancion.izquierda);
        recorrerPostOrder(cancion.derecha);
        System.out.println("[ " + cancion.nombre + 
                " | " + cancion.artista + 
                " | " + cancion.album + 
                " | " + cancion.genero + 
                " | " + cancion.duracionReal + 
                " | " + cancion.tamaño + 
                " | " + cancion.ruta  + 
                " | " + cancion.año + 
                " ]");
    }
    }
 
    public void insertarDesdeArchivo(String ruta) {
    
        try {
        File archivo = new File(ruta);
        AudioFile audioFile = AudioFileIO.read(archivo);

        Tag tag          = audioFile.getTag();
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
        


        this.insertar(nombre, artista, album, genero, duracion, tamaño, ruta, año);
        
    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
        
}
    
    public void cargarCarpeta(String rutaCarpeta) {
    
    File carpeta = new File(rutaCarpeta);


    if (!carpeta.exists() || !carpeta.isDirectory()) {
        System.out.println("LA CARPETA NO EXISTE");
        return;
    }

    File[] archivos = carpeta.listFiles((dir, nombre) ->
        nombre.toLowerCase().endsWith(".mp3")  ||
        nombre.toLowerCase().endsWith(".flac") ||
        nombre.toLowerCase().endsWith(".m4a")  ||
        nombre.toLowerCase().endsWith(".ogg")  ||
        nombre.toLowerCase().endsWith(".wav")
    );

    if (archivos == null || archivos.length == 0) {
        System.out.println("NO HAY CANCIONES EN LA CARPETA");
        return;
    }

    int cargadas = 0;
    int errores  = 0;

    for (File archivo : archivos) {
        try {
            insertarDesdeArchivo(archivo.getAbsolutePath());
            System.out.println("Cancion cargada: " + archivo.getName());
            cargadas++;
        } catch (Exception e) {
            System.out.println("No se pudo cargar: " + archivo.getName() + " → " + e.getMessage());
            errores++;
        }
    }

    System.out.println("\nTotal cargadas: " + cargadas + " | Errores: " + errores);
}
    
    public Cancion getRaiz(){
        return raiz;
    }
    
    public void inOrderALista(Cancion cancion, DefaultListModel<Cancion> modelo) {
        if (cancion == null) return;
        inOrderALista(cancion.izquierda, modelo);
        modelo.addElement(cancion);
        inOrderALista(cancion.derecha, modelo);
    }
    
    public void inOrderAListaDoble(Cancion cancion, ListaDobleEnlazadaCircular lista) {
        if (cancion == null) return;

        inOrderAListaDoble(cancion.izquierda, lista);

        System.out.println("INSERTANDO ----------------------");
        lista.Insertar(
            cancion.nombre,
            cancion.artista,
            cancion.album,
            cancion.genero,
            cancion.duracionSeg, 
            cancion.tamaño,
            cancion.ruta,
            cancion.año
        );

        inOrderAListaDoble(cancion.derecha, lista);
    }


}
