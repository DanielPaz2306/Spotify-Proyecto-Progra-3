package com.utilidades;

import com.estructuras.Cancion;
import com.estructuras.Historial;
import com.estructuras.Playlist;
import com.estructuras.Cola;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.JOptionPane;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class Reproductor {

    private MediaPlayer player;
    public  Cancion cancionActual = new Cancion("","","","",0,0,"","");
    public Playlist playlistActual = null;
    public int modo = 1; // Default to Normal mode (1)
    private ReproductorListener listener;
    public Cola cola = new Cola();
    public Cancion originalPlaylistTrack = null;

    public Reproductor() {

        new JFXPanel();
    }

    public void setListener(ReproductorListener listener) {
        this.listener = listener;
    }

    public void setModo(int nuevoModo) {
        this.modo = nuevoModo;
        if (player != null && playlistActual != null) {
            player.setOnEndOfMedia(() -> avanzarSiguiente(playlistActual));
        }
    }

    public void reproducir(Cancion cancion, Playlist lista) {
        originalPlaylistTrack = cancion;
        reproducirInterno(cancion, lista);
    }

    public void reproducirDeCola(Cancion cancion, Playlist lista) {
        reproducirInterno(cancion, lista);
    }

    private void reproducirInterno(Cancion cancion, Playlist lista) {
        detener();
        Historial historial = Historial.getInstancia();
        historial.Insertar(cancion);
        Media media = new Media(new File(cancion.ruta).toURI().toString());
        player = new MediaPlayer(media);
        cancionActual = cancion;
        playlistActual = lista;
        player.setOnEndOfMedia(() -> avanzarSiguiente(lista));
        player.play();

        if (listener != null) {
            listener.onCancionCambiada(cancionActual);
        }

        JOptionPane.showMessageDialog(null, "Reproduciendo: " + cancionActual.nombre 
                                        + "\n De: " + cancionActual.artista);
    }

    public void pausar() {

        if (player != null) {
            player.pause();
        }
    }
    
    public Cancion getCancionActual(){
        return cancionActual;
    }

    public void reanudar() {

        if (player != null) {
            player.play();
        }
    }

    public void detener() {

        if (player != null) {
            player.stop();
        }
    }

    public boolean estaReproduciendo() {

        return player != null &&
               player.getStatus() == MediaPlayer.Status.PLAYING;
    }
    
    public static Cancion buscarCancion(String ruta) {
            
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



                Cancion c = new Cancion(nombre, artista, album, genero, duracion, tamaño, ruta, año);
                return c;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
                return null;
        }
    
    public void avanzarSiguiente(Playlist lista) {
        if (!cola.estaVacia()) {
            Cancion sig = cola.desencolar();
            reproducirDeCola(sig, lista);
        } else {
            if (modo == 1) {
                siguienteNormal(lista);
            } else if (modo == 2) {
                siguienteAleatoria(lista);
            } else if (modo == 3) {
                siguienteInfinita(lista);
            }
        }
    }

    public void retrocederAnterior(Playlist lista) {
        if (lista == null || originalPlaylistTrack == null) return;
        
        if (modo == 2) {
            siguienteAleatoria(lista);
        } else {
            anteriorCancion(lista);
        }
    }

    public void siguienteCancion(Playlist lista) {
        if (originalPlaylistTrack == null || lista == null) return;

        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equalsIgnoreCase(originalPlaylistTrack.ruta)) {
                
                if(actual.siguiente == lista.inicio){
                    reproducir(lista.inicio, lista);
                    return;
                }
                else{
                    reproducir(actual.siguiente, lista);
                    return;
                }

            }
            actual = actual.siguiente;
        } while (actual != lista.inicio);
    }
    
    public void anteriorCancion(Playlist lista) {
        if (originalPlaylistTrack == null || lista == null) return;

        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equalsIgnoreCase(originalPlaylistTrack.ruta)) {
                if(actual == lista.inicio){
                    reproducir(lista.fin, lista);
                    return;
                }
                else{
                    reproducir(actual.anterior, lista);
                    return;                    
                }

            }
            actual = actual.anterior;
        } while (actual != lista.inicio);
    }

    public void siguienteNormal(Playlist lista){
        if (originalPlaylistTrack == null || lista == null) return;

        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equalsIgnoreCase(originalPlaylistTrack.ruta)) {
                
                if(actual.siguiente == lista.inicio){
                    detener();
                    return;
                }
                else{
                    reproducir(actual.siguiente, lista);
                    return;
                }

            }
            actual = actual.siguiente;
        } while (actual != lista.inicio); 
    }
    
    public void siguienteInfinita(Playlist lista){
        if (originalPlaylistTrack == null || lista == null) return;
        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equalsIgnoreCase(originalPlaylistTrack.ruta)) {
                
                if(actual.siguiente == lista.inicio){
                    reproducir(lista.inicio, lista);
                    return;
                }
                else{
                    reproducir(actual.siguiente, lista);
                    return;
                }

            }
            actual = actual.siguiente;
        } while (actual != lista.inicio);
    }
    
    public void siguienteAleatoria(Playlist lista){
        if (lista == null || lista.inicio == null) return;
        
        ArrayList<Cancion> listaArray = new ArrayList<>();
        Cancion temp = lista.inicio;
        do {
            listaArray.add(temp);
            temp = temp.siguiente;
        } while (temp != lista.inicio);
        
        if (listaArray.isEmpty()) return;
        
        if (listaArray.size() == 1) {
            reproducir(listaArray.get(0), lista);
            return;
        }
        
        int index = -1;
        for (int i = 0; i < listaArray.size(); i++) {
            if (originalPlaylistTrack != null && listaArray.get(i).ruta.equalsIgnoreCase(originalPlaylistTrack.ruta)) {
                index = i;
                break;
            }
        }
        
        Random random = new Random();
        int num;
        do {
            num = random.nextInt(listaArray.size());
        } while (num == index);
        
        reproducir(listaArray.get(num), lista);
    }
    
    
}