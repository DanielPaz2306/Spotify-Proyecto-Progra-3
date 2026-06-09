package com.estructuras;

import java.io.File;
import java.util.ArrayList;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class Playlist {

    public String nombre;
    public Cancion inicio;
    public Cancion fin;
    public int contador;

    public Playlist() {
        inicio = fin = null;
        contador = 0;
    }
    
    public Playlist(String nombre) {
        this.nombre = nombre;
        inicio = fin = null;
        contador = 0;
    }

    public boolean EstaVacia() {
        return inicio == null;
    }

    public void Insertar(String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año){
        
        Cancion nuevo = new Cancion(nombre, artista, album, genero, duracion, tamaño, ruta, año);
        
        if(EstaVacia()){
            inicio = fin = nuevo;
            nuevo.siguiente = nuevo;
            nuevo.anterior = nuevo;
        }
        
        else{
            nuevo.siguiente = inicio;
            nuevo.anterior = fin;
            inicio.anterior = nuevo;
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        
        contador ++;
    }
    
    public void Insertar(Cancion cancion){
            
        Cancion nuevo = new Cancion(cancion.nombre, cancion.artista, cancion.album, cancion.genero, cancion.duracionSeg, cancion.tamaño, cancion.ruta, cancion.año);
        
        if(EstaVacia()){
            inicio = fin = nuevo;
            nuevo.siguiente = nuevo;
            nuevo.anterior = nuevo;
        }
        
        else{
            nuevo.siguiente = inicio;
            nuevo.anterior = fin;
            inicio.anterior = nuevo;
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        
        contador ++;
    }
    
    public Cancion siguiente(Cancion actual){
        return actual.siguiente;
    }
    
    public Cancion anterior(Cancion actual) {
        return actual.anterior;
    }
    
    public boolean eliminar(Cancion c) {
        if (EstaVacia()) return false;

        c.anterior.siguiente = c.siguiente;
        
        c.siguiente.anterior = c.anterior;

        if (c == inicio) inicio = c.siguiente;
        
        if (c == fin) fin = c.anterior;
        
        if (inicio == c) inicio = fin = null; // era el único
        
        fin.siguiente = inicio;
        
        inicio.anterior = fin;
        
        contador--;
        
        return true;
    }

    public void insertarDesdeCarpeta(String rutaCarpeta){
        inicio = fin = null;
        contador = 0;
        
        File carpeta = new File(rutaCarpeta);

        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("La Carpeta no EXISTE");
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
            cargadas++;
        } catch (Exception e) {
            errores++;
        }
    }
    }
    
    public void insertarDesdeArchivo(String ruta){
        
        try 
        {
            File archivo = new File(ruta);
            AudioFile audioFile = AudioFileIO.read(archivo);
            
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();
            
            String nombre  = tag.getFirst(FieldKey.TITLE);
            String artista = tag.getFirst(FieldKey.ARTIST);
            String album   = tag.getFirst(FieldKey.ALBUM);
            String genero  = tag.getFirst(FieldKey.GENRE);
            String año     = tag.getFirst(FieldKey.YEAR);
            int duracion   = header.getTrackLength();
            long tamaño    = archivo.length();
            
            this.Insertar(nombre, artista, album, genero, duracion, tamaño, ruta, año);
            
        }
        
        catch(Exception e){
            System.out.println("error: " + e.getMessage());
        }
    }
    
    public Cancion buscarSiguiente(String ruta) {
       if (EstaVacia()) return null;

       Cancion temp = inicio;

       do {
           
           if (temp.ruta.equals(ruta)) {
               return temp.siguiente;
           }
           temp = temp.siguiente;
       } 
       while (temp != inicio);

       return null; // 
    }
    
    public Cancion buscarAnterior(String ruta){
        if(EstaVacia()) return null;
        
        Cancion temp = inicio;
        
        do {
            if(temp.ruta.equals(ruta)){
                return temp.anterior;
            }
            temp = temp.siguiente;
        }
        
        while (temp != inicio);
        
        return null;
    }
    
    public boolean existe(Cancion cancion){
        if(EstaVacia()) return false;
        
        Cancion temp = inicio;
        
        do {
            if(temp.ruta.equals(cancion.ruta)){
                return true;
            }
            temp = temp.siguiente;
        }
        
        while (temp != inicio);
        
        return false;
    }
    
    public void limpiarLista(){
        inicio = fin = null;
    }

    @Override
    public String toString() {
        return nombre + " - Canciones: " + contador ;
    }
    
    public ArrayList<Cancion> getPlaylistEnArray(Playlist playlist){
        ArrayList<Cancion> array = new ArrayList<>();
        
        if(playlist.EstaVacia()) return array;
        
        Cancion temp = playlist.inicio;
        
        do{
            array.add(temp);
            temp = temp.siguiente;
        }
        while(temp != playlist.inicio);
        
        
        return array;
        
    }
    
}
