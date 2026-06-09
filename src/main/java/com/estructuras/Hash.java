/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estructuras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

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
    
    public void clasificarCanciones(DefaultListModel<Cancion> canciones){
        
        for(int i = 0; i < canciones.getSize(); i++){
            Cancion cancion = canciones.getElementAt(i);
            
            hashGenero.computeIfAbsent(cancion.genero.toLowerCase(), k -> new ArrayList<>()).add(cancion);
            hashArtista.computeIfAbsent(cancion.artista.toLowerCase(), k -> new ArrayList<>()).add(cancion);
        }
        JOptionPane.showMessageDialog(null, "AGREGADAS AL HASH");
    }
    
    public DefaultListModel<Cancion> buscarPorArtista(String artista) {
        DefaultListModel<Cancion> resultado = new DefaultListModel<>();
        List<Cancion> canciones = hashArtista.getOrDefault(artista.toLowerCase(), Collections.emptyList());
        for (Cancion c : canciones) {
            resultado.addElement(c);
        }
        return resultado;
    }
    
    public DefaultListModel<Cancion> buscarPorGenero(String genero) {
        DefaultListModel<Cancion> resultado = new DefaultListModel<>();
        List<Cancion> canciones = hashGenero.getOrDefault(genero.toLowerCase(), Collections.emptyList());
        for (Cancion c : canciones) {
            resultado.addElement(c);
        }
        return resultado;
    }
    
    public DefaultListModel<Cancion> buscarPorArtistaParcial(String texto) {
        DefaultListModel<Cancion> resultado = new DefaultListModel<>();
        for (String artista : hashArtista.keySet()) {
            if (artista.toLowerCase().contains(texto.toLowerCase())) {
                for (Cancion c : hashArtista.get(artista)) {
                    resultado.addElement(c);
                }
            }
        }
        return resultado;
    }

    public DefaultListModel<Cancion> buscarPorGeneroParcial(String texto) {
        DefaultListModel<Cancion> resultado = new DefaultListModel<>();
        for (String genero : hashGenero.keySet()) {
            if (genero.toLowerCase().contains(texto.toLowerCase())) {
                for (Cancion c : hashGenero.get(genero)) {
                    resultado.addElement(c);
                }
            }
        }
        return resultado;
    }
    
}
