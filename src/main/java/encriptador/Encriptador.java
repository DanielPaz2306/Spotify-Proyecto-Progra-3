/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package encriptador;

import com.estructuras.Cancion;
import com.estructuras.Playlist;
import com.utilidades.Reproductor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;

/**
 *
 * @author pchin
 */
public class Encriptador {
    
    private static final String ALGORITMO = "AES";
    private static final String CLAVE = "CLAVESUPERSEGURA";
    
    public static String encriptar(String texto) throws Exception{
        SecretKeySpec key = new SecretKeySpec(CLAVE.getBytes(StandardCharsets.UTF_8), ALGORITMO);
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encriptado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encriptado);
    }
    
    public static String desencriptar(String texto) throws Exception{
        SecretKeySpec key = new SecretKeySpec(CLAVE.getBytes(StandardCharsets.UTF_8), ALGORITMO);
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodificado = Base64.getDecoder().decode(texto);
        return new String(cipher.doFinal(decodificado), StandardCharsets.UTF_8);
    }
    
    public Encriptador(){}
    
    public String encriptarPlaylist(Playlist playlist) throws Exception{
        if (playlist == null || playlist.nombre == null || playlist.nombre.isBlank()) {
            throw new IllegalArgumentException("La playlist no es válida o no tiene nombre.");
        }   
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Eliga la carpeta donde guardar su archivo");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null; 
        }
        
        String carpeta = chooser.getSelectedFile().getAbsolutePath();
        String rutaArchivo = carpeta + File.separator + playlist.nombre + "_encriptada.txt";
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo, StandardCharsets.UTF_8))){
            Cancion actual = playlist.inicio;
            if (actual != null) {
                do {
                    writer.write(encriptar(actual.ruta));
                    writer.newLine();
                    actual = actual.siguiente;
                } while (actual != playlist.inicio);
            }
            return rutaArchivo;
        }
    }
    
    public Playlist desencriptarPlaylist() throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccione el archivo de playlist encriptado");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        
        File archivo = chooser.getSelectedFile();
        String nombrePlaylist = archivo.getName().replace("_encriptada.txt", "").replace(".txt", "");
        
        Playlist playlist = new Playlist(nombrePlaylist);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String rutaDesencriptada = desencriptar(linea.trim());
                Cancion cancion = Reproductor.buscarCancion(rutaDesencriptada);
                if (cancion != null) {
                    playlist.Insertar(cancion);
                }
            }
        }
        
        return playlist;
    }
}
