package com.utilidades;

import com.estructuras.Cancion;
import com.estructuras.ListaDobleEnlazadaCircular;
import java.io.File;

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
    public  Cancion cancionActual = null;

    public Reproductor() {

        // Inicializa JavaFX
        new JFXPanel();
    }

    public void reproducir(Cancion cancion, ListaDobleEnlazadaCircular lista) {

        detener();
        Media media = new Media(new File(cancion.ruta).toURI().toString());
        player = new MediaPlayer(media);
        cancionActual = cancion;

        player.setOnEndOfMedia(() -> siguienteCancion(lista));
        player.play();

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
    
    public Cancion buscarCancion(String ruta) {
            
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
    public void siguienteCancion(ListaDobleEnlazadaCircular lista) {
        if (cancionActual == null || lista == null) return;

        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equals(cancionActual.ruta)) {
                reproducir(actual.siguiente, lista);
                return;
            }
            actual = actual.siguiente;
        } while (actual != lista.inicio);
    }
    
    public void anteriorCancion(ListaDobleEnlazadaCircular lista) {
        if (cancionActual == null || lista == null) return;

        Cancion actual = lista.inicio;
        do {
            if (actual.ruta.equals(cancionActual.ruta)) {
                reproducir(actual.siguiente, lista);
                return;
            }
            actual = actual.siguiente;
        } while (actual != lista.inicio);
    }
}