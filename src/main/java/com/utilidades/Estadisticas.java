/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.utilidades;

import com.estructuras.Cancion;
import com.estructuras.Hash;
import com.estructuras.Playlist;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pchin
 */
public class Estadisticas {
    
    
    
    public Cancion getCancionMasReproducida(Playlist playlist){
        ArrayList<Cancion> array = playlist.getPlaylistEnArray(playlist);
        Cancion mayor = array.get(0);
        
        
        for(Cancion c : array){
            if(c.reproducciones > mayor.reproducciones){
                mayor = c;
            }
        }
        return mayor;
    }

    public String getArtistaMasReproducido(Playlist playlist) {
        ArrayList<Cancion> array = playlist.getPlaylistEnArray(playlist);
        HashMap<String, Integer> artistas = new HashMap<>();

        for (Cancion c : array) {
            artistas.put(
                c.artista,
                artistas.getOrDefault(c.artista, 0)
                + c.reproducciones
            );
        }
        
        String artistaMasEscuchado = "";
        int maxReproducciones = 0;

        for (Map.Entry<String, Integer> entry : artistas.entrySet()) {
            if (entry.getValue() > maxReproducciones) {
                maxReproducciones = entry.getValue();
                artistaMasEscuchado = entry.getKey();
            }
        }
        
        String respuesta = artistaMasEscuchado + " con " + maxReproducciones + " reproducciones";
        
        return respuesta;
    }
    
    
}
