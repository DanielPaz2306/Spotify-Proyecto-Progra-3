/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.utilidades;

import com.estructuras.Cancion;
import com.estructuras.Playlist;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pchin
 */
public class Estadisticas {
    
    private static final String FILE_PATH = "estadisticas.txt";
    private static Map<String, Integer> reproduccionesMap = new HashMap<>();

    static {
        cargarEstadisticas();
    }

    public static void cargarEstadisticas() {
        File archivo = new File(FILE_PATH);
        if (!archivo.exists()) {
            reproduccionesMap = new HashMap<>();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            reproduccionesMap.clear();
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\\|", 2);
                if (partes.length == 2) {
                    try {
                        int plays = Integer.parseInt(partes[0].trim());
                        String ruta = partes[1].trim();
                        reproduccionesMap.put(ruta, plays);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parseando reproducciones: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar estadisticas: " + e.getMessage());
        }
    }

    public static void guardarEstadisticas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, StandardCharsets.UTF_8))) {
            for (Map.Entry<String, Integer> entry : reproduccionesMap.entrySet()) {
                writer.write(entry.getValue() + "|" + entry.getKey());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar estadisticas: " + e.getMessage());
        }
    }

    public static int getPlayCount(String ruta) {
        if (ruta == null) return 0;
        return reproduccionesMap.getOrDefault(ruta.trim(), 0);
    }

    public static void registrarReproduccion(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) return;
        String rutaKey = ruta.trim();
        reproduccionesMap.put(rutaKey, getPlayCount(rutaKey) + 1);
        guardarEstadisticas();
    }

    // --- Métodos de cálculo de Estadísticas ---

    public static String getCancionMasReproducida(Playlist generalPlaylist) {
        if (generalPlaylist == null || generalPlaylist.EstaVacia()) {
            return "No hay canciones cargadas en la biblioteca.";
        }
        ArrayList<Cancion> array = generalPlaylist.getPlaylistEnArray(generalPlaylist);
        Cancion mayor = null;
        int maxPlays = -1;
        for (Cancion c : array) {
            int plays = getPlayCount(c.ruta);
            if (plays > maxPlays) {
                maxPlays = plays;
                mayor = c;
            }
        }
        if (mayor == null || maxPlays <= 0) {
            return "Ninguna canción ha sido reproducida aún.";
        }
        return mayor.nombre + " - " + mayor.artista + " (" + maxPlays + " reproducciones)";
    }

    public static String getArtistaMasReproducido(Playlist generalPlaylist) {
        if (generalPlaylist == null || generalPlaylist.EstaVacia()) {
            return "No hay canciones cargadas en la biblioteca.";
        }
        ArrayList<Cancion> array = generalPlaylist.getPlaylistEnArray(generalPlaylist);
        HashMap<String, Integer> artistaPlays = new HashMap<>();
        for (Cancion c : array) {
            int plays = getPlayCount(c.ruta);
            if (c.artista != null && !c.artista.trim().isEmpty()) {
                artistaPlays.put(c.artista, artistaPlays.getOrDefault(c.artista, 0) + plays);
            }
        }
        String topArtista = null;
        int maxPlays = 0;
        for (Map.Entry<String, Integer> entry : artistaPlays.entrySet()) {
            if (entry.getValue() > maxPlays) {
                maxPlays = entry.getValue();
                topArtista = entry.getKey();
            }
        }
        if (topArtista == null || maxPlays <= 0) {
            return "Ningún artista ha sido escuchado aún.";
        }
        return topArtista + " (" + maxPlays + " reproducciones en total)";
    }

    public static String getPlaylistMasGrande(ArrayList<Playlist> playlists) {
        if (playlists == null || playlists.isEmpty()) {
            return "No hay playlists creadas.";
        }
        Playlist masGrande = null;
        int maxCanciones = -1;
        for (Playlist pl : playlists) {
            if (pl.nombre != null && pl.nombre.equalsIgnoreCase("GENERAL")) continue;
            if (pl.contador > maxCanciones) {
                maxCanciones = pl.contador;
                masGrande = pl;
            }
        }
        if (masGrande == null || maxCanciones <= 0) {
            return "No hay playlists personalizadas con canciones.";
        }
        return masGrande.nombre + " (" + maxCanciones + " canciones)";
    }

    public static String getPlaylistMasLarga(ArrayList<Playlist> playlists) {
        if (playlists == null || playlists.isEmpty()) {
            return "No hay playlists creadas.";
        }
        Playlist masLarga = null;
        int maxDuracion = -1;
        for (Playlist pl : playlists) {
            if (pl.nombre != null && pl.nombre.equalsIgnoreCase("GENERAL")) continue;
            
            ArrayList<Cancion> array = pl.getPlaylistEnArray(pl);
            int duracionTotal = 0;
            for (Cancion c : array) {
                duracionTotal += c.duracionSeg;
            }
            
            if (duracionTotal > maxDuracion) {
                maxDuracion = duracionTotal;
                masLarga = pl;
            }
        }
        if (masLarga == null || maxDuracion <= 0) {
            return "No hay playlists personalizadas con canciones.";
        }
        int m = maxDuracion / 60;
        int s = maxDuracion % 60;
        return masLarga.nombre + " (" + m + "m " + s + "s)";
    }

    public static String getGeneroMasFrecuentePorCantidad(Playlist generalPlaylist) {
        if (generalPlaylist == null || generalPlaylist.EstaVacia()) {
            return "No hay canciones cargadas en la biblioteca.";
        }
        ArrayList<Cancion> array = generalPlaylist.getPlaylistEnArray(generalPlaylist);
        HashMap<String, Integer> generoCount = new HashMap<>();
        for (Cancion c : array) {
            String gen = (c.genero == null || c.genero.isBlank()) ? "Desconocido" : c.genero.trim();
            generoCount.put(gen, generoCount.getOrDefault(gen, 0) + 1);
        }
        String topGenero = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : generoCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topGenero = entry.getKey();
            }
        }
        if (topGenero == null) {
            return "N/A";
        }
        return topGenero + " (" + maxCount + " canciones)";
    }

    public static String getGeneroMasFrecuentePorReproducciones(Playlist generalPlaylist) {
        if (generalPlaylist == null || generalPlaylist.EstaVacia()) {
            return "No hay canciones cargadas en la biblioteca.";
        }
        ArrayList<Cancion> array = generalPlaylist.getPlaylistEnArray(generalPlaylist);
        HashMap<String, Integer> generoPlays = new HashMap<>();
        for (Cancion c : array) {
            String gen = (c.genero == null || c.genero.isBlank()) ? "Desconocido" : c.genero.trim();
            int plays = getPlayCount(c.ruta);
            generoPlays.put(gen, generoPlays.getOrDefault(gen, 0) + plays);
        }
        String topGenero = null;
        int maxPlays = 0;
        for (Map.Entry<String, Integer> entry : generoPlays.entrySet()) {
            if (entry.getValue() > maxPlays) {
                maxPlays = entry.getValue();
                topGenero = entry.getKey();
            }
        }
        if (topGenero == null || maxPlays <= 0) {
            return "Ninguna canción ha sido reproducida aún.";
        }
        return topGenero + " (" + maxPlays + " reproducciones)";
    }

    public static String getPromedioDuracion(Playlist generalPlaylist) {
        if (generalPlaylist == null || generalPlaylist.EstaVacia()) {
            return "0s";
        }
        ArrayList<Cancion> array = generalPlaylist.getPlaylistEnArray(generalPlaylist);
        double suma = 0;
        for (Cancion c : array) {
            suma += c.duracionSeg;
        }
        double promedio = suma / array.size();
        int promSeg = (int) Math.round(promedio);
        int m = promSeg / 60;
        int s = promSeg % 60;
        return m + "m " + s + "s (" + promSeg + " segundos)";
    }
}

