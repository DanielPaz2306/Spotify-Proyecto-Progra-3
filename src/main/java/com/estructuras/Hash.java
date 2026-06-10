/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author pchin
 */
public class Hash {
    
    private static Hash instancia;
    
    private HashMap<String, List<Cancion>> hashGenero = new HashMap<>();
    private HashMap<String, List<Cancion>> hashArtista = new HashMap<>();
    
    public Hash(){}
    
    public static Hash getInstancia() {
        if (instancia == null) {
            instancia = new Hash();
        }
        return instancia;
    }
    
    public void clasificarCanciones(List<Cancion> canciones){
        for(Cancion cancion : canciones){
            hashGenero.computeIfAbsent(cancion.genero.toLowerCase(), k -> new ArrayList<>()).add(cancion);
            hashArtista.computeIfAbsent(cancion.artista.toLowerCase(), k -> new ArrayList<>()).add(cancion);
        }
        System.out.println("AGREGADAS AL HASH");
    }
    
    public List<Cancion> buscarPorArtista(String artista) {
        List<Cancion> resultado = new ArrayList<>();
        List<Cancion> canciones = hashArtista.getOrDefault(artista.toLowerCase(), Collections.emptyList());
        resultado.addAll(canciones);
        return resultado;
    }
    
    public List<Cancion> buscarPorGenero(String genero) {
        List<Cancion> resultado = new ArrayList<>();
        List<Cancion> canciones = hashGenero.getOrDefault(genero.toLowerCase(), Collections.emptyList());
        resultado.addAll(canciones);
        return resultado;
    }
    
    public List<Cancion> buscarPorArtistaParcial(String texto) {
        List<Cancion> resultado = new ArrayList<>();
        for (String artista : hashArtista.keySet()) {
            if (artista.toLowerCase().contains(texto.toLowerCase())) {
                resultado.addAll(hashArtista.get(artista));
            }
        }
        return resultado;
    }

    public List<Cancion> buscarPorGeneroParcial(String texto) {
        List<Cancion> resultado = new ArrayList<>();
        for (String genero : hashGenero.keySet()) {
            if (genero.toLowerCase().contains(texto.toLowerCase())) {
                resultado.addAll(hashGenero.get(genero));
            }
        }
        return resultado;
    }
    
}
